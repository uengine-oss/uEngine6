#!/usr/bin/env bash
# Docker Oracle XE에 schema-docker.sql 적용
# 사용: ./run-schema-docker.sh
# 전제: docker compose up -d 후 Oracle 컨테이너 기동 완료 (2~3분 대기)

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONTAINER_NAME="${ORACLE_CONTAINER:-uengine-oracle-xe}"
USER="${ORACLE_USER:-system}"
PASS="${ORACLE_PASSWORD:-oracle}"
SID="${ORACLE_SID:-XE}"

echo "=== Oracle 스키마 적용 (Docker) ==="
echo "컨테이너: $CONTAINER_NAME, 사용자: $USER, SID: $SID"
echo ""

if [ "$SKIP_DOCKER_CHECK" != "1" ]; then
  if ! docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${CONTAINER_NAME}$"; then
    echo "오라클 컨테이너가 보이지 않습니다. 실행 중이면 SKIP_DOCKER_CHECK=1 ./run-schema-docker.sh 로 재시도."
    echo "수동 실행: cat $SCRIPT_DIR/schema-docker.sql | docker exec -i $CONTAINER_NAME sqlplus -s $USER/$PASS@//localhost:1521/$SID"
    exit 1
  fi
fi

echo "schema-docker.sql 실행 중..."
cat "$SCRIPT_DIR/schema-docker.sql" | docker exec -i "$CONTAINER_NAME" sqlplus -s "$USER/$PASS@//localhost:1521/$SID" || true

# sqlplus EXIT 후 종료코드 0이 아닐 수 있음
echo ""
echo "스키마 적용 요청 완료. 오류가 있다면 위 로그를 확인하세요."
