#!/usr/bin/env bash
# 릴리즈 버전으로 definition-service, keycloak, keycloak-gateway Docker 이미지 빌드
# 사용: ./scripts/build-release-images.sh [버전]
# 예:   ./scripts/build-release-images.sh 1.1.0

set -e

RELEASE_VERSION="${1:-1.1.0}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "=== 릴리즈 버전: $RELEASE_VERSION (프로젝트 루트: $ROOT_DIR) ==="

# 1) definition-service: Maven 빌드 후 Docker 이미지 빌드
echo ""
echo "--- definition-service 빌드 ---"
mvn clean package -DskipTests -pl definition-service -am -q
docker build -t "definition-service:${RELEASE_VERSION}" -f definition-service/Dockerfile .

# 2) keycloak-gateway: Maven 빌드 후 Docker 이미지 빌드
echo ""
echo "--- keycloak-gateway 빌드 ---"
mvn clean package -DskipTests -f keycloak-gateway/pom.xml -q
docker build -t "keycloak-gateway:${RELEASE_VERSION}" -f keycloak-gateway/Dockerfile keycloak-gateway

# 3) keycloak: realm-export.json 포함 빌드
echo ""
echo "--- keycloak (build with realm-export.json) ---"
test -f "infra/keycloak/realm-export.json" || { echo "FATAL: infra/keycloak/realm-export.json not found"; exit 1; }
docker build -t "keycloak:${RELEASE_VERSION}" -f infra/keycloak/Dockerfile .

echo ""
echo "=== 빌드 완료. 이미지 목록 ==="
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" \
  definition-service \
  keycloak-gateway \
  keycloak \
  | head -10
