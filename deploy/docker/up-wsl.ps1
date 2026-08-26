<#
.SYNOPSIS
  Windows-side launcher: forwards to up.sh inside WSL so that Windows
  drive-letter paths in .env are auto-converted to /mnt/<drive>/...

.DESCRIPTION
  Picks the default WSL distro, then runs:
      wsl -d <distro> -- bash -lc "cd <repo>/deploy/docker && ./up.sh <args>"
  so Docker (running inside WSL) sees /mnt/d/... bind mounts.

.PARAMETER Distro
  WSL distro name. Defaults to whatever `wsl -l -q` reports first.

.PARAMETER UpArgs
  Forwarded verbatim to up.sh (e.g. -Reset, --rebuild).
#>
[CmdletBinding()]
param(
    [string]$Distro,
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$UpArgs
)

$ErrorActionPreference = 'Stop'

# Force UTF-8 console output so Chinese / Unicode paths print correctly
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$ScriptDir   = Split-Path -Parent $PSCommandPath
$RepoRootWin = (Resolve-Path (Join-Path $ScriptDir '..\..\..')).Path

# Convert Windows path -> WSL path so bash cd works.
$RepoRootWsl = (& wsl.exe wslpath -a "$RepoRootWin").Trim()

if (-not $Distro) {
    $list = (& wsl.exe -l -q) | Where-Object { $_ -and $_ -notmatch '^\s*$' }
    if (-not $list) { throw "No WSL distros found. Run `wsl --list --quiet` to check." }
    $Distro = $list[0].Trim()
}

$bashCmd = "cd '$RepoRootWsl/deploy/docker' && ./up.sh $($UpArgs -join ' ')"
Write-Host "==> Forwarding to WSL distro '$Distro': $bashCmd" -ForegroundColor Cyan
& wsl.exe -d $Distro -- bash -lc $bashCmd
if ($LASTEXITCODE -ne 0) { throw "up.sh inside WSL failed with code $LASTEXITCODE" }