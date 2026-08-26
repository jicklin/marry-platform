<#
.SYNOPSIS
  Bring up the marry-platform local Docker stack in one command.

.DESCRIPTION
  Designed to run from Windows PowerShell when Docker Desktop's WSL2 backend
  is in use. Windows paths in .env (e.g. D:\my-data\pg) are auto-converted
  to /mnt/d/my-data/pg and written to .env.resolved, which is passed to
  `docker compose --env-file`. The original .env stays untouched.

  If you prefer to run inside WSL directly, run `./up.sh` from a WSL terminal
  (or `.\up-wsl.ps1` from PowerShell to forward into WSL).

.PARAMETER Foreground
  Run containers in the foreground (attach logs).

.PARAMETER Rebuild
  Force a no-cache rebuild of images.

.PARAMETER Reset
  DESTRUCTIVE: deletes the pg data and uploads directories before bringing
  the stack up. Confirms interactively unless -Force is passed.

.PARAMETER Force
  Skip the confirmation prompt for -Reset.

.PARAMETER Import
  After the stack is healthy, run the legacy dump import script
  (deploy/docker/db-import/import-dump.ps1) with the given file path.

.PARAMETER LocalJar
  Force the local-jar build even if the jar is missing (rarely needed; the
  default is to auto-detect).

.PARAMETER ForceMaven
  Always use the in-container Maven build (skip the local-jar fast path).
  Default: use the local jar at marry-platform-admin/target/marry-platform-admin.jar
  if it exists; otherwise fall back to the in-container build.
#>
[CmdletBinding()]
param(
    [switch]$Foreground,
    [switch]$Rebuild,
    [switch]$Reset,
    [switch]$Force,
    [string]$Import,
    [switch]$LocalJar,
    [switch]$ForceMaven
)

$ErrorActionPreference = 'Stop'

# Force UTF-8 console output so Chinese / Unicode paths print correctly
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$ScriptDir    = Split-Path -Parent $PSCommandPath
$ComposeFile  = Join-Path $ScriptDir 'docker-compose.yml'
$EnvFile      = Join-Path $ScriptDir '.env'
$EnvExample   = Join-Path $ScriptDir '.env.example'
$EnvResolved  = Join-Path $ScriptDir '.env.resolved'

function Test-Docker {
    try { docker version | Out-Null } catch {
        throw "Docker is not installed or not on PATH. Install Docker Desktop first."
    }
    # Prefer `docker compose` (v2 plugin). Fall back to standalone `docker-compose` (v1).
    $script:ComposeCmd = $null
    try { docker compose version | Out-Null; $script:ComposeCmd = 'docker compose' } catch {}
    if (-not $script:ComposeCmd) {
        try { docker-compose version | Out-Null; $script:ComposeCmd = 'docker-compose' } catch {}
    }
    if (-not $script:ComposeCmd) {
        throw "Neither `docker compose` (v2) nor `docker-compose` (v1) is available."
    }
}

function Initialize-Env {
    if (-not (Test-Path $EnvFile)) {
        if (-not (Test-Path $EnvExample)) {
            throw ".env.example not found at $EnvExample"
        }
        Write-Host "==> Creating .env from .env.example" -ForegroundColor Cyan
        Copy-Item $EnvExample $EnvFile
        Write-Host "    Edit $EnvFile (UPLOADS_PATH / JWT_SECRET) if needed." -ForegroundColor Yellow
    } else {
        Write-Host "==> Using existing .env" -ForegroundColor DarkGray
    }
}

function ConvertTo-WslPath {
    param([string]$Path)
    if ([string]::IsNullOrEmpty($Path)) { return $Path }
    # Drive-letter form: D:\foo or D:/foo
    if ($Path -match '^([A-Za-z]):[\\/](.+)$') {
        $drive = $Matches[1].ToLower()
        $rest  = $Matches[2] -replace '\\', '/'
        return "/mnt/$drive/$rest"
    }
    # UNC long path: \\?\D:\foo
    if ($Path -match '^\\\\\?\\([A-Za-z]):[\\/](.+)$') {
        $drive = $Matches[1].ToLower()
        $rest  = $Matches[2] -replace '\\', '/'
        return "/mnt/$drive/$rest"
    }
    return $Path
}

