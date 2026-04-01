# realm-export.json 을 이미지에 포함 (기동 시 --import-realm 로 로드)
# 빌드: 프로젝트 루트에서 docker build -t keycloak:버전 -f infra/keycloak/Dockerfile .
FROM quay.io/keycloak/keycloak:18.0.1
COPY infra/keycloak/realm-export.json /opt/keycloak/data/import/realm-export.json
RUN test -s /opt/keycloak/data/import/realm-export.json || (echo "FATAL: realm-export.json missing or empty" && exit 1)
# 실행 시 command: ["start-dev", "--import-realm"] 로 기동
