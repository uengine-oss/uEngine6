#!/usr/bin/env bash
set -euo pipefail

TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/rebuild-release-images-from-package.XXXXXX")"
WORKSPACE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FAKE_BIN_DIR="${TEST_ROOT}/fake-bin"
PACKAGE_SOURCE_DIR="${TEST_ROOT}/package-source/release-build-context-9.9.9"
OUTPUT_DIR="${TEST_ROOT}/output"
DOCKER_LOG="${TEST_ROOT}/docker.log"
PACKAGE_PATH="${OUTPUT_DIR}/uengine-release-build-context-9.9.9.tgz"
DEPENDENCIES_TAR_PATH="${OUTPUT_DIR}/uengine-release-build-dependencies-9.9.9.tar"
IMAGES_LOADED_MARKER="${TEST_ROOT}/images-loaded"

cleanup() {
  rm -rf "${TEST_ROOT}"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

mkdir -p \
  "${FAKE_BIN_DIR}" \
  "${PACKAGE_SOURCE_DIR}/definition-service/src/main/resources" \
  "${PACKAGE_SOURCE_DIR}/definition-service/target" \
  "${PACKAGE_SOURCE_DIR}/process-service/src/main/resources" \
  "${PACKAGE_SOURCE_DIR}/process-service/target" \
  "${PACKAGE_SOURCE_DIR}/keycloak-gateway/target" \
  "${PACKAGE_SOURCE_DIR}/infra/keycloak" \
  "${PACKAGE_SOURCE_DIR}/manifest" \
  "${OUTPUT_DIR}"

printf 'FROM eclipse-temurin:17-jre\n' > "${PACKAGE_SOURCE_DIR}/definition-service/Dockerfile"
printf 'server:\n  port: 8080\n' > "${PACKAGE_SOURCE_DIR}/definition-service/src/main/resources/application.yml"
printf 'stub definition jar\n' > "${PACKAGE_SOURCE_DIR}/definition-service/target/definition-service-9.9.9.jar"
printf 'FROM eclipse-temurin:17-jre\n' > "${PACKAGE_SOURCE_DIR}/process-service/Dockerfile"
printf 'server:\n  port: 8081\n' > "${PACKAGE_SOURCE_DIR}/process-service/src/main/resources/application.yml"
printf 'stub process jar\n' > "${PACKAGE_SOURCE_DIR}/process-service/target/process-service-9.9.9-SNAPSHOT.jar"
printf 'FROM eclipse-temurin:17-jre\n' > "${PACKAGE_SOURCE_DIR}/keycloak-gateway/Dockerfile"
printf 'stub gateway jar\n' > "${PACKAGE_SOURCE_DIR}/keycloak-gateway/target/gateway-9.9.9.jar"
printf 'FROM quay.io/keycloak/keycloak:18.0.1\n' > "${PACKAGE_SOURCE_DIR}/infra/keycloak/Dockerfile"
printf '{\n  "realm": "uengine"\n}\n' > "${PACKAGE_SOURCE_DIR}/infra/keycloak/realm-export.json"
printf 'RELEASE_VERSION=9.9.9\r\nPACKAGE_LAYOUT=release-build-context\r\n' > "${PACKAGE_SOURCE_DIR}/manifest/release.env"
printf 'placeholder package\n' > "${PACKAGE_PATH}"
printf 'placeholder dependencies\n' > "${DEPENDENCIES_TAR_PATH}"

cat > "${FAKE_BIN_DIR}/tar" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
if [[ "$1" != "-xzf" || "$3" != "-C" ]]; then
  exit 1
fi
mkdir -p "$4"
cp -R "${PACKAGE_SOURCE_DIR}" "$4/"
EOF
chmod +x "${FAKE_BIN_DIR}/tar"

cat > "${FAKE_BIN_DIR}/docker" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$*" >> "${DOCKER_LOG}"
if [[ "$1" == "load" ]]; then
  : > "${IMAGES_LOADED_MARKER}"
fi
if [[ "$1" == "image" && "$2" == "inspect" ]]; then
  [[ -f "${IMAGES_LOADED_MARKER}" ]] || exit 1
fi
if [[ "$1" == "save" ]]; then
  output_path="$3"
  : > "${output_path}"
fi
EOF
chmod +x "${FAKE_BIN_DIR}/docker"

PATH="${FAKE_BIN_DIR}:$PATH" \
PACKAGE_SOURCE_DIR="${PACKAGE_SOURCE_DIR}" \
DOCKER_LOG="${DOCKER_LOG}" \
IMAGES_LOADED_MARKER="${IMAGES_LOADED_MARKER}" \
bash "${WORKSPACE_ROOT}/offline-images/rebuild-release-images-from-package.sh" 9.9.9 "${PACKAGE_PATH}"

[[ -f "${OUTPUT_DIR}/uengine-release-9.9.9.tar" ]] || fail "uengine-release tar not created"
[[ -f "${OUTPUT_DIR}/keycloak-bundle-9.9.9.tar" ]] || fail "keycloak-bundle tar not created"

grep -Fqx "build -t definition-service:9.9.9 -f ${TEST_ROOT}/output/" "${DOCKER_LOG}" && fail "unexpected exact build log format"
grep -F "build -t process-service:9.9.9 -f " "${DOCKER_LOG}" > /dev/null || fail "process-service build not requested"
grep -F "build -t definition-service:9.9.9 -f " "${DOCKER_LOG}" > /dev/null || fail "definition-service build not requested"
grep -F "build -t keycloak-gateway:9.9.9 -f " "${DOCKER_LOG}" > /dev/null || fail "keycloak-gateway build not requested"
grep -F "build -t keycloak:9.9.9 -f " "${DOCKER_LOG}" > /dev/null || fail "keycloak build not requested"
grep -F "load -i ${DEPENDENCIES_TAR_PATH}" "${DOCKER_LOG}" > /dev/null || fail "dependencies tar not loaded"
if grep -Fq "pull " "${DOCKER_LOG}"; then
  fail "unexpected docker pull requested"
fi
grep -F "image inspect eclipse-temurin:11-jre" "${DOCKER_LOG}" > /dev/null || fail "temurin image not inspected"
grep -F "image inspect quay.io/keycloak/keycloak:18.0.1" "${DOCKER_LOG}" > /dev/null || fail "keycloak base image not inspected"
grep -F "image inspect confluentinc/cp-zookeeper:7.5.0" "${DOCKER_LOG}" > /dev/null || fail "zookeeper image not inspected"
grep -F "image inspect confluentinc/cp-kafka:7.5.0" "${DOCKER_LOG}" > /dev/null || fail "kafka image not inspected"
grep -F "tag confluentinc/cp-zookeeper:7.5.0 uengine-zookeeper:9.9.9" "${DOCKER_LOG}" > /dev/null || fail "zookeeper tag not requested"
grep -F "tag confluentinc/cp-kafka:7.5.0 uengine-kafka:9.9.9" "${DOCKER_LOG}" > /dev/null || fail "kafka tag not requested"
grep -F "save -o ${OUTPUT_DIR}/uengine-release-9.9.9.tar definition-service:9.9.9 process-service:9.9.9 keycloak-gateway:9.9.9" "${DOCKER_LOG}" > /dev/null || fail "uengine-release save missing expected images"
grep -F "save -o ${OUTPUT_DIR}/keycloak-bundle-9.9.9.tar keycloak:9.9.9 uengine-zookeeper:9.9.9 uengine-kafka:9.9.9" "${DOCKER_LOG}" > /dev/null || fail "keycloak-bundle save missing expected images"

echo "PASS"
