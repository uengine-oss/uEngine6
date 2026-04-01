#!/usr/bin/env bash
set -euo pipefail

TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/package-release-build-context-normalize-lf.XXXXXX")"
WORKSPACE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEST_PROJECT="${TEST_ROOT}/project"
EXTRACT_DIR="${TEST_ROOT}/extracted"
FIXTURE_ROOT="${TEST_PROJECT}"
PACKAGE_DIR="${FIXTURE_ROOT}/offline-images"
MANIFEST_PATH="${EXTRACT_DIR}/release-build-context-9.9.9/manifest/release.env"
CLEAN_MANIFEST_PATH="${TEST_ROOT}/manifest-clean.env"
RUNNER_CMD="${TEST_ROOT}/run-test.cmd"
DEPENDENCIES_TAR_PATH="${PACKAGE_DIR}/uengine-release-build-dependencies-9.9.9.tar"
DOCKER_LOG="${TEST_ROOT}/docker.log"

cleanup() {
  rm -rf "${TEST_ROOT}"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

mkdir -p \
  "${TEST_PROJECT}/scripts" \
  "${FIXTURE_ROOT}/definition-service/target" \
  "${FIXTURE_ROOT}/definition-service/src/main/resources" \
  "${FIXTURE_ROOT}/process-service/target" \
  "${FIXTURE_ROOT}/process-service/src/main/resources" \
  "${FIXTURE_ROOT}/keycloak-gateway/target" \
  "${FIXTURE_ROOT}/infra/keycloak" \
  "${PACKAGE_DIR}" \
  "${EXTRACT_DIR}"

cp "${WORKSPACE_ROOT}/scripts/package-release-build-context.bat" "${TEST_PROJECT}/scripts/package-release-build-context.bat"

printf 'stub definition jar\n' > "${FIXTURE_ROOT}/definition-service/target/definition-service-9.9.9.jar"
printf 'stub process jar\n' > "${FIXTURE_ROOT}/process-service/target/process-service-9.9.9-SNAPSHOT.jar"
printf 'stub gateway jar\n' > "${FIXTURE_ROOT}/keycloak-gateway/target/gateway-9.9.9.jar"
printf 'FROM eclipse-temurin:17-jre\r\n' > "${FIXTURE_ROOT}/definition-service/Dockerfile"
printf 'server:\r\n  port: 8080\r\n' > "${FIXTURE_ROOT}/definition-service/src/main/resources/application.yml"
printf 'FROM eclipse-temurin:17-jre\r\n' > "${FIXTURE_ROOT}/process-service/Dockerfile"
printf 'server:\r\n  port: 8081\r\n' > "${FIXTURE_ROOT}/process-service/src/main/resources/application.yml"
printf 'FROM eclipse-temurin:17-jre\r\n' > "${FIXTURE_ROOT}/keycloak-gateway/Dockerfile"
printf 'FROM quay.io/keycloak/keycloak:18.0.1\r\n' > "${FIXTURE_ROOT}/infra/keycloak/Dockerfile"
printf '{\r\n  "realm": "uengine"\r\n}\r\n' > "${FIXTURE_ROOT}/infra/keycloak/realm-export.json"

cat > "${TEST_PROJECT}/mvn.cmd" <<'EOF'
@echo off
exit /b 0
EOF

cat > "${TEST_PROJECT}/tar.cmd" <<'EOF'
@echo off
setlocal EnableExtensions
if /I not "%~1"=="-czf" exit /b 1
if /I not "%~3"=="-C" exit /b 1
if not defined EXTRACT_DIR exit /b 1
if exist "%EXTRACT_DIR%" rmdir /s /q "%EXTRACT_DIR%"
mkdir "%EXTRACT_DIR%" || exit /b 1
xcopy /E /I /Y "%~4\%~5" "%EXTRACT_DIR%\%~5\" >nul || exit /b 1
type nul > "%~2"
exit /b 0
EOF

cat > "${TEST_PROJECT}/docker.cmd" <<'EOF'
@echo off
setlocal EnableExtensions EnableDelayedExpansion
>> "%DOCKER_LOG%" echo %*
if /I "%~1"=="save" (
  type nul > "%~3"
)
exit /b 0
EOF

cat > "${RUNNER_CMD}" <<EOF
@echo off
setlocal EnableExtensions
set "PATH=$(cygpath -w "${TEST_PROJECT}");%PATH%"
set "EXTRACT_DIR=$(cygpath -w "${EXTRACT_DIR}")"
set "DOCKER_LOG=$(cygpath -w "${DOCKER_LOG}")"
cd /d "$(cygpath -w "${TEST_PROJECT}")" || exit /b 1
call "$(cygpath -w "${TEST_PROJECT}/scripts/package-release-build-context.bat")" 9.9.9 --normalize-lf
EOF

powershell -NoProfile -Command "& { cmd.exe /d /c '$(cygpath -w "${RUNNER_CMD}")'; exit \$LASTEXITCODE }"

[[ -f "${MANIFEST_PATH}" ]] || fail "manifest not extracted"
[[ ! -f "${DEPENDENCIES_TAR_PATH}" ]] || fail "dependencies tar should not be created by Windows bat"
[[ -f "${EXTRACT_DIR}/release-build-context-9.9.9/process-service/Dockerfile" ]] || fail "process-service Dockerfile not extracted"
[[ -f "${EXTRACT_DIR}/release-build-context-9.9.9/process-service/src/main/resources/application.yml" ]] || fail "process-service application.yml not extracted"
compgen -G "${EXTRACT_DIR}/release-build-context-9.9.9/process-service/target/*" > /dev/null || fail "process-service jar not extracted"

if LC_ALL=C grep -q $'\r' "${MANIFEST_PATH}"; then
  fail "manifest/release.env still contains CRLF"
fi

sed $'1s/^\xef\xbb\xbf//' "${MANIFEST_PATH}" > "${CLEAN_MANIFEST_PATH}"
if ! grep -qx 'RELEASE_VERSION=9.9.9' "${CLEAN_MANIFEST_PATH}"; then
  sed -n 'l' "${CLEAN_MANIFEST_PATH}" >&2
  fail "unexpected release version line"
fi
if ! grep -qx 'PACKAGE_LAYOUT=release-build-context' "${CLEAN_MANIFEST_PATH}"; then
  sed -n 'l' "${CLEAN_MANIFEST_PATH}" >&2
  fail "unexpected package layout line"
fi
if [[ -f "${DOCKER_LOG}" ]] && grep -Fq "docker" "${DOCKER_LOG}"; then
  fail "windows bat should not invoke docker"
fi

echo "PASS"
