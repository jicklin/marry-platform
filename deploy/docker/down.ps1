<#
.SYNOPSIS
  Stop the marry-platform Docker stack and optionally remove volumes/images.

.PARAMETER Volumes
  Also remove the named Redis volume.

.PARAMETER Images
  Also remove the locally-built backend/frontend images.

.PARAMETER Project
  Remove orphaned containers too.

.PARAMETER Full
  Convenience: -Volumes -Images -Project.
#>
[CmdletBinding()]
param(
    [switch]$Volumes,
    [switch]$Images,
    [switch]$Project,
    [switch]$Full
)


# Detect compose command (v2 plugin or v1 standalone)
$ComposeCmd = $null
try { docker compose version | Out-Null; $ComposeCmd = 'docker compose' } catch {}
if (-not $ComposeCmd) {
    try { docker-compose version | Out-Null; $ComposeCmd = 'docker-compose' } catch {}
}if (-not $ComposeCmd) { throw "Neither `docker compose` nor `docker-compose` is available." }
$ErrorActionPreference = 'Stop'

# Force UTF-8 console output so Chinese / Unicode paths print correctly
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$ScriptDir   = Split-Path -Parent $PSCommandPath
$ComposeFile = Join-Path $ScriptDir 'docker-compose.yml'
$EnvResolved = Join-Path $ScriptDir '.env.resolved'

if ($Full) { $Volumes = $true; $Images = $true; $Project = $true }

$args = @('-f', $ComposeFile, 'down')
if ($Project) { $args += '--remove-orphans' }
if ($Volumes) { $args += '-v' }
if (Test-Path $EnvResolved) { $args = @('--env-file', $EnvResolved) + $args }

Write-Host "==> $ComposeCmd $($args -join ' ')" -ForegroundColor Cyan
$tokens = $ComposeCmd -split ' '
    $cmdParts = $tokens + $args
& $cmdParts[0] @($cmdParts[1..($cmdParts.Length-1)])
if ($LASTEXITCODE -ne 0) { throw "docker compose down failed with code $LASTEXITCODE" }

if ($Images) {
    foreach ($img in 'marry-platform-backend:local', 'marry-platform-frontend:local') {
        Write-Host "    removing $img" -ForegroundColor DarkYellow
        docker rmi $img 2>$null | Out-Null
    }
}

if ($Volumes -and -not $Project) {
    Write-Host "    removing named volume marry-redis-data" -ForegroundColor DarkYellow
    docker volume rm marry-redis-data 2>$null | Out-Null
}

Write-Host "OK Stack stopped." -ForegroundColor Green