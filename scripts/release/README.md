# release 폴더 배포

같은 폴더에 아래 tar 파일과 이 docker-compose를 두고, **이미지 로드 후** 컨테이너만 올리는 방식입니다.

## 컴파일·빌드 (프로젝트 루트에서)

**전체 이미지 빌드 (definition-service, process-service, keycloak-gateway, keycloak + ZK/Kafka 태그):**
```powershell
.\scripts\build-release-images.ps1 1.1.0
```

**gateway만 수정 후 빠르게 재빌드:**
```powershell
mvn clean package -DskipTests -f keycloak-gateway/pom.xml -q
docker build -t keycloak-gateway:1.1.0 -f keycloak-gateway/Dockerfile keycloak-gateway
```
이후 release 폴더에서 `docker compose up -d --force-recreate keycloak-gateway` 로 컨테이너만 갱신.

**definition-service만 수정 후 재빌드:**
```powershell
mvn clean package -DskipTests -pl definition-service -am -q
docker build -t definition-service:1.1.0 -f definition-service/Dockerfile .
```

**process-service만 수정 후 재빌드:**
```powershell
mvn clean package -DskipTests -pl process-service -am -q
docker build -t process-service:1.1.0 -f process-service/Dockerfile .
```

---

## 올인원 커맨드

**빌드 (프로젝트 루트에서, tar까지 한 번에):**
```powershell
.\scripts\build-release-images.ps1 1.1.0 -SaveTar
```
→ `offline-images/uengine-release-1.1.0.tar`, `offline-images/keycloak-bundle-1.1.0.tar` 생성. **버전은 1.1.0처럼 점 포함**으로 주면 compose 이미지 태그와 동일해짐. 인자를 `1`만 주면 스크립트가 1.1.0으로 보정함.

**실행 (release 배포 폴더에서, 로드 + 전체 기동):**
```powershell
.\load-images.ps1; docker compose up -d
```
또는 Bash: `./load-images.sh && docker compose up -d`

---

**전체 체크리스트(빌드 → 복사 → 실행)**는 **[RELEASE-SETUP.md](./RELEASE-SETUP.md)**를 보세요.  
**Oracle 쪽 Docker와의 비교·정합성**은 **[README-ORACLE-COMPARE.md](./README-ORACLE-COMPARE.md)**를 보세요.

## 필요한 파일 (같은 폴더에 둠)

| 파일 | 용도 |
|------|------|
| `keycloak-bundle-1.1.0.tar` | Keycloak + Zookeeper + Kafka 이미지 |
| `uengine-release-1.1.0.tar` | definition-service, process-service, keycloak-gateway 이미지 |
| `process-gpt-vue3-1.4.0.tar` | 프론트엔드 이미지 |
| `docker-compose.yml` | 전체 한 번에 (선택) |
| `docker-compose.keycloak.yml` | 키클락만 |
| `docker-compose.uengine.yml` | uEngine(definition-service + process-service + gateway)만 |
| `docker-compose.frontend.yml` | 프론트만 |
| `keycloak/realm-export.json` | 키클락 uengine 리얼 (compose에서 마운트) |
| `load-images.sh` 또는 `load-images.ps1` | tar 로드 스크립트 (선택) |

## 실행 방식

### A) 따로 따로 올리기 / 내리기 (권장)

각각 다른 compose로 올리면 **down 해도 해당 스택만** 내려갑니다.

```bash
# 1) 키클락 먼저 (공유 네트워크 release-shared 생성)
docker compose -f docker-compose.keycloak.yml up -d

# 2) uEngine (같은 네트워크에서 keycloak:8080 으로 접속)
docker compose -f docker-compose.uengine.yml up -d

# 3) 프론트
docker compose -f docker-compose.frontend.yml up -d
```

내릴 때:
```bash
docker compose -f docker-compose.frontend.yml down   # 프론트만
docker compose -f docker-compose.uengine.yml down    # uEngine만
docker compose -f docker-compose.keycloak.yml down  # 키클락만
```

