# Fails if banned proprietary / advertising identity APIs appear in source.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$patterns = @(
    "AdvertisingIdClient",
    "com.google.android.gms",
    "com.google.firebase",
    "firebase-analytics",
    "play-services"
)
$include = @("*.kt", "*.kts", "*.toml", "*.xml", "*.gradle")
$hits = @()
foreach ($pattern in $patterns) {
    $found = Get-ChildItem -Path $root -Recurse -Include $include |
        Where-Object {
            $p = $_.FullName.Replace('\', '/')
            $p -notmatch '/build/' -and $p -notmatch '/\.gradle/' -and $p -notmatch '/scripts/'
        } |
        Select-String -Pattern $pattern -SimpleMatch
    if ($found) {
        $hits += $found
    }
}
if ($hits.Count -gt 0) {
    Write-Host "Banned API / dependency references found:" -ForegroundColor Red
    $hits | ForEach-Object { Write-Host $_.Path ":" $_.LineNumber ":" $_.Line }
    exit 1
}
Write-Host "Banned API check passed."
exit 0
