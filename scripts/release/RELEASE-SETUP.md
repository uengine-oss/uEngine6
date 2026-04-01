# release 폴더 구성 및 실행 (부산은행 배포용)

배포 폴더 예: `D:\uEngineProject\부산은행\release`  
여기에 아래 파일을 두고 **이미지 로드 → compose up** 순서로 실행합니다.

---

## 1. release 폴더에 있어야 할 파일

| 파일/폴더 | 필수 | 설명 |
|-----------|------|------|
| `keycloak-bundle-1.1.0.tar` | ✅ | Keycloak + Zookeeper + Kafka 이미지 (오프라인용 일괄 포함) |
| `uengine-release-1.1.0.tar` | ✅ | definition-service + keycloak-gateway 이미지 |
| `process-gpt-vue3-1.4.0.tar` | 선택 | 프론트엔드 (쓰는 경우만) |
| `docker-compose.yml` | ✅ | 전체 한 번에 기동용 |
| `docker-compose.keycloak.yml` | 선택 | Keycloak만 따로 기동 |
| `docker-compose.uengine.yml` | 선택 | uEngine(definition + gateway)만 |
| `docker-compose.frontend.yml` | 선택 | 프론트만 |
| `keycloak/realm-export.json` | ✅ | Keycloak uengine 리얼 (compose에서 마운트) |
| `load-images.ps1` 또는 `load-images.sh` | 선택 | tar 일괄 로드 스크립트 |

- **Kafka**: 전체 compose에 Zookeeper + Kafka가 포함되어 있으며, **keycloak-bundle tar**에 Keycloak과 함께 `uengine-zookeeper:1.1.0`, `uengine-kafka:1.1.0` 이미지가 들어 있어 오프라인에서도 로드 후 바로 사용 가능합니다. (빌드 시 Confluent 이미지 pull 후 위 태그로 저장됨)
- **버전이 다르면**: 파일명과 compose 안의 `image:` 태그를 해당 버전(예: 1.2.0)으로 맞추세요.

---

## 2. 빌드 (uEngine6 프로젝트에서)

이미지와 tar는 **uEngine6 레포**에서 만든 뒤 release 폴더로 복사합니다.

```powershell
# 프로젝트 루트(c:\uEngineProject\uEngine6)에서
.\scripts\build-release-images.ps1 1.1.0 -SaveTar
```

생성 위치: `offline-images\`  
- `uengine-release-1.1.0.tar`  
- `keycloak-bundle-1.1.0.tar`  

프론트는 별도 빌드 후 `process-gpt-vue3-1.4.0.tar` 등으로 저장해 두고, 사용 시 release 폴더에 복사합니다.

---

## 3. release 폴더로 복사할 것

| 복사 대상 | 목적지 (release 폴더) |
|-----------|------------------------|
| `offline-images\uengine-release-1.1.0.tar` | `D:\uEngineProject\부산은행\release\` |
| `offline-images\keycloak-bundle-1.1.0.tar` | 위와 동일 |
| (선택) 프론트 tar | 위와 동일 |
| `scripts\release\docker-compose.yml` | 위와 동일 |
| `scripts\release\docker-compose.keycloak.yml` | 위와 동일 |
| `scripts\release\docker-compose.uengine.yml` | 위와 동일 |
| `scripts\release\docker-compose.frontend.yml` | 위와 동일 |
| `scripts\release\keycloak\realm-export.json` | `D:\uEngineProject\부산은행\release\keycloak\realm-export.json` |
| `scripts\release\load-images.ps1` | 위와 동일 (Windows) |
| `scripts\release\load-images.sh` | 위와 동일 (Bash) |

---

## 4. 실행 순서 (release 폴더에서)

### 4-1. 이미지 로드 (최초 1회)

**PowerShell:**
```powershell
cd D:\uEngineProject\부산은행\release
.\load-images.ps1
```

**Bash:**
```bash
cd /d/uEngineProject/부산은행/release
./load-images.sh
```

또는 수동:
```powershell
docker load -i keycloak-bundle-1.1.0.tar
docker load -i uengine-release-1.1.0.tar
docker load -i process-gpt-vue3-1.4.0.tar   # 프론트 쓸 때만
```

### 4-2. 컨테이너 기동

**방법 A – 전체 한 번에**
```powershell
docker compose up -d
```

**방법 B – 단계별 (Keycloak → uEngine → 프론트)**
```powershell
docker compose -f docker-compose.keycloak.yml up -d
docker compose -f docker-compose.uengine.yml up -d
docker compose -f docker-compose.frontend.yml up -d
```

### 4-3. 브라우저 로그인 시 (Keycloak 리다이렉트)

게이트웨이가 `KEYCLOAK_CLIENT_SERVER_URL=http://keycloak:8080`으로 로그인 페이지를 **keycloak:8080**으로 보냅니다.  
PC에서 `keycloak`이라는 호스트명을 쓰려면 **hosts 파일**에 다음 한 줄을 추가하세요.

- **Windows**: `C:\Windows\System32\drivers\etc\hosts`  
  추가: `localhost keycloak`
- 그 후 브라우저에서 `http://keycloak:8080` 또는 `http://localhost:8080` 접속

hosts 수정이 어렵다면, 로그인 화면이 `http://keycloak:8080`으로 열리지 않을 수 있으므로 **localhost:8080**으로 접속해 보세요 (동일 포트로 노출되어 있으면 동작할 수 있음).

---

## 5. 포트·서비스 요약

| 서비스 | 컨테이너 이름 | 호스트 포트 |
|--------|----------------|-------------|
| keycloak | release-keycloak | 8080 |
| keycloak-gateway | release-gateway | 8088 |
| definition-service | release-definition-service | (내부만, 9093) |
| frontend | release-frontend | 5173 |
| zookeeper | release-zookeeper | (내부만) |
| kafka | release-kafka | (내부만, 컨테이너 간 kafka:29092) |

- **API/게이트웨이**: http://localhost:8088  
- **Keycloak 콘솔**: http://localhost:8080 (admin / admin)  
- **프론트**: http://localhost:5173  

---

## 6. 환경 변수 (선택, .env 또는 compose)

```env
# Keycloak 클라이언트 시크릿 (realm-export와 동일하게 두면 됨)
KEYCLOAK_CLIENT_SECRET=66LpF19OpkpgKKpWHdgiCEKisx5AXqLA

# Oracle (설치 측 DB 사용 시)
SPRING_PROFILES_ACTIVE=oracle
ORACLE_HOST=db-server
ORACLE_PORT=1521
ORACLE_USER=uengine
ORACLE_PASSWORD=비밀번호

# Kafka (외부 사용 시)
KAFKA_BOOTSTRAP_SERVERS=kafka-server:9092
```

---

## 7. 문제 해결

- **이미지 없음**: `load-images.ps1` 실행 후 다시 `docker compose up -d`.
- **Keycloak 로그인 화면 안 뜸**: hosts에 `localhost keycloak` 추가 후 `http://localhost:8080` 또는 `http://keycloak:8080` 접속.
- **리다이렉트 URI 오류**: `keycloak/realm-export.json`의 client `redirectUris`에 `http://localhost:8088/*` 포함 여부 확인.
- **프론트 이미지 태그**: compose의 `frontend.image`가 실제 로드한 태그(예: `process-gpt-vue3:1.4.0`)와 같은지 확인.

이 문서와 `README.md`를 함께 보면 release 폴더 기준 배포가 한 번에 맞춰집니다.
