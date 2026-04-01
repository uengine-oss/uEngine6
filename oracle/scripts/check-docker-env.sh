#!/usr/bin/env bash
# Docker 컨테이너 환경변수 확인 (현재 빌드/호스트 앱이 Docker 쪽에 붙을 때 비교용)
# 사용: bash check-docker-env.sh

set -e
containers="uengine-oracle-xe uengine-kafka uengine-zookeeper uengine-keycloak"

echo "========================================"
echo " Docker 컨테이너 환경변수 (oracle/docker-compose)"
echo "========================================"

for name in $containers; do
  id=$(docker ps -q -f "name=$name" 2>/dev/null)
  if [ -z "$id" ]; then
    echo ""
    echo "[$name] 컨테이너 없음 또는 중지됨"
    continue
  fi
  echo ""
  echo "--- $name (ID: ${id:0:12}) ---"
  docker inspect --format '{{range $k, $v := .Config.Env}}{{println $v}}{{end}}' "$id"
done

echo ""
echo "========================================"
echo " 호스트에서 앱 실행 시 Docker에 붙을 때 사용할 값"
echo "========================================"
cat << 'EOF'

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
  export SPRING_PROFILES_ACTIVE=oracle
  export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  (ORACLE_* 기본값이 이미 localhost:1521, system/oracle)

EOF
