#!/usr/bin/env bash
# Docker build dependencies needed by rebuild-release-images-from-package.sh.
# Usage: ./scripts/package-release-build-dependencies.sh [version]

set -euo pipefail

RELEASE_VERSION="${1:-1.1.0}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${OUTPUT_DIR:-${ROOT_DIR}/offline-images}"
DEPENDENCIES_NAME="uengine-release-build-dependencies-${RELEASE_VERSION}.tar"
DEPENDENCIES_PATH="${OUT_DIR}/${DEPENDENCIES_NAME}"
TEMURIN_IMAGE="eclipse-temurin:11-jre"
KEYCLOAK_BASE_IMAGE="quay.io/keycloak/keycloak:18.0.1"
ZOOKEEPER_IMAGE="confluentinc/cp-zookeeper:7.5.0"
KAFKA_IMAGE="confluentinc/cp-kafka:7.5.0"

require_command() {
  local command_name="$1"
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "FATAL: required command not found: ${command_name}" >&2
    exit 1
  fi
}

require_command docker

mkdir -p "${OUT_DIR}"
rm -f "${DEPENDENCIES_PATH}"

echo "=== 릴리즈 빌드 의존 이미지 준비: ${RELEASE_VERSION} (출력: ${OUT_DIR}) ==="
echo ""
echo "--- 의존 이미지 pull ---"
docker pull "${TEMURIN_IMAGE}"
docker pull "${KEYCLOAK_BASE_IMAGE}"
docker pull "${ZOOKEEPER_IMAGE}"
docker pull "${KAFKA_IMAGE}"

echo ""
echo "--- 의존 이미지 tar 저장 ---"
docker save -o "${DEPENDENCIES_PATH}" \
  "${TEMURIN_IMAGE}" \
  "${KEYCLOAK_BASE_IMAGE}" \
  "${ZOOKEEPER_IMAGE}" \
  "${KAFKA_IMAGE}"

echo ""
echo "=== 완료 ==="
echo "의존 이미지 tar: ${DEPENDENCIES_PATH}"
