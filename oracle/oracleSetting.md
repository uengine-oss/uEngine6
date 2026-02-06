# Oracle 설정 가이드

이 문서는 **Oracle DB를 처음부터 구축하지 않고**, 이미 준비된 Oracle(또는 프로젝트 `oracle` 폴더 Docker)에 **로컬/앱에서 연결**할 때 필요한 정보를 정리한 것입니다.

---

## 1. 사전 요구사항

- **Docker, Docker Compose** (Oracle을 프로젝트 제공 Docker로 띄우는 경우)
- **Java 11** (앱 실행 시)
- 로컬에서 DB 접속 시: **SQL 클라이언트** (DBeaver, Oracle SQL Developer, DataGrip 등) 또는 터미널에서 `docker exec` + sqlplus

---

## 2. Oracle 서버 기동 (Docker)

Oracle을 아직 띄우지 않은 경우, 프로젝트의 `oracle` 폴더에서 Docker로 기동합니다.

```bash
cd oracle
docker compose up -d
```

- **최초 기동**: 압축 해제·초기화로 **2~3분** 소요. 로그에 `DATABASE IS READY TO USE!` 가 나올 때까지 대기.
- 상태 확인: `docker compose ps` (healthy 여부 확인)

자세한 내용은 **[README.md](README.md)** 참고.

---

## 3. 접속 정보

| 항목 | 값 |
|------|-----|
| **호스트** | `localhost` (로컬에서 접속 시). Docker 내부에서는 서비스명 `oracle` |
| **포트** | `1521` |
| **SID** | `XE` |
| **서비스 이름** (필요 시) | `XE` (SID 연결) 또는 `XEPDB1` (PDB 연결) |
| **시스템 계정** | `system` / `oracle` ← **앱·로컬 접속 기본 권장** |
| **앱 전용 계정** | `uengine` / `uengine` (최초 1회 기동 성공 시에만 생성됨. 크래시 후 재기동한 DB에는 없을 수 있음) |

---

## 4. 로컬에서 접속

### 4.1 SQL 클라이언트 (DBeaver, SQL Developer, DataGrip 등)

- **호스트:** `localhost`
- **포트:** `1521`
- **SID:** `XE` (또는 서비스 이름 `XE`)
- **사용자:** `system`
- **비밀번호:** `oracle`

연결 문자열 예:

- **SID 방식:** `localhost:1521:XE`
- **Thin URL:** `jdbc:oracle:thin:@localhost:1521:XE`

### 4.2 터미널에서 sqlplus (Docker 컨테이너 경유)

컨테이너 이름이 `uengine-oracle-xe` 인 경우:

```bash
docker exec -it uengine-oracle-xe sqlplus system/oracle
```

컨테이너 안에서 로컬 DB로 접속하므로, 호스트/포트 설정 없이 바로 접속됩니다.

---

## 5. 앱(Spring Boot)에서 Oracle 사용

### 5.1 DB 스위칭 (H2 ↔ Oracle)

- **기본( H2 )**: 별도 설정 없이 실행 → 메모리 DB
- **Oracle 사용**: **프로필 `oracle`** 로 실행

실행 예:

```bash
# process-service 또는 definition-service
SPRING_PROFILES_ACTIVE=oracle mvn spring-boot:run
```

또는 IDE에서 Active profiles에 `oracle` 지정.

### 5.2 Oracle 프로필 동작

- **URL:** `jdbc:oracle:thin:@localhost:1521:XE` (기본)
- **계정:** `system` / `oracle` (기본)
- **Dialect:** `Oracle12cDialect`
- **ddl-auto:** `update` (테이블/컬럼 자동 반영)

환경 변수로 덮어쓰기 가능:

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `ORACLE_HOST` | localhost | DB 호스트 |
| `ORACLE_PORT` | 1521 | 포트 |
| `ORACLE_USER` | system | DB 사용자 |
| `ORACLE_PASSWORD` | oracle | 비밀번호 |

### 5.3 적용 파일

- `process-service/src/main/resources/application.yml` (프로필 `oracle` 블록)
- `definition-service/src/main/resources/application.yml` (프로필 `oracle` 블록)

---

## 6. JDBC URL 정리

| 용도 | URL |
|------|-----|
| 앱 기본 (SID) | `jdbc:oracle:thin:@localhost:1521:XE` |
| PDB 연결 시 | `jdbc:oracle:thin:@localhost:1521/XEPDB1` |

드라이버: `oracle.jdbc.OracleDriver` (프로젝트에 `ojdbc11` 의존성 포함)

---

## 7. 문제 해결

| 현상 | 대응 |
|------|------|
| **Connection refused** | Oracle이 아직 기동 중이거나 꺼져 있음. `docker compose ps`, 로그에 `DATABASE IS READY TO USE!` 확인 후 앱 재실행. |
| **ORA-01092, ORA-03113** | 컨테이너 공유 메모리 부족. `oracle/docker-compose.yml`에 `shm_size: '2gb'` 확인. |
| **ORA-01017 (invalid username/password)** | 앱은 `system`/`oracle` 사용. 비밀번호 불일치 시: `docker exec -it uengine-oracle-xe resetPassword oracle` |
| **IdentityColumnSupport / identity key generation** | Dialect가 `Oracle12cDialect` 인지 확인 (oracle 프로필). |
| **크래시 후 재기동해도 불안정** | `docker compose down -v` 후 `docker compose up -d` 로 볼륨 새로 만들고 재기동. |

자세한 트러블슈팅은 **[README.md](README.md)** 참고.

---

## 8. 참고: 앱에서 쓰는 주요 테이블 (SYSTEM 스키마)

Oracle에 연결한 뒤 JPA가 생성·사용하는 테이블 예시입니다.

| 테이블명 | 설명 |
|----------|------|
| BPM_PROCINST | 프로세스 인스턴스 |
| BPM_WORKLIST | 업무 목록(할 일) |
| BPM_AUDIT | 감사 로그 |
| BPM_EVENT_MAPPING | 이벤트 매핑 |
| BPM_ROLEMAPPING | 역할 매핑 |
| BPM_SERVICE | 서비스 |
| DEF_INFO | 정의 정보 |
| SERVICE_ENDPOINT_ENTITY_EVENTS | 서비스 엔드포인트 이벤트 |

로컬에서 위 테이블을 조회·확인할 때 위 접속 정보(system/oracle, localhost:1521, SID XE)를 사용하면 됩니다.