function Resolve-HostPath {
    param(
        [string]$Raw,
        [string]$Base
    )
    $converted = ConvertTo-WslPath $Raw
    if (-not [System.IO.Path]::IsPathRooted($converted)) {
        $converted = Join-Path $Base $converted
    }
    return $converted
}

function Build-ResolvedEnv {
    $lines = Get-Content -LiteralPath $EnvFile -Encoding utf8
    $out = New-Object System.Collections.Generic.List[string]
    foreach ($line in $lines) {
        if ($line -match '^\s*([^#][^=]*)=(.*)$') {
            $key = $Matches[1].Trim()
            $val = $Matches[2].Trim().Trim('"','''')
            switch ($key) {
                'UPLOADS_PATH' {
                    $resolved = Resolve-HostPath -Raw $val -Base $ScriptDir
                    $script:UPLOADS_PATH = $resolved
                    $out.Add("$key=$resolved") | Out-Null
                }
                default { $out.Add($line) | Out-Null }
            }
        } else {
            $out.Add($line) | Out-Null
        }
    }
    Set-Content -Path $EnvResolved -Value $out -Encoding utf8

    Write-Host "==> Persistent paths" -ForegroundColor Cyan
    Write-Host "    UPLOADS_PATH = $script:UPLOADS_PATH" -ForegroundColor DarkGray
    if ($script:UPLOADS_PATH -and -not (Test-Path $script:UPLOADS_PATH)) {
        Write-Host "    creating $script:UPLOADS_PATH" -ForegroundColor DarkYellow
        New-Item -ItemType Directory -Force -Path $script:UPLOADS_PATH | Out-Null
    }
}

function Build-DbInit {
    $buildScript = Join-Path $ScriptDir 'scripts\build-db-init.ps1'
    Write-Host "==> Aggregating Flyway migrations" -ForegroundColor Cyan
    & $buildScript
}

function Reset-Data {
    $ErrorActionPreference = 'Continue'
    Write-Host "==> Reset requested. This DELETES persistent data." -ForegroundColor Red
    if (-not $Force) {
        $ok = Read-Host "    Type 'yes' to confirm deletion of pg volume + uploads"
        if ($ok -ne 'yes') { throw "Aborted by user." }
    }
    # PG data lives in a docker named volume (ext4 inside docker-desktop-data);
    # remove via `docker volume rm` so we don't touch the Windows host FS.
    foreach ($v in @('marry-pg-data', 'marry-redis-data')) {
        try {
            $null = docker volume rm $v 2>&1
        } catch {
            Write-Host "    (no existing volume $v to remove)" -ForegroundColor DarkGray
        }
    }
    # Uploads stay on the Windows host FS (user wants visibility).
    if ($script:UPLOADS_PATH -and (Test-Path $script:UPLOADS_PATH)) {
        Write-Host "    removing $script:UPLOADS_PATH" -ForegroundColor DarkYellow
        Remove-Item -Recurse -Force $script:UPLOADS_PATH
        New-Item -ItemType Directory -Force -Path $script:UPLOADS_PATH | Out-Null
    } else {
        Write-Host "    $script:UPLOADS_PATH (missing, skipped)" -ForegroundColor DarkGray
    }
}

function Start-Stack {
    $tokens = $script:ComposeCmd -split ' '

    # If -Rebuild is requested, force a clean re-build BEFORE bringing
    # services up, so the new image is the one that runs.
    # When -LocalJar is in effect, $ComposeFile points at the override file.
    # docker compose merges an override file on top of the base; both must
    # be passed as -f in order.
    if ($UseLocalJar) {
        $ComposeArgs = @('-f', (Join-Path $ScriptDir 'docker-compose.yml'), '--env-file', $EnvResolved, '-f', $ComposeFile)
    } else {
        $ComposeArgs = @('-f', $ComposeFile, '--env-file', $EnvResolved)
    }

    if ($Rebuild) {
        Write-Host "==> $script:ComposeCmd $($ComposeArgs -join ' ') build --no-cache" -ForegroundColor Cyan
        $cmdParts = $tokens + $ComposeArgs + @('build', '--no-cache')
        & $cmdParts[0] @($cmdParts[1..($cmdParts.Length-1)])
        if ($LASTEXITCODE -ne 0) { throw "compose build failed with code $LASTEXITCODE" }
    }

    Write-Host "==> $script:ComposeCmd $($ComposeArgs -join ' ') up -d --build" -ForegroundColor Cyan
    $upTail = @('up', '-d', '--build')
    if ($Foreground) { $upTail = @('up', '--build') }
    $cmdParts = $tokens + $ComposeArgs + $upTail
    & $cmdParts[0] @($cmdParts[1..($cmdParts.Length-1)])
    if ($LASTEXITCODE -ne 0) { throw "compose up failed with code $LASTEXITCODE" }
}

function Wait-Backend {
    Write-Host "==> Waiting for backend health check (this may take ~60s on first start)..." -ForegroundColor Cyan
    $deadline = (Get-Date).AddMinutes(5)
    while ((Get-Date) -lt $deadline) {
        $status = docker inspect --format '{{.State.Health.Status}}' marry-backend 2>$null
        if ($status -eq 'healthy') {
            Write-Host "==> Backend is healthy OK" -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds 3
    }
    throw "Backend did not become healthy within 5 minutes. Try: docker compose -f $ComposeFile logs backend"
}

Test-Docker
Initialize-Env
Build-ResolvedEnv
Build-DbInit
if ($Reset) { Reset-Data }

# Resolve the backend module directory: .../marry-platform/  (repo root)
$RepoRoot = Resolve-Path (Join-Path $ScriptDir '..\..')
$BackendTarget = Join-Path $RepoRoot 'marry-platform-admin\target\marry-platform-admin.jar'

# If a local jar is already on disk, the local-jar path is used automatically.
# This is the default flow for day-to-day development: run `mvn -Pprod package`
# on the host, then `up.ps1`. Docker does not re-run Maven.
# To force the slow in-container Maven build (e.g. for CI), pass `-Rebuild`
# or `-ForceMaven` (the latter always uses the in-container build, even when
# a local jar exists).
$UseLocalJar = (Test-Path $BackendTarget) -and -not $ForceMaven

if ($UseLocalJar) {
    Write-Host "==> Local jar detected: $BackendTarget" -ForegroundColor Cyan
    Write-Host "    (skipping Maven build inside Docker; pass -ForceMaven to override)" -ForegroundColor DarkGray
    $ComposeFile = Join-Path $ScriptDir 'docker-compose.local.yml'
    if (-not (Test-Path $ComposeFile)) {
        throw "Local compose file not found at $ComposeFile"
    }
} else {
    if ($LocalJar) {
        throw "Local jar not found at $BackendTarget. Run `mvn -Pprod package` first."
    }
}
Start-Stack
Wait-Backend

Write-Host ""
if ($Import) {
    $ImportScript = Join-Path $ScriptDir 'db-import\import-dump.ps1'
    if (-not (Test-Path $ImportScript)) {
        throw "import-dump.ps1 not found at $ImportScript"
    }
    Write-Host "==> Importing legacy dump: $Import" -ForegroundColor Cyan
    & $ImportScript -DumpPath $Import
}

Write-Host "OK marry-platform is up." -ForegroundColor Green
$fePort = if ($env:FRONTEND_PORT) { $env:FRONTEND_PORT } else { '5173' }
Write-Host "   Frontend : http://localhost:$fePort" -ForegroundColor Green
Write-Host "   Backend  : http://localhost:10045/api (inside the docker network)" -ForegroundColor Green
Write-Host "   Login    : admin / admin123" -ForegroundColor Green