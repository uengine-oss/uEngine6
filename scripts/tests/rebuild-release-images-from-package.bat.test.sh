#!/usr/bin/env bash
set -euo pipefail

TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/rebuild-release-images-from-package-bat.XXXXXX")"
WORKSPACE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEST_PROJECT="${TEST_ROOT}/project"
FAKE_BIN_DIR="${TEST_ROOT}/fake-bin"
PACKAGE_SOURCE_DIR="${TEST_ROOT}/package-source/release-build-context-9.9.9"
OUTPUT_DIR="${TEST_ROOT}/output"
DOCKER_LOG="${TEST_ROOT}/docker.log"
PACKAGE_PATH="${OUTPUT_DIR}/uengine-release-build-context-9.9.9.tgz"
DEPENDENCIES_TAR_PATH="${OUTPUT_DIR}/uengine-release-build-dependencies-9.9.9.tar"
IMAGES_LOADED_MARKER="${TEST_ROOT}/images-loaded"
RUNNER_CMD="${TEST_ROOT}/run-test.cmd"

cleanup() {
  rm -rf "${TEST_ROOT}"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

mkdir -p \
  "${TEST_PROJECT}/offline-images" \
  "${FAKE_BIN_DIR}" \
  "${PACKAGE_SOURCE_DIR}/definition-service/src/main/resources" \
  "${PACKAGE_SOURCE_DIR}/definition-service/target" \
  "${PACKAGE_SOURCE_DIR}/process-service/src/main/resources" \
  "${PACKAGE_SOURCE_DIR}/process-service/target" \
  "${PACKAGE_SOURCE_DIR}/keycloak-gateway/target" \
  "${PACKAGE_SOURCE_DIR}/infra/keycloak" \
  "${PACKAGE_SOURCE_DIR}/manifest" \
  "${OUTPUT_DIR}"

cp "${WORKSPACE_ROOT}/offline-images/rebuild-release-images-from-package.bat" "${TEST_PROJECT}/offline-images/rebuild-release-images-from-package.bat"

printf 'FROM eclipse-temurin:17-jre\r\n' > "${PACKAGE_SOURCE_DIR}/definition-service/Dockerfile"
printf 'server:\r\n  port: 8080\r\n' > "${PACKAGE_SOURCE_DIR}/definition-service/src/main/resources/application.yml"
printf 'stub definition jar\n' > "${PACKAGE_SOURCE_DIR}/definition-service/target/definition-service-9.9.9.jar"
printf 'FROM eclipse-temurin:17-jre\r\n' > "${PACKAGE_SOURCE_DIR}/process-service/Dockerfile"
printf 'server:\r\n  port: 8081\r\n' > "${PACKAGE_SOURCE_DIR}/process-service/src/main/resources/application.yml"
printf 'stub process jar\n' > "${PACKAGE_SOURCE_DIR}/process-service/target/process-service-9.9.9-SNAPSHOT.jar"
printf 'FROM eclipse-temurin:17-jre\r\n' > "${PACKAGE_SOURCE_DIR}/keycloak-gateway/Dockerfile"
printf 'stub gateway jar\n' > "${PACKAGE_SOURCE_DIR}/keycloak-gateway/target/gateway-9.9.9.jar"
printf 'FROM quay.io/keycloak/keycloak:18.0.1\r\n' > "${PACKAGE_SOURCE_DIR}/infra/keycloak/Dockerfile"
printf '{\r\n  "realm": "uengine"\r\n}\r\n' > "${PACKAGE_SOURCE_DIR}/infra/keycloak/realm-export.json"
printf 'RELEASE_VERSION=9.9.9\r\nPACKAGE_LAYOUT=release-build-context\r\n' > "${PACKAGE_SOURCE_DIR}/manifest/release.env"
printf 'placeholder package\n' > "${PACKAGE_PATH}"
printf 'placeholder dependencies\n' > "${DEPENDENCIES_TAR_PATH}"

cat > "${FAKE_BIN_DIR}/tar.cmd" <<'EOF'
@echo off
setlocal EnableExtensions
if /I not "%~1"=="-xzf" exit /b 1
if /I not "%~3"=="-C" exit /b 1
if not defined PACKAGE_SOURCE_DIR exit /b 1
xcopy /E /I /Y "%PACKAGE_SOURCE_DIR%" "%~4\release-build-context-9.9.9\" >nul || exit /b 1
exit /b 0
EOF

cat > "${FAKE_BIN_DIR}/docker.cmd" <<'EOF'
@echo off
setlocal EnableExtensions EnableDelayedExpansion
>> "%DOCKER_LOG%" echo %*
if /I "%~1"=="load" (
  type nul > "%IMAGES_LOADED_MARKER%"
)
if /I "%~1"=="image" if /I "%~2"=="inspect" (
  if not exist "%IMAGES_LOADED_MARKER%" exit /b 1
)
if /I "%~1"=="save" (
  type nul > "%~3"
)
exit /b 0
EOF

cat > "${RUNNER_CMD}" <<EOF
@echo off
setlocal EnableExtensions
set "PATH=$(cygpath -w "${FAKE_BIN_DIR}");%PATH%"
set "PACKAGE_SOURCE_DIR=$(cygpath -w "${PACKAGE_SOURCE_DIR}")"
set "DOCKER_LOG=$(cygpath -w "${DOCKER_LOG}")"
set "IMAGES_LOADED_MARKER=$(cygpath -w "${IMAGES_LOADED_MARKER}")"
cd /d "$(cygpath -w "${TEST_PROJECT}/offline-images")" || exit /b 1
call rebuild-release-images-from-package.bat 9.9.9 "$(cygpath -w "${PACKAGE_PATH}")"
EOF

powershell -NoProfile -Command "& { cmd.exe /d /c '$(cygpath -w "${RUNNER_CMD}")'; exit \$LASTEXITCODE }"

[[ -f "${OUTPUT_DIR}/uengine-release-9.9.9.tar" ]] || fail "uengine-release tar not created"
[[ -f "${OUTPUT_DIR}/keycloak-bundle-9.9.9.tar" ]] || fail "keycloak-bundle tar not created"
grep -F "build -t \"process-service:9.9.9\" -f " "${DOCKER_LOG}" > /dev/null || fail "process-service build not requested"
grep -F "load -i " "${DOCKER_LOG}" | grep -F "uengine-release-build-dependencies-9.9.9.tar" > /dev/null || fail "dependencies tar not loaded"
if grep -Fq "pull " "${DOCKER_LOG}"; then
  fail "unexpected docker pull requested"
fi
grep -F "image inspect \"eclipse-temurin:11-jre\"" "${DOCKER_LOG}" > /dev/null || fail "temurin image not inspected"
grep -F "image inspect \"quay.io/keycloak/keycloak:18.0.1\"" "${DOCKER_LOG}" > /dev/null || fail "keycloak base image not inspected"
grep -F "image inspect \"confluentinc/cp-zookeeper:7.5.0\"" "${DOCKER_LOG}" > /dev/null || fail "zookeeper image not inspected"
grep -F "image inspect \"confluentinc/cp-kafka:7.5.0\"" "${DOCKER_LOG}" > /dev/null || fail "kafka image not inspected"
grep -F "tag \"confluentinc/cp-zookeeper:7.5.0\" \"uengine-zookeeper:9.9.9\"" "${DOCKER_LOG}" > /dev/null || fail "zookeeper tag not requested"
grep -F "tag \"confluentinc/cp-kafka:7.5.0\" \"uengine-kafka:9.9.9\"" "${DOCKER_LOG}" > /dev/null || fail "kafka tag not requested"
grep -F "save -o " "${DOCKER_LOG}" | grep -F "uengine-release-9.9.9.tar" | grep -F "definition-service:9.9.9" | grep -F "process-service:9.9.9" | grep -F "keycloak-gateway:9.9.9" > /dev/null || fail "uengine-release save missing expected images"
grep -F "save -o " "${DOCKER_LOG}" | grep -F "keycloak-bundle-9.9.9.tar" | grep -F "keycloak:9.9.9" | grep -F "uengine-zookeeper:9.9.9" | grep -F "uengine-kafka:9.9.9" > /dev/null || fail "keycloak-bundle save missing expected images"

echo "PASS"
