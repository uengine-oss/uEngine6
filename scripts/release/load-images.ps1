# release 폴더에서 실행: 같은 폴더의 tar 파일을 docker load
# 사용: .\load-images.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$tars = @("keycloak-bundle-1.1.0.tar", "uengine-release-1.1.0.tar", "process-gpt-vue3-1.4.0.tar")
foreach ($f in $tars) {
    if (Test-Path $f) {
        Write-Host "  load: $f"
        docker load -i $f
    } else {
        Write-Host "  skip (not found): $f"
    }
}
Write-Host "Done. Run: docker compose up -d"
