Set-Location D:\myprojects\marry-platform\deploy\docker
$b = [System.IO.File]::ReadAllBytes('up.ps1.bak')[0..30]
$b | ForEach-Object { Write-Host ('{0:X2}' -f $_) }