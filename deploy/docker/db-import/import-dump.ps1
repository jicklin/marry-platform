<#
.SYNOPSIS
  Import a legacy PostgreSQL dump into the running marry-platform stack.

.DESCRIPTION
  Mirrors import-dump.sh. Auto-detects plain-text SQL vs binary (-Fc)
  vs tar (-Ft) and dispatches to psql / pg_restore inside the
  postgres container.

.PARAMETER DumpPath
  Path to the dump file on the host. Can be a Windows path or a
  WSL path; will be converted automatically.

.PARAMETER Clean
  Drop existing objects before restoring (DANGEROUS — wipes the
  admin/demo seed).

.PARAMETER DropSchema
  DROP SCHEMA public CASCADE before restoring. DANGEROUS.

.PARAMETER Schema
  Schema to restore (default: public).

.PARAMETER NoOwner
  Don't set ownership (default: ON, safe).

.PARAMETER NoPublic
  Skip the public schema.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$DumpPath,

    [switch]$Clean,
    [switch]$DropSchema,
    [switch]$NoOwner = $true,
    [switch]$Owner,
    [string]$Schema = 'public',
    [switch]$NoPublic
)

$ErrorActionPreference = 'Stop'

# Force UTF-8 console output so Chinese / Unicode paths print correctly
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$ScriptDir    = Split-Path -Parent $PSCommandPath
$ComposeFile  = Join-Path $ScriptDir '..\docker-compose.yml'
$EnvResolved  = Join-Path $ScriptDir '..\.env.resolved'

if ($Owner) { $NoOwner = $false }
if ($NoPublic) { $Schema = '' }

# Detect compose command (v2 plugin or v1 standalone)
$ComposeCmd = $null
try { docker compose version | Out-Null; $ComposeCmd = 'docker compose' } catch {}
if (-not $ComposeCmd) {
    try { docker-compose version | Out-Null; $ComposeCmd = 'docker-compose' } catch {}
}
if (-not $ComposeCmd) { throw "Neither `docker compose` nor `docker-compose` is available." }

# ---- WSL path conversion (host -> unix) ----
function ConvertTo-DockerPath {
    param([string]$P)
    if ([string]::IsNullOrEmpty($P)) { return $P }
    if ($P -match '^([A-Za-z]):[\\/](.+)$') {
        return "/mnt/$($Matches[1].ToLower())/$($Matches[2] -replace '\\','/')"
    }
    return $P
}

function Test-DumpReachable {
    param([string]$P)
    # Try Windows path first
    if (Test-Path -LiteralPath $P) { return (Resolve-Path -LiteralPath $P).Path }
    # Then WSL path
    $wsl = ConvertTo-DockerPath $P
    $exists = wsl.exe test -e $wsl 2>$null
    if ($LASTEXITCODE -eq 0) { return $wsl }
    throw "Dump file not reachable: $P  (also tried: $wsl)"
}

if (-not (Test-Path $ComposeFile)) {
    throw "docker-compose.yml not found at $ComposeFile. Run \`.\up.ps1\` first."
}

# ---- stack running? ----
$tokens = $ComposeCmd -split ' '
    $cmdParts = $tokens + @('-f', $ComposeFile, 'ps', '--status', 'running', 'postgres')
    $running = & $cmdParts[0] @($cmdParts[1..($cmdParts.Length-1)]) 2>$null
    & $cmdParts[0] @($cmdParts[1..($cmdParts.Length-1)]) 2>$null
if ($LASTEXITCODE -ne 0 -or -not ($running -match 'postgres')) {
    throw "Postgres container is not running. Start the stack with \`.\up.ps1\` first."
}

$DOCKER_USER = $env:POSTGRES_USER; if (-not $DOCKER_USER) { $DOCKER_USER = 'marry' }
$DOCKER_DB   = $env:POSTGRES_DB;   if (-not $DOCKER_DB)   { $DOCKER_DB   = 'marry_platform' }

Write-Host "==> Importing: $DumpPath" -ForegroundColor Cyan
Write-Host "    User / DB : $DOCKER_USER @ $DOCKER_DB" -ForegroundColor DarkGray
Write-Host "    Schema    : $(if ($Schema) { $Schema } else { '<none>' })" -ForegroundColor DarkGray
Write-Host "    Flags     : clean=$Clean drop-schema=$DropSchema no-owner=$NoOwner" -ForegroundColor DarkGray