### B) 전체 한 번에

```bash
docker compose up -d
docker compose down   # 전부 내려감
```

## 실행 순서

### 1) 이미지 로드 (최초 1회)

**Bash:**
```bash
./load-images.sh
```

**PowerShell:**
```powershell
.\load-images.ps1
```

또는 수동:
```bash
docker load -i keycloak-bundle-1.1.0.tar
docker load -i uengine-release-1.1.0.tar
docker load -i process-gpt-vue3-1.4.0.tar
```

### 2) 환경 변수 (선택)

같은 폴더에 `.env` 파일로 접속 정보 설정:

```env
# Keycloak 외부 노출/브라우저 접속
KEYCLOAK_HOST_PORT=8080
KC_HOSTNAME=localhost
KC_HOSTNAME_PORT=8080
KEYCLOAK_BROWSER_AUTH_URL=http://localhost:8080
VITE_KEYCLOAK_URL=http://localhost:8080

# Gateway 외부 노출
GATEWAY_HOST_PORT=8088
GATEWAY_URI=http://localhost:8088
KEYCLOAK_REDIRECT_URI=http://localhost:8088/login/oauth2/code/keycloak

# Frontend 외부 노출
FRONTEND_HOST_PORT=5173

# 서비스 내부 URL 재정의가 필요할 때만
DEFINITION_SERVICE_URL=http://definition-service:8080
PROCESS_SERVICE_URL=http://process-service:8080
EXECUTION_SERVICE_URL=http://localhost:8000
FRONTEND_URL=http://frontend:8080
KEYCLOAK_CLIENT_SERVER_URL=http://keycloak:8080

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092

# Oracle (설치 측 DB 사용 시)
SPRING_PROFILES_ACTIVE=oracle,docker
ORACLE_HOST=host.docker.internal
ORACLE_PORT=1521
ORACLE_SERVICE=XE
ORACLE_USER=system
ORACLE_PASSWORD=oracle

# Keycloak / Mail / 기타
KEYCLOAK_CLIENT_SECRET=시크릿값
MAIL_SMTP_HOST=smtp.gmail.com
MAIL_SMTP_PORT=587
MAIL_USERNAME=계정
MAIL_PASSWORD=비밀번호
```

중첩 변수 치환 호환성 문제를 피하기 위해, 외부 URL은 각각 독립 변수로 지정합니다.

### 3) 컨테이너 기동

A 방식이면 위처럼 `-f docker-compose.keycloak.yml` 등으로 하나씩, B 방식이면 `docker compose up -d`.

## 컨테이너·포트

| 서비스 | 컨테이너 이름 | 호스트 포트 |
|--------|----------------|-------------|
| keycloak | release-keycloak | `${KEYCLOAK_HOST_PORT:-8080}` |
| keycloak-gateway | release-gateway | `${GATEWAY_HOST_PORT:-8088}` |
| definition-service | release-definition-service | (내부만) |
| process-service | release-process-service | (내부만) |
| frontend | release-frontend | `${FRONTEND_HOST_PORT:-5173}` |

## 참고

- **Keycloak 접속 주소**: 기본값은 **http://localhost:8080** 이며, 필요하면 `KEYCLOAK_BROWSER_AUTH_URL`로 개별 override할 수 있습니다.
- **Kafka**: definition-service가 필요로 함. 외부 Kafka가 없으면 `KAFKA_BOOTSTRAP_SERVERS` 연결 실패 시 서비스가 실패할 수 있음.
- **Oracle**: `ORACLE_HOST`, `ORACLE_PORT`, `ORACLE_SERVICE`, `ORACLE_USER`, `ORACLE_PASSWORD`를 각각 지정하는 방식입니다. 기본값은 현재와 동일한 `jdbc:oracle:thin:@host:port:XE` 형식으로 조합됩니다.
- 프론트 이미지 태그가 `process-gpt-vue3:1.4.0`이 아니면 `docker-compose.yml`의 `frontend.image`를 실제 태그로 수정하세요.
