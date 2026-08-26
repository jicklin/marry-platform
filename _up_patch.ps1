$enc = [System.Text.Encoding]::UTF8
$c = [System.IO.File]::ReadAllText((Resolve-Path 'up.ps1'), $enc)
Write-Host ('len=' + $c.Length)