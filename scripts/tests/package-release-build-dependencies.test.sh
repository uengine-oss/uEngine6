#!/usr/bin/env bash
set -euo pipefail

TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/package-release-build-dependencies.XXXXXX")"
WORKSPACE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FAKE_BIN_DIR="${TEST_ROOT}/fake-bin"
OUTPUT_DIR="${TEST_ROOT}/offline-images"
DOCKER_LOG="${TEST_ROOT}/docker.log"
EXPECTED_TAR="${OUTPUT_DIR}/uengine-release-build-dependencies-9.9.9.tar"

cleanup() {
  rm -rf "${TEST_ROOT}"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

mkdir -p "${FAKE_BIN_DIR}" "${OUTPUT_DIR}"

cat > "${FAKE_BIN_DIR}/docker" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail
printf '%s\n' "$*" >> "${DOCKER_LOG}"
if [[ "$1" == "save" ]]; then
  : > "$3"
fi
EOF
chmod +x "${FAKE_BIN_DIR}/docker"

PATH="${FAKE_BIN_DIR}:$PATH" \
DOCKER_LOG="${DOCKER_LOG}" \
OUTPUT_DIR="${OUTPUT_DIR}" \
bash "${WORKSPACE_ROOT}/scripts/package-release-build-dependencies.sh" 9.9.9

[[ -f "${EXPECTED_TAR}" ]] || fail "dependency tar not created"
grep -F "pull eclipse-temurin:11-jre" "${DOCKER_LOG}" > /dev/null || fail "temurin pull missing"
grep -F "pull quay.io/keycloak/keycloak:18.0.1" "${DOCKER_LOG}" > /dev/null || fail "keycloak base pull missing"
grep -F "pull confluentinc/cp-zookeeper:7.5.0" "${DOCKER_LOG}" > /dev/null || fail "zookeeper pull missing"
grep -F "pull confluentinc/cp-kafka:7.5.0" "${DOCKER_LOG}" > /dev/null || fail "kafka pull missing"
grep -F "save -o ${EXPECTED_TAR} eclipse-temurin:11-jre quay.io/keycloak/keycloak:18.0.1 confluentinc/cp-zookeeper:7.5.0 confluentinc/cp-kafka:7.5.0" "${DOCKER_LOG}" > /dev/null || fail "docker save missing expected images"

echo "PASS"
