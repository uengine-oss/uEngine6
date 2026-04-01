#!/usr/bin/env bash
# 릴리즈 빌드 컨텍스트 압축파일을 Linux에서 풀어 기존 릴리즈 흐름과 같은 이미지와 tar를 만든다.
# 사용: ./scripts/rebuild-release-images-from-package.sh [버전] [패키지경로]

set -euo pipefail

REQUESTED_VERSION="${1:-}"
DEFAULT_VERSION="${REQUESTED_VERSION:-1.1.0}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_PATH="${2:-${SCRIPT_DIR}/uengine-release-build-context-${DEFAULT_VERSION}.tgz}"
WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/uengine-release-rebuild.${DEFAULT_VERSION}.XXXXXX")"
OUTPUT_DIR="$(cd "$(dirname "${PACKAGE_PATH}")" && pwd)"

cleanup() {
  rm -rf "${WORK_DIR}"
}
trap cleanup EXIT

require_command() {
  local command_name="$1"
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "FATAL: required command not found: ${command_name}" >&2
    exit 1
  fi
}

require_file() {
  local path="$1"
  if [[ ! -f "${path}" ]]; then
    echo "FATAL: required file not found: ${path}" >&2
    exit 1
  fi
}

require_docker_image() {
  local image_name="$1"
  if ! docker image inspect "${image_name}" >/dev/null 2>&1; then
    echo "FATAL: required docker image not loaded: ${image_name}" >&2
    echo "Load uengine-release-build-dependencies-${RELEASE_VERSION}.tar before running this script." >&2
    exit 1
  fi
}

source_release_manifest() {
  local manifest_path="$1"
  local normalized_path="${WORK_DIR}/$(basename "${manifest_path}").normalized"
  tr -d '\r' < "${manifest_path}" > "${normalized_path}"
  # shellcheck disable=SC1090
  source "${normalized_path}"
}

require_command docker
require_command tar
require_command tr

ZOOKEEPER_IMAGE="confluentinc/cp-zookeeper:7.5.0"
KAFKA_IMAGE="confluentinc/cp-kafka:7.5.0"
TEMURIN_IMAGE="eclipse-temurin:11-jre"
KEYCLOAK_BASE_IMAGE="quay.io/keycloak/keycloak:18.0.1"

if [[ ! -f "${PACKAGE_PATH}" ]]; then
  echo "FATAL: package not found: ${PACKAGE_PATH}" >&2
  exit 1
fi

echo "=== 릴리즈 이미지 복원: ${PACKAGE_PATH} ==="

tar -xzf "${PACKAGE_PATH}" -C "${WORK_DIR}"

context_dirs=("${WORK_DIR}"/release-build-context-*)
if [[ "${#context_dirs[@]}" -ne 1 || ! -d "${context_dirs[0]}" ]]; then
  echo "FATAL: could not determine extracted build context directory" >&2
  exit 1
fi

CONTEXT_DIR="${context_dirs[0]}"
MANIFEST_PATH="${CONTEXT_DIR}/manifest/release.env"

require_file "${MANIFEST_PATH}"
require_file "${CONTEXT_DIR}/definition-service/Dockerfile"
require_file "${CONTEXT_DIR}/definition-service/src/main/resources/application.yml"
require_file "${CONTEXT_DIR}/process-service/Dockerfile"
require_file "${CONTEXT_DIR}/process-service/src/main/resources/application.yml"
require_file "${CONTEXT_DIR}/infra/keycloak/Dockerfile"
require_file "${CONTEXT_DIR}/infra/keycloak/realm-export.json"
require_file "${CONTEXT_DIR}/keycloak-gateway/Dockerfile"

# manifest가 CRLF여도 Linux에서 안전하게 source한다.
source_release_manifest "${MANIFEST_PATH}"
PACKAGE_VERSION="${RELEASE_VERSION:-}"
if [[ -z "${PACKAGE_VERSION}" ]]; then
  echo "FATAL: package manifest does not contain RELEASE_VERSION" >&2
  exit 1
fi

if [[ -n "${REQUESTED_VERSION}" && "${REQUESTED_VERSION}" != "${PACKAGE_VERSION}" ]]; then
  echo "FATAL: requested version '${REQUESTED_VERSION}' does not match package version '${PACKAGE_VERSION}'" >&2
  exit 1
fi

RELEASE_VERSION="${PACKAGE_VERSION}"
DEPENDENCIES_PATH="${OUTPUT_DIR}/uengine-release-build-dependencies-${RELEASE_VERSION}.tar"

if [[ -f "${DEPENDENCIES_PATH}" ]]; then
  echo ""
  echo "--- 의존 이미지 로드 ---"
  docker load -i "${DEPENDENCIES_PATH}"
fi

require_docker_image "${TEMURIN_IMAGE}"
require_docker_image "${KEYCLOAK_BASE_IMAGE}"
require_docker_image "${ZOOKEEPER_IMAGE}"
require_docker_image "${KAFKA_IMAGE}"

echo ""
echo "--- definition-service 빌드 ---"
docker build -t "definition-service:${RELEASE_VERSION}" -f "${CONTEXT_DIR}/definition-service/Dockerfile" "${CONTEXT_DIR}"

echo ""
echo "--- process-service 빌드 ---"
docker build -t "process-service:${RELEASE_VERSION}" -f "${CONTEXT_DIR}/process-service/Dockerfile" "${CONTEXT_DIR}"

echo ""
echo "--- keycloak-gateway 빌드 ---"
docker build -t "keycloak-gateway:${RELEASE_VERSION}" -f "${CONTEXT_DIR}/keycloak-gateway/Dockerfile" "${CONTEXT_DIR}/keycloak-gateway"

echo ""
echo "--- keycloak 빌드 ---"
docker build -t "keycloak:${RELEASE_VERSION}" -f "${CONTEXT_DIR}/infra/keycloak/Dockerfile" "${CONTEXT_DIR}"

echo ""
echo "--- Zookeeper/Kafka 준비 ---"
docker tag "${ZOOKEEPER_IMAGE}" "uengine-zookeeper:${RELEASE_VERSION}"
docker tag "${KAFKA_IMAGE}" "uengine-kafka:${RELEASE_VERSION}"

echo ""
echo "--- tar 저장 ---"
docker save -o "${OUTPUT_DIR}/uengine-release-${RELEASE_VERSION}.tar" \
  "definition-service:${RELEASE_VERSION}" \
  "process-service:${RELEASE_VERSION}" \
  "keycloak-gateway:${RELEASE_VERSION}"
docker save -o "${OUTPUT_DIR}/keycloak-bundle-${RELEASE_VERSION}.tar" \
  "keycloak:${RELEASE_VERSION}" \
  "uengine-zookeeper:${RELEASE_VERSION}" \
  "uengine-kafka:${RELEASE_VERSION}"

echo ""
echo "=== 완료 ==="
echo "출력 tar:"
echo "  ${OUTPUT_DIR}/uengine-release-${RELEASE_VERSION}.tar"
echo "  ${OUTPUT_DIR}/keycloak-bundle-${RELEASE_VERSION}.tar"
