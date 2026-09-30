# Capture stage-3 high-fidelity screenshots at three resolutions.
# Usage: powershell -NoProfile -File tools/capture_hifi_screenshots.ps1
# Output: prototypes/high-fidelity/v1/screenshots/<1366|1440|1920>/<page>.png
# NOTE: keep this file ASCII-only. Windows PowerShell 5.1 reads .ps1 as ANSI,
# so non-ASCII text would break parsing without a BOM.
$ErrorActionPreference = 'Continue'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

$chrome = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
if (-not (Test-Path $chrome)) { $chrome = 'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe' }
if (-not (Test-Path $chrome)) { Write-Output 'ERROR: Chrome not found'; exit 1 }

$root = 'prototypes/high-fidelity/v1'
$pages = Get-ChildItem "$root/pages" -Filter *.html -Name | Sort-Object
$sizes = @(
    @{ tag = '1366'; w = 1366; h = 900 },
    @{ tag = '1440'; w = 1440; h = 900 },
    @{ tag = '1920'; w = 1920; h = 1080 }
)

$userDir = Join-Path $env:TEMP 'codex-hifi-shots'
$total = $pages.Count * $sizes.Count
$done = 0

Write-Output "START pages=$($pages.Count) sizes=$($sizes.Count) total=$total"
foreach ($s in $sizes) {
    $outDir = Join-Path $root "screenshots/$($s.tag)"
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    $outDirAbs = (Resolve-Path $outDir).Path
    foreach ($p in $pages) {
        $name = $p -replace '\.html$', '.png'
        $out = $outDirAbs + '\' + $name
        $pageAbs = (Resolve-Path (Join-Path "$root/pages" $p)).Path
        $uri = 'file:///' + ($pageAbs -replace '\\', '/')
        $sizeArg = '--window-size=' + $s.w + ',' + $s.h
        $shotArg = '--screenshot=' + $out
        $profileArg = '--user-data-dir=' + $userDir
        & $chrome '--headless=new' '--disable-gpu' '--no-first-run' '--hide-scrollbars' '--allow-file-access-from-files' $profileArg $sizeArg '--virtual-time-budget=6000' $shotArg $uri 2>$null | Out-Null
        $done++
        $ok = if (Test-Path $out) { 'OK' } else { 'MISS' }
        Write-Output "[$done/$total] $ok $($s.tag) $p"
    }
}
Write-Output "DONE captured=$done total=$total"
