<#
.SYNOPSIS
  Backup marry-platform data (PostgreSQL + uploads) to SMB share.

.DESCRIPTION
  - pg_dump from the running postgres container, written to Z:\<share>\pg-backup\
  - robocopy /MIR from D:\marry-uploads to Z:\<share>\
  - Keeps the last 7 PostgreSQL dumps (auto-purge)
  - Designed to run from a Windows scheduled task (e.g. daily at 02:00).

.PARAMETER SmbRoot
  SMB share root path. Default: Z:\椹懄鍛︿笂瀛﹁

.PARAMETER LocalUploads
  Local uploads directory (bind-mounted into the backend container).
  Default: D:\marry-uploads
#>
[CmdletBinding()]
param(
    [string]     = ''Z:\椹懄鍛︿笂瀛﹁'',
    [string] = ''D:\marry-uploads''
)

Continue = ''Stop''

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
System.Text.UTF8Encoding+UTF8EncodingSealed = [System.Text.UTF8Encoding]::new(False)

       = Get-Date -Format ''yyyyMMdd-HHmmss''
 = Join-Path  ''pg-backup''
     = Join-Path  ''marry-platform-.dump''
 =    # robocopy mirrors uploads into the share root

# ---- ensure SMB paths exist ----
foreach ( in @(, )) {
    if (-not (Test-Path )) {
        try {
            New-Item -ItemType Directory -Force -Path  | Out-Null
        } catch {
            throw ''Cannot access SMB path . Is the share mounted? ''
        }
    }
}

# ---- 1. PostgreSQL dump ----
Write-Host '==> Dumping PostgreSQL...' -ForegroundColor Cyan
docker exec marry-postgres pg_dump -U marry -d marry_platform -Fc > 
if ( -ne 0) {
    throw ''pg_dump failed with code ''
}
Write-Host ''    saved: '' -ForegroundColor Green

# Purge dumps older than 7 days
Get-ChildItem  -Filter ''marry-platform-*.dump'' |
    Where-Object { .LastWriteTime -lt (Get-Date).AddDays(-7) } |
    ForEach-Object {
        Write-Host ''    removing old: '' + .FullName -ForegroundColor DarkGray
        Remove-Item .FullName -Force
    }

# ---- 2. Uploads mirror ----
Write-Host ''==> Mirroring uploads to SMB...'' -ForegroundColor Cyan
if (-not (Test-Path )) {
    Write-Host ''    source not found:  (skipping)'' -ForegroundColor Yellow
} else {
    # /MIR = mirror (add new, update changed, delete removed)
    # /R:3 /W:5 = retry 3 times, wait 5s between
    robocopy   /MIR /R:3 /W:5 /NP /NDL
     = 
    # robocopy exit codes: 0=no change, 1=files copied, 2=extra files deleted, 3=both
    # anything > 7 is an error
    if ( -ge 8) {
        throw ''robocopy failed with code ''
    }
    Write-Host ''    mirrored:  -> '' -ForegroundColor Green
}

Write-Host '''' -ForegroundColor Green
Write-Host ''OK backup finished.'' -ForegroundColor Green