# ---- safety prompts ----
if ($Clean) {
    Write-Host "" -ForegroundColor Yellow
    Write-Host "WARNING: --clean will DROP existing objects. The admin/demo seed WILL be wiped." -ForegroundColor Yellow
    $ans = Read-Host "Type 'yes' to continue"
    if ($ans -ne 'yes') { throw "Aborted by user." }
}
if ($DropSchema) {
    Write-Host "" -ForegroundColor Yellow
    Write-Host "WARNING: --drop-schema will DROP SCHEMA $Schema CASCADE." -ForegroundColor Yellow
    $ans = Read-Host "Type 'yes' to continue"
    if ($ans -ne 'yes') { throw "Aborted by user." }
}

# ---- resolve final host path / WSL path ----
$finalPath = Test-DumpReachable -P $DumpPath
$basename  = Split-Path -Leaf $finalPath
$isWsl     = $finalPath -match '^/mnt/'

Write-Host "==> Resolved path: $finalPath (via $(if ($isWsl) {'WSL'} else {'host FS'}))"

# ---- copy into container ----
$containerDump = "/tmp/import-$PID-$basename"
Write-Host "==> Copying dump into postgres container" -ForegroundColor Cyan

if ($isWsl) {
    # File lives in WSL; use docker exec with cat redirect
    # (docker cp from WSL to WSL-vm isn't supported directly)
    Get-Content -Raw -Path "wsl:$finalPath" 2>$null | Out-Null
    # That doesn't actually read WSL files. Use docker exec with cat:
    wsl.exe cat "$finalPath" | docker exec -i marry-postgres sh -c "cat > $containerDump"
} else {
    docker cp $finalPath "marry-postgres:$containerDump"
}

try {
    # ---- detect format ----
    $head = if ($isWsl) { (wsl.exe head -c 200 "$finalPath" 2>$null) } else { try { $bytes = [System.IO.File]::ReadAllBytes($finalPath); [System.Text.Encoding]::UTF8.GetString($bytes, 0, [Math]::Min(200, $bytes.Length)) } catch { '' } }    $isText = ($head -match '^--|CREATE TABLE|INSERT INTO|^\s*SET\s')
    if (-not $isText) {
        # also try `file` heuristic
        try {
            $ft = if ($isWsl) { (wsl.exe file "$finalPath" 2>$null) } else { (file "$finalPath" 2>$null) }
            if ($ft -match 'text|ASCII|SQL') { $isText = $true }
        } catch {}
    }

    if ($isText) {
        Write-Host "==> Detected plain-text SQL — using psql" -ForegroundColor Cyan
        $psqlArgs = @('-v', 'ON_ERROR_STOP=1', '-U', $DOCKER_USER, '-d', $DOCKER_DB, '-f', $containerDump)
        docker exec -u postgres marry-postgres psql @psqlArgs
    } else {
        Write-Host "==> Detected binary/tar dump — using pg_restore" -ForegroundColor Cyan
        $pgArgs = @('--no-owner', '--no-privileges', '-U', $DOCKER_USER, '-d', $DOCKER_DB)
        if ($NoOwner) { $pgArgs = @('--no-owner', '--no-privileges', '-U', $DOCKER_USER, '-d', $DOCKER_DB) }
        if ($Schema)  { $pgArgs += @('--schema=' + $Schema) }
        if ($Clean)   { $pgArgs += @('--clean', '--if-exists') }
        $pgArgs += @($containerDump)
        docker exec -u postgres marry-postgres pg_restore @pgArgs
        if ($LASTEXITCODE -ne 0) {
            Write-Host "" -ForegroundColor Yellow
            Write-Host "pg_restore failed. Common causes:" -ForegroundColor Yellow
            Write-Host "  * Schema already exists (use -Clean or -DropSchema)" -ForegroundColor Yellow
            Write-Host "  * Row conflicts (id collisions with the seed)" -ForegroundColor Yellow
            Write-Host "  * Field type mismatch" -ForegroundColor Yellow
            throw "pg_restore exited with code $LASTEXITCODE"
        }
    }
}
finally {
    docker exec marry-postgres rm -f $containerDump 2>$null | Out-Null
}

Write-Host ""
Write-Host "OK Import finished." -ForegroundColor Green
Write-Host "   Verify with: docker exec -it marry-postgres psql -U $DOCKER_USER -d $DOCKER_DB -c '\dt'" -ForegroundColor Green