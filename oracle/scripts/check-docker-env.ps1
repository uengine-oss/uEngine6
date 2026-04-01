# Docker 컨테이너 환경변수 확인 (현재 빌드/호스트 앱이 Docker 쪽에 붙을 때 비교용)
# 사용: .\check-docker-env.ps1   또는  pwsh -File check-docker-env.ps1

$ErrorActionPreference = "SilentlyContinue"
$containers = @("uengine-oracle-xe", "uengine-kafka", "uengine-zookeeper", "uengine-keycloak")

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Docker 컨테이너 환경변수 (oracle/docker-compose)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

foreach ($name in $containers) {
    $id = docker ps -q -f "name=$name" 2>$null
    if (-not $id) {
        Write-Host "`n[$name] 컨테이너 없음 또는 중지됨" -ForegroundColor Yellow
        continue
    }
    Write-Host "`n--- $name (ID: $($id.Substring(0,12))) ---" -ForegroundColor Green
    docker inspect --format '{{range $k, $v := .Config.Env}}{{println $v}}{{end}}' $id | Where-Object { $_ }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " 호스트에서 앱 실행 시 Docker에 붙을 때 사용할 값" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host @"

Oracle (uengine-oracle-xe):
  ORACLE_HOST=localhost
  ORACLE_PORT=1521
  ORACLE_USER=system
  ORACLE_PASSWORD=oracle
  (또는 APP_USER: uengine / uengine)
  JDBC URL: jdbc:oracle:thin:@localhost:1521:XE

Kafka (uengine-kafka):
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  (컨테이너 내부는 kafka:29092, 호스트에서는 localhost:9092)

Keycloak (uengine-keycloak):
  URL: http://localhost:8080
  (Admin: admin / admin)

앱 실행 예 (호스트에서 Oracle+Kafka 사용):
  set SPRING_PROFILES_ACTIVE=oracle
  set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  (ORACLE_* 기본값이 이미 localhost:1521, system/oracle)

"@ -ForegroundColor White
