#!/usr/bin/env bash
# Docker를 사용할 수 없는 환경에서 릴리즈용 Docker 빌드 컨텍스트를 하나의 압축파일로 만든다.
# 사용: ./scripts/package-release-build-context.sh [버전]

set -euo pipefail

RELEASE_VERSION="${1:-1.1.0}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${ROOT_DIR}/offline-images"
PACKAGE_NAME="uengine-release-build-context-${RELEASE_VERSION}.tgz"
PACKAGE_PATH="${OUT_DIR}/${PACKAGE_NAME}"
WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/uengine-release-build-context.${RELEASE_VERSION}.XXXXXX")"
STAGING_DIR="${WORK_DIR}/release-build-context-${RELEASE_VERSION}"

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

copy_single_file() {
  local destination_dir="$1"
  shift
  local matches=()
  shopt -s nullglob
  matches=("$@")
  shopt -u nullglob

  if [[ "${#matches[@]}" -ne 1 ]]; then
    echo "FATAL: expected exactly one file match for: $*" >&2
    exit 1
  fi

  mkdir -p "${destination_dir}"
  cp "${matches[0]}" "${destination_dir}/"
}

require_command mvn
require_command tar

mkdir -p "${OUT_DIR}"
rm -f "${PACKAGE_PATH}"
mkdir -p "${STAGING_DIR}/definition-service/target"
mkdir -p "${STAGING_DIR}/definition-service/src/main/resources"
mkdir -p "${STAGING_DIR}/process-service/target"
mkdir -p "${STAGING_DIR}/process-service/src/main/resources"
mkdir -p "${STAGING_DIR}/keycloak-gateway/target"
mkdir -p "${STAGING_DIR}/infra/keycloak"
mkdir -p "${STAGING_DIR}/manifest"

cd "${ROOT_DIR}"

echo "=== 릴리즈 빌드 컨텍스트 패키징: ${RELEASE_VERSION} (프로젝트 루트: ${ROOT_DIR}) ==="

echo ""
echo "--- definition-service Maven 빌드 ---"
mvn clean package -DskipTests -pl definition-service -am -q

echo ""
echo "--- process-service Maven 빌드 ---"
mvn clean package -DskipTests -pl process-service -am -q

echo ""
echo "--- keycloak-gateway Maven 빌드 ---"
mvn clean package -DskipTests -f keycloak-gateway/pom.xml -q

require_file "${ROOT_DIR}/definition-service/Dockerfile"
require_file "${ROOT_DIR}/definition-service/src/main/resources/application.yml"
require_file "${ROOT_DIR}/process-service/Dockerfile"
require_file "${ROOT_DIR}/process-service/src/main/resources/application.yml"
require_file "${ROOT_DIR}/keycloak-gateway/Dockerfile"
require_file "${ROOT_DIR}/infra/keycloak/Dockerfile"
require_file "${ROOT_DIR}/infra/keycloak/realm-export.json"

copy_single_file "${STAGING_DIR}/definition-service/target" "${ROOT_DIR}"/definition-service/target/definition-service-*.jar
copy_single_file "${STAGING_DIR}/process-service/target" "${ROOT_DIR}"/process-service/target/*-SNAPSHOT.jar
copy_single_file "${STAGING_DIR}/keycloak-gateway/target" "${ROOT_DIR}"/keycloak-gateway/target/gateway-*.jar

cp "${ROOT_DIR}/definition-service/Dockerfile" "${STAGING_DIR}/definition-service/Dockerfile"
cp "${ROOT_DIR}/definition-service/src/main/resources/application.yml" "${STAGING_DIR}/definition-service/src/main/resources/application.yml"
cp "${ROOT_DIR}/process-service/Dockerfile" "${STAGING_DIR}/process-service/Dockerfile"
cp "${ROOT_DIR}/process-service/src/main/resources/application.yml" "${STAGING_DIR}/process-service/src/main/resources/application.yml"
cp "${ROOT_DIR}/keycloak-gateway/Dockerfile" "${STAGING_DIR}/keycloak-gateway/Dockerfile"
cp "${ROOT_DIR}/infra/keycloak/Dockerfile" "${STAGING_DIR}/infra/keycloak/Dockerfile"
cp "${ROOT_DIR}/infra/keycloak/realm-export.json" "${STAGING_DIR}/infra/keycloak/realm-export.json"

cat > "${STAGING_DIR}/manifest/release.env" <<EOF
RELEASE_VERSION=${RELEASE_VERSION}
PACKAGE_LAYOUT=release-build-context
EOF

tar -czf "${PACKAGE_PATH}" -C "${WORK_DIR}" "$(basename "${STAGING_DIR}")"

echo ""
echo "=== 패키징 완료 ==="
echo "압축파일: ${PACKAGE_PATH}"
echo "의존 이미지 tar는 별도 스크립트로 준비하세요:"
echo "  ./scripts/package-release-build-dependencies.sh ${RELEASE_VERSION}"
