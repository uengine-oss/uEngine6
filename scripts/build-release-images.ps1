# 릴리즈 버전으로 definition-service, keycloak, keycloak-gateway Docker 이미지 빌드
# 사용: .\scripts\build-release-images.ps1 [버전] [-SaveTar] [-Offline]
# 예:   .\scripts\build-release-images.ps1 1.1.0
#      .\scripts\build-release-images.ps1 1.1.0 -SaveTar   # 빌드 후 tar로 저장
#      .\scripts\build-release-images.ps1 1.1.0 -Offline   # 오프라인: Zookeeper/Kafka pull 생략

$ErrorActionPreference = "Stop"

$SaveTar = $false
$Offline = $false
$VerArgs = $args | Where-Object { $_ -notin @("-SaveTar", "-Offline") }
if ($args -contains "-SaveTar") { $SaveTar = $true }
if ($args -contains "-Offline") { $Offline = $true }
$rawVersion = if ($VerArgs.Count -gt 0) { $VerArgs[0] } else { "1.1.0" }
# docker-compose와 tar 파일명 통일: "1" 같은 단일 숫자는 1.1.0으로 취급
$ReleaseVersion = if ($rawVersion -match "^\d+\.\d+") { $rawVersion } else { "1.1.0" }
if ($rawVersion -ne $ReleaseVersion) { Write-Host "버전 '$rawVersion' -> '$ReleaseVersion' (compose와 맞춤)" -ForegroundColor Gray }
$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir
$OutDir = Join-Path $RootDir "offline-images"

Write-Host "=== Release: $ReleaseVersion (root: $RootDir) ===" -ForegroundColor Cyan

# 1) definition-service
Write-Host ""
Write-Host "--- definition-service ---" -ForegroundColor Yellow
mvn clean package -DskipTests -pl definition-service -am -q
docker build -t "definition-service:${ReleaseVersion}" -f definition-service/Dockerfile .

# 2) process-service
Write-Host ""
Write-Host "--- process-service ---" -ForegroundColor Yellow
mvn clean package -DskipTests -pl process-service -am -q
docker build -t "process-service:${ReleaseVersion}" -f process-service/Dockerfile .

# 3) keycloak-gateway
Write-Host ""
Write-Host "--- keycloak-gateway ---" -ForegroundColor Yellow
mvn clean package -DskipTests -f keycloak-gateway/pom.xml -q
docker build -t "keycloak-gateway:${ReleaseVersion}" -f keycloak-gateway/Dockerfile keycloak-gateway

# 4) keycloak (realm-export.json 포함 빌드)
Write-Host ""
Write-Host "--- keycloak (build with realm-export.json) ---" -ForegroundColor Yellow
$realmJson = Join-Path $RootDir "infra\keycloak\realm-export.json"
if (-not (Test-Path $realmJson)) { throw "realm-export.json not found at infra/keycloak/realm-export.json (current dir: $RootDir)" }
docker build -t "keycloak:${ReleaseVersion}" -f infra/keycloak/Dockerfile .

# 5) Zookeeper/Kafka 이미지 준비 (keycloak-bundle에 포함용, compose에서 uengine-zookeeper / uengine-kafka 사용)
# -Offline 시 pull 생략 (이미 docker load로 로드해 둔 경우에만 keycloak-bundle tar 포함 가능)
Write-Host ""
Write-Host "--- Zookeeper/Kafka 이미지 (keycloak-bundle용) ---" -ForegroundColor Yellow
if ($Offline) {
    Write-Host "  -Offline: pull 생략 (이미 uengine-zookeeper/uengine-kafka 로드된 경우 -SaveTar 시 keycloak-bundle에 포함)" -ForegroundColor Gray
} else {
    docker pull confluentinc/cp-zookeeper:7.5.0
    docker pull confluentinc/cp-kafka:7.5.0
    docker tag confluentinc/cp-zookeeper:7.5.0 "uengine-zookeeper:${ReleaseVersion}"
    docker tag confluentinc/cp-kafka:7.5.0 "uengine-kafka:${ReleaseVersion}"
    Write-Host "  uengine-zookeeper:${ReleaseVersion}, uengine-kafka:${ReleaseVersion} (태그 완료)"
}

# 6) Optional: save to tar (release 폴더 load-images.ps1 와 동일한 파일명)
if ($SaveTar) {
    Write-Host ""
    Write-Host "--- Save to tar ---" -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
    docker save -o "$OutDir\uengine-release-${ReleaseVersion}.tar" "definition-service:${ReleaseVersion}", "process-service:${ReleaseVersion}", "keycloak-gateway:${ReleaseVersion}"
    Write-Host "  $OutDir\uengine-release-${ReleaseVersion}.tar"
    $keycloakBundleImages = @("keycloak:${ReleaseVersion}")
    $zk = "uengine-zookeeper:${ReleaseVersion}"
    $kafka = "uengine-kafka:${ReleaseVersion}"
    $hasZk = docker image inspect $zk 2>$null; $hasKafka = docker image inspect $kafka 2>$null
    if (-not $Offline -or ($hasZk -and $hasKafka)) {
        $keycloakBundleImages += $zk, $kafka
    }
    docker save -o "$OutDir\keycloak-bundle-${ReleaseVersion}.tar" $keycloakBundleImages
    Write-Host "  $OutDir\keycloak-bundle-${ReleaseVersion}.tar (Keycloak" + $(if ($keycloakBundleImages.Count -gt 1) { " + Zookeeper + Kafka" } else { " only (-Offline 시 ZK/Kafka 없음)" }) + ")"
    Write-Host "Tar files saved to: $OutDir (copy to release folder with docker-compose)" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "tar 저장은 -SaveTar 옵션 사용 시에만 수행됩니다." -ForegroundColor Gray
    Write-Host "keycloak-bundle(Keycloak+Zookeeper+Kafka) 포함 저장: .\scripts\build-release-images.ps1 $ReleaseVersion -SaveTar" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== Done. Images ===" -ForegroundColor Green
Write-Host "REPOSITORY`tTAG`tSIZE"
@("definition-service", "process-service", "keycloak-gateway", "keycloak", "uengine-zookeeper", "uengine-kafka") | ForEach-Object {
    docker images $_ --format "{{.Repository}}\t{{.Tag}}\t{{.Size}}" 2>$null
}
