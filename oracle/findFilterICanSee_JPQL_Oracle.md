# findFilterICanSee JPQL 전환 (Oracle)

## 1. Oracle UDF DDL

- **경로**: `oracle/scripts/init.sql`
- **함수**: `REGEXP_LIKE_YN(p_text IN VARCHAR2, p_pattern IN VARCHAR2) RETURN NUMBER`
- **동작**: `REGEXP_LIKE(p_text, p_pattern)`이면 1, 아니면 0. NULL이면 0 반환.
- **적용**: Oracle 스키마(APP_USER 등)에 접속 후 스크립트 실행.

```sql
-- 실행 예 (sqlplus)
@oracle/scripts/init.sql
```

## 2. 변경 전/후 요약

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 쿼리 타입 | `nativeQuery = true` (BPM_PROCINST, 컬럼명 직접 사용) | JPQL (엔티티/필드명만 사용) |
| 정렬 | Pageable Sort가 `pi.startedDate` → Oracle에 `PI.STARTEDDATE` 전달 → ORA-00904 | JPQL `order by pi.startedDate desc` + 엔티티 매핑 → `STARTED_DATE` 정상 변환 |
| REGEXP | `REGEXP_LIKE(pi.curr_ep, :rolePattern)` (네이티브) | `function('REGEXP_LIKE_YN', pi.currEp, :rolePattern) = 1` (UDF) |
| LIKE | `'%' \|\| :defId \|\| '%'` | `CONCAT(CONCAT('%', :defId), '%')` |
| finishedDate | `pi.finished_date <= :finishedDate` | `pi.finishedDate <= :finishedDate` |
| 중복 제거 | `SELECT DISTINCT pi.*` + count 서브쿼리 | `SELECT DISTINCT pi` + `COUNT(DISTINCT pi)` countQuery |
| group by | 없음(네이티브) | 없음(distinct만 사용) |

## 3. Repository JPQL (최종)

- **파일**: `process-service/.../ProcessInstanceRepositoryOracle.java`
- **메서드**: `findFilterICanSee`
- **JPQL**: `SELECT DISTINCT pi FROM ProcessInstanceEntity pi WHERE ... ORDER BY pi.startedDate DESC`
- **패턴 조건**:  
  `(:rolePattern IS NULL AND :namePattern IS NULL) OR (... REGEXP_LIKE_YN(pi.currEp, :rolePattern) = 1) OR (... REGEXP_LIKE_YN(pi.currEp, :namePattern) = 1)`

Dialect에 `REGEXP_LIKE_YN` 등록: `process-service/.../config/Oracle12cDialectWithRegexp.java`.

## 4. 테스트 호출 예시

### REST (Oracle 프로필 기동 후)

```http
GET /instances/search/findFilterICanSee?page=0&size=20&sort=startedDate,desc
GET /instances/search/findFilterICanSee?defId=myDef&status=RUNNING&page=0&size=10
GET /instances/search/findFilterICanSee?rolePattern=manager&namePattern=test&page=0&size=20
GET /instances/search/findFilterICanSee?startedDate=2025-01-01&finishedDate=2025-12-31&page=0&size=20
```

### 필터 동작 확인

- **둘 다 null**: rolePattern/namePattern 조건 없음 → 전체 대상.
- **rolePattern만**: `function('REGEXP_LIKE_YN', pi.currEp, :rolePattern) = 1` 만 적용.
- **namePattern만**: `function('REGEXP_LIKE_YN', pi.currEp, :namePattern) = 1` 만 적용.
- **둘 다 설정**: 둘 중 하나라도 매칭되면 통과 (OR).

### 단위 테스트

- `ProcessInstanceSearchControllerTest`: Controller가 Repository에 동일 파라미터로 위임하는지 검증.
- Oracle + 실제 DB 연동 시: `@DataJpaTest` + `@ActiveProfiles("oracle")`로 Repository 쿼리 실행 및 ORA-00904 미발생 확인.

## 5. 검증 체크리스트

- [ ] Oracle에 `init.sql` 적용 후 process-service(Oracle 프로필) 기동
- [ ] `/instances/search/findFilterICanSee?sort=startedDate,desc` 호출 시 ORA-00904 없음
- [ ] rolePattern/namePattern null·비null 조합 시 필터 기대대로 동작
- [ ] defId/name/currEp/prevCurrEp/initEp like, startedDate/finishedDate, subProcess/status 등 기존 필터 동작 유지
- [ ] page/size 및 sort 변경 시 페이징·정렬 정상
