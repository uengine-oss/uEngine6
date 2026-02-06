# Oracle XE (Docker)

프로젝트 Java 11 환경에 맞춘 Oracle Database XE 21c Docker 구성입니다.

## 요구 사항

- Docker, Docker Compose

## 실행

```bash
# oracle 폴더에서
docker compose up -d

# 초기 기동 후 DB 준비까지 약 2~3분 소요 (healthcheck 완료까지 대기)
docker compose ps
```

**앱(process-service, definition-service) 실행 순서:** Oracle을 먼저 띄운 뒤, `docker compose ps`로 컨테이너가 healthy 되거나 로그에 "DATABASE IS READY TO USE"가 나온 다음에 `SPRING_PROFILES_ACTIVE=oracle`로 앱을 실행하세요. Oracle이 아직 안 떴을 때 앱을 켜면 "Connection refused"가 나옵니다.

## 접속 정보

| 항목 | 값 |
|------|-----|
| 호스트 | `localhost` (컨테이너 내부에서는 서비스명 `oracle`) |
| 포트 | `1521` |
| SID | `XE` |
| 시스템 계정 | `system` / `oracle` (앱 oracle 프로필 기본값) |
| 앱 계정 | `uengine` / `uengine` (최초 1회 기동 성공 시에만 생성됨) |

## JDBC URL (Java 11 / Spring Boot)

```
jdbc:oracle:thin:@localhost:1521:XE
```

Spring Boot `application.yml` 예:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: uengine
    password: uengine
    driver-class-name: oracle.jdbc.OracleDriver
```

**의존성 (Oracle JDBC):**

```xml
<dependency>
  <groupId>com.oracle.database.jdbc</groupId>
  <artifactId>ojdbc11</artifactId>
  <version>21.9.0.0</version>
</dependency>
```

## 유용한 명령

```bash
# 로그 확인
docker compose logs -f oracle

# 중지
docker compose down

# 볼륨까지 삭제 (데이터 초기화)
docker compose down -v
```

## 문제 해결

- **ORA-01092, ORA-03113** (기동 중 인스턴스 종료): 컨테이너 공유 메모리 부족입니다. `docker-compose.yml`에 `shm_size: '2gb'`가 설정되어 있어야 합니다.
- **한 번 크래시한 뒤 계속 실패**: 초기화 중 끊겨서 볼륨이 꼬였을 수 있습니다. 볼륨을 지우고 다시 띄우세요.
  ```bash
  docker compose down -v
  docker compose up -d
  ```
  (처음 기동 시 압축 해제·초기화로 2~3분 걸릴 수 있습니다.)
- **ORA-01017 (invalid username/password)**: 크래시 후 재기동한 DB는 APP_USER(uengine)가 없을 수 있습니다. 앱은 **system** / **oracle** 로 연결하도록 설정되어 있습니다. system 비밀번호가 맞지 않으면 컨테이너에서 재설정하세요.
  ```bash
  docker exec -it uengine-oracle-xe resetPassword oracle
  ```
  (그 다음 앱에서 system / oracle 로 접속)

## 버전

- 이미지: `gvenzl/oracle-xe:21-slim` (Oracle Database 21c XE)
- 프로젝트 Java: 11 → Oracle 21c + ojdbc11 사용
