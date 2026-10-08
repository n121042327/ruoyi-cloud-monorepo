# Build the local images this fork needs, then tag them exactly as
# script/docker/docker-compose.yml expects.
#
# Why: docker-compose.yml ships upstream prebuilt images (ruoyi/*:2.6.2) for
# the framework services. This fork changed three modules, so those images are
# not enough:
#   ruoyi-edu      : brand new module, no upstream image at all
#   ruoyi-system   : added RemoteUserService.resetPassword / changeAccountStatus
#   ruoyi-resource : added RemoteFileService.downloadByUrl
# Running edu against the upstream system/resource images would fail at runtime
# with "method not found" on those Dubbo calls.
#
# NOTE: this file is intentionally ASCII only. Windows PowerShell 5.1 parses a
# non-ASCII .ps1 as ANSI and breaks on Chinese text - see AGENTS.md section 10.
#
# Usage (from anywhere):
#   powershell -ExecutionPolicy Bypass -File services\RuoYi-Cloud-Plus\script\docker\build-edu-images.ps1

$ErrorActionPreference = "Stop"

# script/docker -> services/RuoYi-Cloud-Plus (the maven root)
$mavenRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $mavenRoot
Write-Host ("maven root: " + $mavenRoot)

Write-Host "[1/2] mvn package (edu + system + resource + dependencies) ..."
mvn -DskipTests -pl ruoyi-modules/ruoyi-edu,ruoyi-modules/ruoyi-system,ruoyi-modules/ruoyi-resource -am package
if ($LASTEXITCODE -ne 0) { throw "mvn package failed with exit code $LASTEXITCODE" }

Write-Host "[2/2] docker build ..."
$images = @(
    @{ Dir = "ruoyi-modules/ruoyi-edu";      Tag = "ruoyi/ruoyi-edu:2.6.2" },
    @{ Dir = "ruoyi-modules/ruoyi-system";   Tag = "ruoyi/ruoyi-system:2.6.2" },
    @{ Dir = "ruoyi-modules/ruoyi-resource"; Tag = "ruoyi/ruoyi-resource:2.6.2" }
)
foreach ($item in $images) {
    Write-Host ("  building " + $item.Tag)
    docker build -t $item.Tag -f (Join-Path $item.Dir "Dockerfile") $item.Dir
    if ($LASTEXITCODE -ne 0) { throw ("docker build failed: " + $item.Tag) }
}

Write-Host "done. images:"
docker images --format "{{.Repository}}:{{.Tag}}" | Select-String "ruoyi/ruoyi-edu|ruoyi/ruoyi-system|ruoyi/ruoyi-resource"
