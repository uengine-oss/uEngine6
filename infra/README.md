# Docker Compose (infra)

## 사전 준비: JAR 빌드

`process-service`, `definition-service`, `keycloak-gateway` 이미지는 로컬 Dockerfile로 빌드하며, 그 전에 각 모듈에서 JAR이 있어야 합니다.

```bash
# 1) 프로젝트 루트에서 (process-service, definition-service)
mvn package -pl process-service,definition-service -am -DskipTests

# 2) keycloak-gateway는 reactor에 없으므로 해당 디렉터리에서 별도 빌드
cd keycloak-gateway
mvn package -DskipTests
cd ..
```

## 실행

```bash
cd infra
docker-compose up --build
```

- `--build`: 이미지가 없거나 변경되면 자동으로 빌드합니다.
- **frontend**는 기본으로 포함되지 않습니다. (프로필 `frontend` 사용 시에만)

## frontend 포함 실행

frontend 이미지를 별도로 빌드한 뒤:

```bash
docker-compose --profile frontend up -d
```
