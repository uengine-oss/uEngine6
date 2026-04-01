# 스키마 사용 코드 테스트

Oracle 스키마(`schema.sql` / `schema-definition-service.sql` / `schema-process-service.sql`)를 사용하는 레포지토리·엔티티를 H2 메모리 DB로 검증하는 테스트입니다.

## definition-service

- **테스트 클래스**: `definition-service/src/test/java/org/uengine/five/DefinitionSchemaRepositoryTest.java`
- **대상**: TB_BPM_PROCDEF, TB_BPM_PDEF_VER, TB_BPM_DEF_LOCK, TB_BPM_PDEF_CMT, TB_BPM_DEF_INFO (ProcDefRepository, ProcDefVersionRepository, DefinitionLockRepository, ProcDefCommentRepository, DefinitionEntityRepository)
- **실행 방법** (의존 모듈 포함 빌드 필요):

```bash
# 프로젝트 루트에서
mvn test -pl definition-service -am -Dtest=DefinitionSchemaRepositoryTest -DfailIfNoTests=false
```

- **설정**: `definition-service/src/test/resources/application-test.yml` (H2, `ddl-auto=create-drop`)

## process-service

- **테스트 클래스**: `process-service/src/test/java/org/uengine/test/ProcessSchemaRepositoryTest.java`
- **대상**: TB_BPM_WORKLIST, TB_BPM_AUDIT_LOG, TB_BPM_EVTMAP 등 (WorklistRepository, AuditLogEntityRepository, EventMappingRepository)
- **실행 방법**:

```bash
mvn test -pl process-service -am -Dtest=ProcessSchemaRepositoryTest -DfailIfNoTests=false
```

- **설정**: `process-service/src/test/resources/application-test.yml` (H2)

## 참고

- 테스트는 Oracle DDL 파일을 직접 실행하지 않고, **동일 엔티티 매핑**으로 H2에 테이블을 생성(`ddl-auto=create-drop`)한 뒤 CRUD를 검증합니다.
- Oracle 전용 컬럼/컨버터(OracleBooleanConverter, OracleDateTimeStringConverter)는 H2에서도 동작하도록 되어 있습니다.
