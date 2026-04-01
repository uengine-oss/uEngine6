-- ============================================================================
-- uEngine BPM Oracle 스키마 DDL (부산은행 비표준 테이블 가이드 적용)
-- ============================================================================
-- 목적    : Oracle 프로필에서 사용하는 저장 테이블을 가이드 기준으로 수동 관리
-- 원칙    : TB_ 네이밍 / PK 제약 미사용 / I0 유니크 인덱스 / NOT NULL+DEFAULT / 문자열 일시 컬럼
-- 적용    : Object Owner PAL_DB 접속 후 실행. 서비스 계정(PAL_AP 등)은 synonym/권한 부여 후 사용
-- 참고    : 부산은행 확정값 - OBJECT OWNER: PAL_DB, TABLESPACE: TS_CQI_D001 (DATA/INDEX/LOB 동일)
--
-- 서비스 분리 스키마 (동일 DDL, 서비스별 적용용):
--   - schema-definition-service.sql : definition-service 전용 테이블
--   - schema-process-service.sql    : process-service 전용 테이블
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 시퀀스 (ID 생성)
-- ---------------------------------------------------------------------------
-- PAL_DB 접속 시 현재 스키마에 생성. 타 스키마에서 실행 시: CREATE SEQUENCE PAL_DB.HIBERNATE_SEQUENCE ...
CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ---------------------------------------------------------------------------
-- 1. TB_BPM_PROCINST (ProcessInstanceEntity)
-- LOB(var_lob) 사용 → 파티션 적용. 파티션 키 started_date는 I0에 포함.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_PROCINST (
    inst_id             NUMBER(19) DEFAULT 0 NOT NULL,
    def_ver_id          VARCHAR2(255),
    def_id              VARCHAR2(255),
    def_path            VARCHAR2(255),
    def_name            VARCHAR2(255),
    started_date        VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    finished_date       VARCHAR2(14),
    due_date            VARCHAR2(14),
    def_mod_date        VARCHAR2(14),
    mod_date            VARCHAR2(14),
    status              VARCHAR2(255),
    info                VARCHAR2(255),
    name                VARCHAR2(255),
    deleted             NUMBER(1) DEFAULT 0 NOT NULL,
    adhoc               NUMBER(1) DEFAULT 0 NOT NULL,
    sub_process         NUMBER(1) DEFAULT 0 NOT NULL,
    event_handler       NUMBER(1) DEFAULT 0 NOT NULL,
    root_inst_id        NUMBER(19),
    main_inst_id        NUMBER(19),
    main_act_trc_tag    VARCHAR2(255),
    main_exec_scope     VARCHAR2(255),
    main_def_ver_id      NUMBER(19),
    archive             NUMBER(1) DEFAULT 0 NOT NULL,
    abs_trc_path        VARCHAR2(255),
    dont_return         NUMBER(1) DEFAULT 0 NOT NULL,
    ext1                VARCHAR2(255),
    ext2                VARCHAR2(255),
    ext3                VARCHAR2(255),
    ext4                VARCHAR2(255),
    ext5                VARCHAR2(255),
    init_com_cd         VARCHAR2(255),
    corr_key            VARCHAR2(255),
    init_ep             VARCHAR2(255),
    init_rs_nm          VARCHAR2(255),
    prev_curr_ep        VARCHAR2(255),
    prev_curr_rs_nm     VARCHAR2(255),
    curr_ep             VARCHAR2(255),
    curr_rs_nm          VARCHAR2(255),
    groups              VARCHAR2(2000),
    variables_path      VARCHAR2(512),
    var_lob             BLOB
) TABLESPACE TS_CQI_D001
PARTITION BY RANGE (started_date)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (var_lob) STORE AS SECUREFILE LS_TB_BPM_PROCINST_00 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_PROCINST
    ON PAL_DB.TB_BPM_PROCINST (started_date, inst_id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_PROCINST
    ON PAL_DB.TB_BPM_PROCINST (status, started_date)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I2TB_BPM_PROCINST
    ON PAL_DB.TB_BPM_PROCINST (corr_key)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 2. TB_BPM_DEF_INFO (ProcessDefinitionEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_DEF_INFO (
    id                  NUMBER(19) DEFAULT 0 NOT NULL,
    name                VARCHAR2(255),
    path                VARCHAR2(255)
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_DEF_INFO
    ON PAL_DB.TB_BPM_DEF_INFO (id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_DEF_INFO
    ON PAL_DB.TB_BPM_DEF_INFO (path)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 3. TB_BPM_PROCDEF (ProcDefEntity)
-- LOB 보유. 마스터성(정의 저장)으로 DBA 협의 시 일반 테이블 허용 가능.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_PROCDEF (
    id                  VARCHAR2(512) DEFAULT ' ' NOT NULL,
    name                VARCHAR2(255),
    path                VARCHAR2(512) DEFAULT ' ' NOT NULL,
    is_directory        NUMBER(1) DEFAULT 0 NOT NULL,
    resource_type       VARCHAR2(64),
    snapshot            CLOB,
    definition_json     CLOB,
    created_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    updated_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    created_by_name     VARCHAR2(255),
    updated_by_name     VARCHAR2(255)
) TABLESPACE TS_CQI_D001
LOB (snapshot) STORE AS SECUREFILE LS_TB_BPM_PROCDEF_00 (TABLESPACE TS_CQI_D001)
LOB (definition_json) STORE AS SECUREFILE LS_TB_BPM_PROCDEF_01 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_PROCDEF
    ON PAL_DB.TB_BPM_PROCDEF (id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_PROCDEF
    ON PAL_DB.TB_BPM_PROCDEF (path)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 4. TB_BPM_PDEF_VER (ProcDefVersionEntity)
-- 이력성 + LOB. 파티션 적용. 파티션 키 time_stamp는 I0에 포함.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_PDEF_VER (
    arcv_id             VARCHAR2(768) DEFAULT ' ' NOT NULL,
    proc_def_id         VARCHAR2(512) DEFAULT ' ' NOT NULL,
    version             VARCHAR2(64) DEFAULT ' ' NOT NULL,
    version_tag         VARCHAR2(64),
    time_stamp          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    snapshot            CLOB,
    definition_json     CLOB,
    diff_text           CLOB,
    message_text        CLOB,
    created_by_name     VARCHAR2(255),
    updated_by_name     VARCHAR2(255)
) TABLESPACE TS_CQI_D001
PARTITION BY RANGE (time_stamp)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (snapshot) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_00 (TABLESPACE TS_CQI_D001)
LOB (definition_json) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_01 (TABLESPACE TS_CQI_D001)
LOB (diff_text) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_02 (TABLESPACE TS_CQI_D001)
LOB (message_text) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_03 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_PDEF_VER
    ON PAL_DB.TB_BPM_PDEF_VER (arcv_id, time_stamp)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_PDEF_VER
    ON PAL_DB.TB_BPM_PDEF_VER (proc_def_id, version)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 5. TB_BPM_WORKLIST (WorklistEntity)
-- LOB(reason, payload) + 내역성 → 파티션 적용. 파티션 키 save_date는 I0에 포함.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_WORKLIST (
    task_id             NUMBER(19) DEFAULT 0 NOT NULL,
    inst_id             NUMBER(19) DEFAULT 0 NOT NULL,
    title               VARCHAR2(255),
    description         VARCHAR2(255),
    endpoint            VARCHAR2(255),
    role_name           VARCHAR2(255),
    ref_role_name       VARCHAR2(255),
    res_name            VARCHAR2(255),
    def_id              VARCHAR2(255),
    def_ver_id          VARCHAR2(255),
    scope               VARCHAR2(255),
    assign_type         NUMBER(10),
    def_name            VARCHAR2(255),
    trc_tag             VARCHAR2(255),
    tool                VARCHAR2(255),
    parameter           VARCHAR2(255),
    priority            NUMBER(10),
    start_date          VARCHAR2(8),
    end_date            VARCHAR2(8),
    save_date           VARCHAR2(8) DEFAULT '00000000' NOT NULL,
    due_date            VARCHAR2(8),
    status              VARCHAR2(255),
    decision            VARCHAR2(255),
    reason              CLOB,
    dispatch_option     NUMBER(10),
    dispatch_param1     VARCHAR2(255),
    prev_user_name      VARCHAR2(255),
    root_inst_id        NUMBER(19),
    read_date           VARCHAR2(8),
    act_type            VARCHAR2(255),
    abs_trc_tag         VARCHAR2(255),
    delegated           NUMBER(1) DEFAULT 0 NOT NULL,
    urget               NUMBER(1) DEFAULT 0 NOT NULL,
    exec_scope          VARCHAR2(255),
    ext1                VARCHAR2(255),
    ext2                VARCHAR2(255),
    ext3                VARCHAR2(255),
    ext4                VARCHAR2(255),
    ext5                VARCHAR2(255),
    payload             CLOB
) TABLESPACE TS_CQI_D001
PARTITION BY RANGE (save_date)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (reason) STORE AS SECUREFILE LS_TB_BPM_WORKLIST_00 (TABLESPACE TS_CQI_D001)
LOB (payload) STORE AS SECUREFILE LS_TB_BPM_WORKLIST_01 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_WORKLIST
    ON PAL_DB.TB_BPM_WORKLIST (save_date, task_id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_WORKLIST
    ON PAL_DB.TB_BPM_WORKLIST (inst_id, status, endpoint)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I2TB_BPM_WORKLIST
    ON PAL_DB.TB_BPM_WORKLIST (role_name, scope)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 6. TB_BPM_ROLEMAP (RoleMappingEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_ROLEMAP (
    role_mapping_id     NUMBER(19) DEFAULT 0 NOT NULL,
    inst_id             NUMBER(19) DEFAULT 0 NOT NULL,
    role_name           VARCHAR2(255),
    value               VARCHAR2(255),
    endpoint            VARCHAR2(255),
    res_name            VARCHAR2(255),
    group_id            VARCHAR2(255),
    assign_type         NUMBER(19),
    assign_param1       VARCHAR2(255),
    dispatch_param1     VARCHAR2(255),
    dispatch_option     NUMBER(19)
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_ROLEMAP
    ON PAL_DB.TB_BPM_ROLEMAP (role_mapping_id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_ROLEMAP
    ON PAL_DB.TB_BPM_ROLEMAP (inst_id, role_name)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 7. TB_BPM_AUDIT (AuditEntity)
-- 이력성. 월 10만 건 이상 시 파티션 검토.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_AUDIT (
    audit_id            NUMBER(19) DEFAULT 0 NOT NULL,
    full_tracing_tag     VARCHAR2(255),
    started_date        VARCHAR2(14),
    finished_date       VARCHAR2(14),
    activity_name       VARCHAR2(255),
    tracing_tag         VARCHAR2(255),
    inst_id             NUMBER(19),
    root_inst_id        NUMBER(19)
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_AUDIT
    ON PAL_DB.TB_BPM_AUDIT (audit_id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_AUDIT
    ON PAL_DB.TB_BPM_AUDIT (inst_id, started_date)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I2TB_BPM_AUDIT
    ON PAL_DB.TB_BPM_AUDIT (root_inst_id, started_date)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 8. TB_BPM_AUDIT_LOG (AuditLogEntity)
-- append-only 로그성 + CLOB. 파티션 적용. 파티션 키 occurred_at은 I0에 포함.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_AUDIT_LOG (
    id                  NUMBER(19) DEFAULT 0 NOT NULL,
    event_type          VARCHAR2(64) DEFAULT ' ' NOT NULL,
    root_inst_id        NUMBER(19) DEFAULT 0 NOT NULL,
    inst_id             NUMBER(19),
    tracing_tag         VARCHAR2(512),
    occurred_at         VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    actor               VARCHAR2(255),
    payload             CLOB
) TABLESPACE TS_CQI_D001
PARTITION BY RANGE (occurred_at)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (payload) STORE AS SECUREFILE LS_TB_BPM_AUDIT_LOG_00 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_AUDIT_LOG
    ON PAL_DB.TB_BPM_AUDIT_LOG (occurred_at, id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_AUDIT_LOG
    ON PAL_DB.TB_BPM_AUDIT_LOG (root_inst_id, occurred_at)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I2TB_BPM_AUDIT_LOG
    ON PAL_DB.TB_BPM_AUDIT_LOG (inst_id, occurred_at)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 9. TB_BPM_EVTMAP (EventMappingEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_EVTMAP (
    event_type          VARCHAR2(255) DEFAULT ' ' NOT NULL,
    definition_id       VARCHAR2(255),
    correlation_key     VARCHAR2(255),
    tracing_tag         VARCHAR2(255),
    is_start_event      NUMBER(1) DEFAULT 0 NOT NULL
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_EVTMAP
    ON PAL_DB.TB_BPM_EVTMAP (event_type)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 10. TB_BPM_PDEF_CMT (ProcDefCommentEntity)
-- 이력성. created_at 기준 파티션 검토 가능.
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_PDEF_CMT (
    id                  VARCHAR2(36) DEFAULT ' ' NOT NULL,
    proc_def_id         VARCHAR2(512) DEFAULT ' ' NOT NULL,
    element_id          VARCHAR2(255) DEFAULT ' ' NOT NULL,
    element_type        VARCHAR2(128),
    element_name        VARCHAR2(512),
    author_id           VARCHAR2(255) DEFAULT ' ' NOT NULL,
    author_name         VARCHAR2(255),
    content             VARCHAR2(4000) DEFAULT ' ' NOT NULL,
    parent_comment_id   VARCHAR2(36),
    is_resolved         NUMBER(1) DEFAULT 0 NOT NULL,
    resolved_by         VARCHAR2(255),
    resolved_at         VARCHAR2(14),
    resolve_action_text VARCHAR2(1000),
    tenant_id           VARCHAR2(255),
    created_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    updated_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_PDEF_CMT
    ON PAL_DB.TB_BPM_PDEF_CMT (id, created_at)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_PDEF_CMT
    ON PAL_DB.TB_BPM_PDEF_CMT (proc_def_id, element_id)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 11. TB_BPM_DEF_LOCK (DefinitionLockEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_DEF_LOCK (
    resource_id         VARCHAR2(512) DEFAULT ' ' NOT NULL,
    user_id             VARCHAR2(255) DEFAULT ' ' NOT NULL,
    updated_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_DEF_LOCK
    ON PAL_DB.TB_BPM_DEF_LOCK (resource_id)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 12. TB_BPM_SERVICE (ServiceEndpointEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_SERVICE (
    path                VARCHAR2(255) DEFAULT ' ' NOT NULL
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_SERVICE
    ON PAL_DB.TB_BPM_SERVICE (path)
    TABLESPACE TS_CQI_D001;

-- ---------------------------------------------------------------------------
-- 13. TB_BPM_SVC_EVT (CatchEvent element collection)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_SVC_EVT (
    service_path        VARCHAR2(255) DEFAULT ' ' NOT NULL,
    message_class       VARCHAR2(255),
    correlation_key     VARCHAR2(255),
    def_id              VARCHAR2(255)
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_SVC_EVT
    ON PAL_DB.TB_BPM_SVC_EVT (service_path, message_class, correlation_key, def_id)
    TABLESPACE TS_CQI_D001;

CREATE INDEX PAL_DB.I1TB_BPM_SVC_EVT
    ON PAL_DB.TB_BPM_SVC_EVT (def_id)
    TABLESPACE TS_CQI_D001;

-- ============================================================================
-- PAL_AP 사용자 생성 (DBA 접속 후 실행. 이미 있으면 생략 또는 제거 후 GRANT부터 실행)
-- ============================================================================
-- 비밀번호는 운영 정책에 맞게 변경 후 적용.
CREATE USER PAL_AP IDENTIFIED BY "비밀번호변경필수"
  DEFAULT TABLESPACE TS_CQI_D001;
GRANT CONNECT TO PAL_AP;

-- ============================================================================
-- PAL_AP 최소 권한 (테이블/시퀀스 생성 후 실행. PAL_DB 또는 DBA 접속 후 실행)
-- ============================================================================
-- 시퀀스: NEXTVAL/CURRVAL 사용을 위한 SELECT
GRANT SELECT ON PAL_DB.HIBERNATE_SEQUENCE TO PAL_AP;

-- 테이블: 앱 CRUD에 필요한 최소 권한
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_PROCINST  TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_DEF_INFO   TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_PROCDEF    TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_PDEF_VER   TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_WORKLIST   TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_ROLEMAP    TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_AUDIT      TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_AUDIT_LOG  TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_EVTMAP    TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_PDEF_CMT   TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_DEF_LOCK   TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_SERVICE    TO PAL_AP;
GRANT SELECT, INSERT, UPDATE, DELETE ON PAL_DB.TB_BPM_SVC_EVT    TO PAL_AP;

-- ============================================================================
-- PAL_AP Private 시노님 생성 (DBA 또는 CREATE ANY SYNONYM 권한 보유자 접속 후 실행)
-- ============================================================================
CREATE OR REPLACE SYNONYM PAL_AP.HIBERNATE_SEQUENCE FOR PAL_DB.HIBERNATE_SEQUENCE;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_PROCDEF     FOR PAL_DB.TB_BPM_PROCDEF;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_PDEF_VER    FOR PAL_DB.TB_BPM_PDEF_VER;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_DEF_INFO    FOR PAL_DB.TB_BPM_DEF_INFO;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_DEF_LOCK    FOR PAL_DB.TB_BPM_DEF_LOCK;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_PDEF_CMT    FOR PAL_DB.TB_BPM_PDEF_CMT;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_PROCINST    FOR PAL_DB.TB_BPM_PROCINST;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_WORKLIST    FOR PAL_DB.TB_BPM_WORKLIST;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_ROLEMAP     FOR PAL_DB.TB_BPM_ROLEMAP;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_AUDIT       FOR PAL_DB.TB_BPM_AUDIT;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_AUDIT_LOG   FOR PAL_DB.TB_BPM_AUDIT_LOG;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_EVTMAP      FOR PAL_DB.TB_BPM_EVTMAP;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_SERVICE     FOR PAL_DB.TB_BPM_SERVICE;
CREATE OR REPLACE SYNONYM PAL_AP.TB_BPM_SVC_EVT     FOR PAL_DB.TB_BPM_SVC_EVT;
