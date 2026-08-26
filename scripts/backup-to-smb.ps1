<#
.SYNOPSIS
  Backup marry-platform data (PostgreSQL + uploads) to SMB share.

.DESCRIPTION
  Two-step backup:
    1. pg_dump -> local staging D:\marry-platform-backup\pg-backup\
    2. robocopy /E /Z staging -> Z:\mayouyouschoolbackup

  Uploads are mirrored directly from D:\马呦呦上学记 to the SMB share
  (no staging step needed — robocopy /E never deletes files).

  Keeps the last 7 PostgreSQL dumps (auto-purge in staging only).
#>
[CmdletBinding()]
param(
    [string]$SmbRoot         = 'Z:\mayouyouschoolbackup',
    [string]$LocalUploads    = 'D:\马呦呦上学记',
    [string]$StagingRoot     = 'D:\marry-platform-backup',
    [int]   $RetentionDays   = 7
)

$ErrorActionPreference = 'Stop'

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$Date        = Get-Date -Format 'yyyyMMdd-HHmmss'
$LocalPgDir  = Join-Path $StagingRoot 'pg-backup'
$LocalPgDump = Join-Path $LocalPgDir "marry-platform-$Date.dump"
$SmbPgDir    = Join-Path $SmbRoot 'pg-backup'

foreach ($d in @($StagingRoot, $LocalPgDir, $SmbRoot, $SmbPgDir)) {
    if (-not (Test-Path $d)) {
        New-Item -ItemType Directory -Force -Path $d | Out-Null
    }
}

# ---- 1. PostgreSQL dump -> local staging ----
Write-Host '==> [1/3] Dumping PostgreSQL to local staging...' -ForegroundColor Cyan
docker exec marry-postgres pg_dump -U marry -d marry_platform -Fc |
    Out-File -FilePath $LocalPgDump -Encoding utf8 -NoNewline
if ($LASTEXITCODE -ne 0) {
    throw "pg_dump failed with code $LASTEXITCODE"
}
Write-Host "    local: $LocalPgDump ($((Get-Item $LocalPgDump).Length) bytes)" -ForegroundColor Green

# ---- 2. Push PG dumps -> SMB ----
Write-Host '==> [2/3] Pushing PG dumps to SMB...' -ForegroundColor Cyan
robocopy $LocalPgDir $SmbPgDir *.dump /E /Z /R:3 /W:5 /NP /NDL | Out-Null
if ($LASTEXITCODE -ge 8) {
    throw "robocopy pg -> SMB failed with code $LASTEXITCODE"
}
Write-Host "    smb: $SmbPgDir" -ForegroundColor Green

# ---- 3. Mirror uploads -> SMB directly (no staging) ----
Write-Host '==> [3/3] Mirroring uploads to SMB...' -ForegroundColor Cyan
if (Test-Path $LocalUploads) {
    robocopy $LocalUploads $SmbRoot /E /Z /R:3 /W:5 /NP /NDL | Out-Null
    if ($LASTEXITCODE -ge 8) {
        throw "robocopy uploads -> SMB failed with code $LASTEXITCODE"
    }
    Write-Host "    $LocalUploads -> $SmbRoot" -ForegroundColor Green
} else {
    Write-Host "    source missing: $LocalUploads (skipping)" -ForegroundColor Yellow
}

# ---- purge old staging PG dumps ----
Get-ChildItem $LocalPgDir -Filter 'marry-platform-*.dump' |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays) } |
    ForEach-Object {
        Write-Host "    purging: $($_.FullName)" -ForegroundColor DarkGray
        Remove-Item $_.FullName -Force
    }

Write-Host ''
Write-Host 'OK backup finished.' -ForegroundColor Green