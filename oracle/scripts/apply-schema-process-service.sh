#!/usr/bin/env bash
# process-service 스키마를 Docker Oracle XE에 적용 (application.yml oracle 프로필 접속 정보 사용)
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ORACLE_USER="${ORACLE_USER:-system}"
ORACLE_PASSWORD="${ORACLE_PASSWORD:-oracle}"

echo "=== process-service 스키마 적용 (TB_BPM_AUDIT 등) ==="
echo "Container: uengine-oracle-xe, User: $ORACLE_USER"
docker exec -i uengine-oracle-xe sqlplus -s "$ORACLE_USER/$ORACLE_PASSWORD@//localhost:1521/XE" < "$SCRIPT_DIR/schema-process-service-docker.sql"
echo "process-service 스키마 적용 완료."
