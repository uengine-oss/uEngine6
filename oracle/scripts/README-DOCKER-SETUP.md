# Docker Oracle 스키마 적용 및 부산은행 정의 적재

## 1. Oracle 컨테이너 기동

```bash
cd oracle
docker compose up -d
```

Oracle이 완전히 뜰 때까지 **2~3분** 기다립니다.

## 2. 스키마 적용

```bash
cd oracle/scripts
./run-schema-docker.sh
```

환경변수(선택): `ORACLE_CONTAINER`, `ORACLE_USER`, `ORACLE_PASSWORD`, `ORACLE_SID`  
기본값: 컨테이너 `uengine-oracle-xe`, 사용자 `system`, 비밀번호 `oracle`, SID `XE`

## 3. definition-service 기동 (Oracle 프로필)

```bash
# 프로젝트 루트에서
export SPRING_PROFILES_ACTIVE=oracle
# 필요 시
export ORACLE_HOST=localhost
export ORACLE_PORT=1521
# definition-service 실행 (예: IDE 또는)
cd definition-service && mvn spring-boot:run
```

기본 포트: **9093**

## 4. 부산은행 정의 적재

definition-service가 떠 있는 상태에서:

```bash
cd oracle/scripts
./seed-definitions-busanbank.sh
```

`definitions/부산은행` 폴더의 모든 `.bpmn` 파일이 definition API를 통해 DB에 저장됩니다.  
필요 시 `DEFINITION_SERVICE_URL=http://localhost:9093` 지정 (기본값이 이미 9093).

## 설정 참고

- Oracle 접속: `definition-service/src/main/resources/application.yml` 의 `spring.profiles.oracle` 참고.
- Docker용 DDL: `schema-docker.sql` (스키마/테이블스페이스 없음, 접속 사용자 스키마 사용).
- 운영(부산은행) DDL: `schema.sql` (PAL_DB, TS_CQI_D001).
