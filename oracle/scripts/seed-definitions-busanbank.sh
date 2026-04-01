#!/usr/bin/env bash
# definitions/부산은행 폴더의 BPMN을 definition-service API로 DB에 저장
# 사용: definition-service 기동 후 ./seed-definitions-busanbank.sh
# 환경변수: DEFINITION_SERVICE_URL (기본 http://localhost:9093)

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DEF_DIR="$PROJECT_ROOT/definitions/부산은행"
BASE_URL="${DEFINITION_SERVICE_URL:-http://localhost:9093}"

if [ ! -d "$DEF_DIR" ]; then
  echo "definitions/부산은행 폴더가 없습니다: $DEF_DIR"
  exit 1
fi

echo "=== 부산은행 정의 DB 적재 ==="
echo "API: $BASE_URL, 소스: $DEF_DIR"
echo ""

# 부산은행 폴더 생성 (API로)
echo "폴더 생성: 부산은행"
curl -s -X PUT -G "$BASE_URL/definition/raw" --data-urlencode "defPath=부산은행" \
  -H "Content-Type: application/json" \
  -d '{"definition":""}' > /dev/null || true

for f in "$DEF_DIR"/*.bpmn; do
  [ -f "$f" ] || continue
  name="$(basename "$f")"
  path="부산은행/$name"
  echo "저장 중: $path"
  # JSON 이스케이프: 내용을 JSON 한 필드로 넣기
  content=$(jq -Rs '{definition: .}' "$f" 2>/dev/null) || {
    content=$(python3 -c "import json,sys; print(json.dumps({'definition': sys.stdin.read()}))" < "$f" 2>/dev/null) || {
      echo "  (jq 또는 python3 필요. 수동으로 PUT $BASE_URL/definition/raw?defPath=$path 로 저장 가능)"
      continue
    }
  }
  curl -s -X PUT -G "$BASE_URL/definition/raw" --data-urlencode "defPath=$path" \
    -H "Content-Type: application/json" \
    -d "$content" > /dev/null && echo "  OK" || echo "  실패"
done

echo ""
echo "적재 완료."
