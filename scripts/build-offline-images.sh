#!/usr/bin/env bash
# =============================================================================
# uEngine 오프라인 Docker 이미지 빌드 스크립트
# 인터넷이 있는 환경에서 실행 → tar 파일로 저장 → 오프라인 환경으로 이전
#
# 출력:
#   - uengine-app-bundle.tar  : process-service, definition-service, keycloak-gateway, kafka, zookeeper
#   - keycloak-bundle.tar     : keycloak
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="${OUTPUT_DIR:-$PROJECT_ROOT/offline-images}"

# 이미지 태그/버전
PROCESS_IMAGE="process-service:v0.0.1"
DEFINITION_IMAGE="definition-service:v0.0.1"
GATEWAY_IMAGE="keycloak-gateway:0.0.5"
ZOOKEEPER_IMAGE="confluentinc/cp-zookeeper:7.5.0"
KAFKA_IMAGE="confluentinc/cp-kafka:7.5.0"
KEYCLOAK_IMAGE="quay.io/keycloak/keycloak:18.0.1"

echo "=============================================="
echo "uEngine 오프라인 이미지 빌드"
echo "=============================================="
echo "프로젝트 루트: $PROJECT_ROOT"
echo "출력 디렉터리: $OUTPUT_DIR"
echo ""

mkdir -p "$OUTPUT_DIR"
cd "$PROJECT_ROOT"

# -----------------------------------------------------------------------------
# 1. Maven 빌드 (JAR 생성)
# -----------------------------------------------------------------------------
echo "[1/4] Maven 빌드..."
mvn clean package -DskipTests -q
echo "  - process-service, definition-service JAR 빌드 완료"

cd keycloak-gateway
mvn clean package -DskipTests -q
cd "$PROJECT_ROOT"
echo "  - keycloak-gateway JAR 빌드 완료"
echo ""

# -----------------------------------------------------------------------------
# 2. Docker 이미지 빌드 (애플리케이션)
# -----------------------------------------------------------------------------
echo "[2/4] Docker 이미지 빌드..."

docker build -t "$PROCESS_IMAGE" -f process-service/Dockerfile .
docker build -t "$DEFINITION_IMAGE" -f definition-service/Dockerfile .
docker build -t "$GATEWAY_IMAGE" -f keycloak-gateway/Dockerfile keycloak-gateway/

echo "  - process-service, definition-service, keycloak-gateway 빌드 완료"
echo ""

# -----------------------------------------------------------------------------
# 3. 외부 이미지 Pull (Kafka, Zookeeper, Keycloak)
# -----------------------------------------------------------------------------
echo "[3/4] 외부 이미지 Pull..."

docker pull "$ZOOKEEPER_IMAGE"
docker pull "$KAFKA_IMAGE"
docker pull "$KEYCLOAK_IMAGE"

echo "  - zookeeper, kafka, keycloak pull 완료"
echo ""

# -----------------------------------------------------------------------------
# 4. tar 파일로 저장
# -----------------------------------------------------------------------------
echo "[4/4] tar 파일로 저장..."

# 묶음 1: process-service, definition-service, keycloak-gateway, kafka, zookeeper
docker save -o "$OUTPUT_DIR/uengine-app-bundle.tar" \
  "$PROCESS_IMAGE" \
  "$DEFINITION_IMAGE" \
  "$GATEWAY_IMAGE" \
  "$ZOOKEEPER_IMAGE" \
  "$KAFKA_IMAGE"

echo "  - $OUTPUT_DIR/uengine-app-bundle.tar 생성 완료"

# 묶음 2: keycloak (별도)
docker save -o "$OUTPUT_DIR/keycloak-bundle.tar" "$KEYCLOAK_IMAGE"
echo "  - $OUTPUT_DIR/keycloak-bundle.tar 생성 완료"
echo ""

echo "=============================================="
echo "완료. 오프라인 환경으로 아래 파일을 복사하세요:"
echo "  - uengine-app-bundle.tar"
echo "  - keycloak-bundle.tar"
echo "  - scripts/load-offline-images.sh"
echo "  - infra/docker-compose.offline.yml (또는 docker-compose.yml)"
echo "  - infra/keycloak/realm-export.json"
echo "=============================================="
