#!/usr/bin/env bash
# =============================================================================
# uEngine 오프라인 Docker 이미지 로드 스크립트
# 인터넷이 없는 환경에서 실행 → tar 파일에서 이미지 로드
#
# 사전 준비: build-offline-images.sh로 생성한 tar 파일들을 같은 디렉터리에 둠
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
IMAGES_DIR="${IMAGES_DIR:-$PROJECT_ROOT/offline-images}"

echo "=============================================="
echo "uEngine 오프라인 이미지 로드"
echo "=============================================="
echo "이미지 디렉터리: $IMAGES_DIR"
echo ""

if [[ ! -f "$IMAGES_DIR/uengine-app-bundle.tar" ]]; then
  echo "오류: $IMAGES_DIR/uengine-app-bundle.tar 를 찾을 수 없습니다."
  echo "build-offline-images.sh 를 먼저 실행하세요."
  exit 1
fi

if [[ ! -f "$IMAGES_DIR/keycloak-bundle.tar" ]]; then
  echo "오류: $IMAGES_DIR/keycloak-bundle.tar 를 찾을 수 없습니다."
  exit 1
fi

echo "[1/2] uengine-app-bundle.tar 로드 중..."
docker load -i "$IMAGES_DIR/uengine-app-bundle.tar"
echo "  - process-service, definition-service, keycloak-gateway, kafka, zookeeper 로드 완료"
echo ""

echo "[2/2] keycloak-bundle.tar 로드 중..."
docker load -i "$IMAGES_DIR/keycloak-bundle.tar"
echo "  - keycloak 로드 완료"
echo ""

echo "=============================================="
echo "로드 완료. 실행 방법:"
echo "  cd infra && docker compose -f docker-compose.offline.yml up -d"
echo "=============================================="
