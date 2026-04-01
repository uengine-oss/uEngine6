---
name: docker-oracle-sql
description: >-
  Runs Oracle SQL inside the local Docker XE container via docker exec and sqlplus.
  Use when the user asks to query Oracle with docker exec, TB_BPM_* tables, PAL_DB,
  sqlplus, or uengine-oracle-xe. Before any SELECT, follow the schema and container
  checks in this skill so queries target the correct owner (SYSTEM vs PAL_DB).
---

# Docker Oracle SQL (`docker exec`)

## When this applies

- `docker exec`와 Oracle·`sqlplus`·`TB_BPM_*`·`PAL_DB`·`uengine-oracle-xe`가 함께 언급되거나, 로컬 Docker Oracle로 테이블을 조회·검증할 때 **먼저** 이 스킬 순서를 따른다.

## 1. 우선 확인 (실행 전 필수)

1. **컨테이너**
   - 기본 이름: `uengine-oracle-xe` (`oracle/docker-compose.yml`).
   - 확인: `docker ps --format '{{.Names}}' | grep -E '^uengine-oracle-xe$'` (없으면 `cd oracle && docker compose up -d` 등으로 기동 후 재시도).

2. **스키마(오브젝트 소유자) — 여기서 실수하면 ORA-00942**
   - **Docker용 DDL** (`oracle/scripts/schema-docker.sql`): 테이블이 **`SYSTEM` 스키마**에 생성됨. 예: `TB_BPM_PROCDEF` → `SELECT ... FROM TB_BPM_PROCDEF` 또는 `SYSTEM.TB_BPM_PROCDEF`.
   - **운영/표준 DDL** (`oracle/scripts/schema.sql`): 테이블이 **`PAL_DB` 스키마**에 생성됨. 예: `PAL_DB.TB_BPM_PROCDEF`. Docker에 이 스크립트만 적용하지 않았다면 `PAL_DB.*`는 존재하지 않을 수 있음.
   - **규칙**: 로컬 Docker XE를 `schema-docker.sql`로만 깔았다면 **`SYSTEM` 쪽 이름을 우선** 사용한다. 불확실하면 `dba_tables`로 확인한다.

   ```bash
   docker exec -i uengine-oracle-xe sqlplus -s system/oracle@//localhost:1521/XE <<'EOSQL'
   SET PAGESIZE 50 LINESIZE 120
   SELECT owner, table_name FROM dba_tables WHERE table_name = 'TB_BPM_PROCDEF';
   EXIT;
   EOSQL
   ```

3. **접속 문자열 (기본값)**

   - 컨테이너 **내부**에서: `system/oracle@//localhost:1521/XE`
   - 환경변수로 덮어쓰는 경우: `oracle/scripts/run-schema-docker.sh`, `oracle/scripts/README-DOCKER-SETUP.md` 참고 (`ORACLE_CONTAINER`, `ORACLE_USER`, `ORACLE_PASSWORD`, `ORACLE_SID`).

## 2. 조회 실행 패턴

`sqlplus`에 SQL을 넘길 때는 `-i`와 heredoc(또는 파이프)을 사용한다.

**LOB 제외(요약 조회)** — 로그·확인용으로 적합:

```bash
docker exec -i uengine-oracle-xe sqlplus -s system/oracle@//localhost:1521/XE <<'EOSQL'
SET PAGESIZE 200 LINESIZE 200 FEEDBACK ON HEADING ON
SELECT id, name, path, is_directory, resource_type, created_at, updated_at
  FROM TB_BPM_PROCDEF;
EXIT;
EOSQL
```

**전체 컬럼( CLOB 포함 )** — 출력이 매우 길 수 있음:

```bash
docker exec -i uengine-oracle-xe sqlplus -s system/oracle@//localhost:1521/XE <<'EOSQL'
SET PAGESIZE 500 LINESIZE 200 LONG 4000 LONGCHUNKSIZE 4000 FEEDBACK ON HEADING ON
SELECT * FROM TB_BPM_PROCDEF;
EXIT;
EOSQL
```

**`PAL_DB`가 실제로 있을 때만** (스키마 확인 후):

```sql
SELECT * FROM PAL_DB.TB_BPM_PROCDEF;
```

## 3. 에이전트 동작 요약

- `docker exec`로 Oracle에 붙는 요청이면 **먼저** 컨테이너 가동 여부와 **테이블 owner(Docker=SYSTEM vs PAL_DB)** 를 확인한 뒤 `FROM` 절을 고른다.
- 동일 저장소의 `oracle/scripts/schema-docker.sql` vs `oracle/scripts/schema.sql` 차이를 근거로 삼는다.
- 결과가 비어 있거나 ORA-00942이면 owner/스크립트 적용 여부를 의심하고 `dba_tables`로 재확인한다.
