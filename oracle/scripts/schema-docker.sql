-- ============================================================================
-- uEngine BPM Oracle 스키마 DDL (Docker/로컬 Oracle XE용)
-- ============================================================================
-- 목적    : Docker Oracle XE(system/oracle) 또는 uengine/uengine 접속 후 실행
-- 적용    : 스키마 접두어/테이블스페이스 없음 → 접속 사용자 스키마·기본 테이블스페이스 사용
-- 참고    : application.yml oracle 프로필 - localhost:1521, system/oracle, XE
-- ============================================================================

CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE TB_BPM_PROCINST (
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
)
PARTITION BY RANGE (started_date)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (var_lob) STORE AS SECUREFILE LS_TB_BPM_PROCINST_00;

CREATE UNIQUE INDEX I0TB_BPM_PROCINST ON TB_BPM_PROCINST (started_date, inst_id);
CREATE INDEX I1TB_BPM_PROCINST ON TB_BPM_PROCINST (status, started_date);
CREATE INDEX I2TB_BPM_PROCINST ON TB_BPM_PROCINST (corr_key);

CREATE TABLE TB_BPM_DEF_INFO (
    id                  NUMBER(19) DEFAULT 0 NOT NULL,
    name                VARCHAR2(255),
    path                VARCHAR2(255)
);
CREATE UNIQUE INDEX I0TB_BPM_DEF_INFO ON TB_BPM_DEF_INFO (id);
CREATE INDEX I1TB_BPM_DEF_INFO ON TB_BPM_DEF_INFO (path);

CREATE TABLE TB_BPM_PROCDEF (
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
)
LOB (snapshot) STORE AS SECUREFILE LS_TB_BPM_PROCDEF_00
LOB (definition_json) STORE AS SECUREFILE LS_TB_BPM_PROCDEF_01;

CREATE UNIQUE INDEX I0TB_BPM_PROCDEF ON TB_BPM_PROCDEF (id);
CREATE INDEX I1TB_BPM_PROCDEF ON TB_BPM_PROCDEF (path);

CREATE TABLE TB_BPM_PDEF_VER (
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
)
PARTITION BY RANGE (time_stamp)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (snapshot) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_00
LOB (definition_json) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_01
LOB (diff_text) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_02
LOB (message_text) STORE AS SECUREFILE LS_TB_BPM_PDEF_VER_03;

CREATE UNIQUE INDEX I0TB_BPM_PDEF_VER ON TB_BPM_PDEF_VER (arcv_id, time_stamp);
CREATE INDEX I1TB_BPM_PDEF_VER ON TB_BPM_PDEF_VER (proc_def_id, version);

CREATE TABLE TB_BPM_WORKLIST (
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
)
PARTITION BY RANGE (save_date)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (reason) STORE AS SECUREFILE LS_TB_BPM_WORKLIST_00
LOB (payload) STORE AS SECUREFILE LS_TB_BPM_WORKLIST_01;

CREATE UNIQUE INDEX I0TB_BPM_WORKLIST ON TB_BPM_WORKLIST (save_date, task_id);
CREATE INDEX I1TB_BPM_WORKLIST ON TB_BPM_WORKLIST (inst_id, status, endpoint);
CREATE INDEX I2TB_BPM_WORKLIST ON TB_BPM_WORKLIST (role_name, scope);

CREATE TABLE TB_BPM_ROLEMAP (
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
);
CREATE UNIQUE INDEX I0TB_BPM_ROLEMAP ON TB_BPM_ROLEMAP (role_mapping_id);
CREATE INDEX I1TB_BPM_ROLEMAP ON TB_BPM_ROLEMAP (inst_id, role_name);

CREATE TABLE TB_BPM_AUDIT (
    audit_id            NUMBER(19) DEFAULT 0 NOT NULL,
    full_tracing_tag     VARCHAR2(255),
    started_date        VARCHAR2(14),
    finished_date       VARCHAR2(14),
    activity_name       VARCHAR2(255),
    tracing_tag         VARCHAR2(255),
    inst_id             NUMBER(19),
    root_inst_id        NUMBER(19)
);
CREATE UNIQUE INDEX I0TB_BPM_AUDIT ON TB_BPM_AUDIT (audit_id);
CREATE INDEX I1TB_BPM_AUDIT ON TB_BPM_AUDIT (inst_id, started_date);
CREATE INDEX I2TB_BPM_AUDIT ON TB_BPM_AUDIT (root_inst_id, started_date);

CREATE TABLE TB_BPM_AUDIT_LOG (
    id                  NUMBER(19) DEFAULT 0 NOT NULL,
    event_type          VARCHAR2(64) DEFAULT ' ' NOT NULL,
    root_inst_id        NUMBER(19) DEFAULT 0 NOT NULL,
    inst_id             NUMBER(19),
    tracing_tag         VARCHAR2(512),
    occurred_at         VARCHAR2(14) DEFAULT '00000000000000' NOT NULL,
    actor               VARCHAR2(255),
    payload             CLOB
)
PARTITION BY RANGE (occurred_at)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (payload) STORE AS SECUREFILE LS_TB_BPM_AUDIT_LOG_00;

CREATE UNIQUE INDEX I0TB_BPM_AUDIT_LOG ON TB_BPM_AUDIT_LOG (occurred_at, id);
CREATE INDEX I1TB_BPM_AUDIT_LOG ON TB_BPM_AUDIT_LOG (root_inst_id, occurred_at);
CREATE INDEX I2TB_BPM_AUDIT_LOG ON TB_BPM_AUDIT_LOG (inst_id, occurred_at);

CREATE TABLE TB_BPM_EVTMAP (
    event_type          VARCHAR2(255) DEFAULT ' ' NOT NULL,
    definition_id       VARCHAR2(255),
    correlation_key     VARCHAR2(255),
    tracing_tag         VARCHAR2(255),
    is_start_event      NUMBER(1) DEFAULT 0 NOT NULL
);
CREATE UNIQUE INDEX I0TB_BPM_EVTMAP ON TB_BPM_EVTMAP (event_type);

CREATE TABLE TB_BPM_PDEF_CMT (
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
);
CREATE UNIQUE INDEX I0TB_BPM_PDEF_CMT ON TB_BPM_PDEF_CMT (id, created_at);
CREATE INDEX I1TB_BPM_PDEF_CMT ON TB_BPM_PDEF_CMT (proc_def_id, element_id);

CREATE TABLE TB_BPM_DEF_LOCK (
    resource_id         VARCHAR2(512) DEFAULT ' ' NOT NULL,
    user_id             VARCHAR2(255) DEFAULT ' ' NOT NULL,
    updated_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL
);
CREATE UNIQUE INDEX I0TB_BPM_DEF_LOCK ON TB_BPM_DEF_LOCK (resource_id);

CREATE TABLE TB_BPM_SERVICE (
    path                VARCHAR2(255) DEFAULT ' ' NOT NULL
);
CREATE UNIQUE INDEX I0TB_BPM_SERVICE ON TB_BPM_SERVICE (path);

CREATE TABLE TB_BPM_SVC_EVT (
    service_path        VARCHAR2(255) DEFAULT ' ' NOT NULL,
    message_class       VARCHAR2(255),
    correlation_key     VARCHAR2(255),
    def_id              VARCHAR2(255)
);
CREATE UNIQUE INDEX I0TB_BPM_SVC_EVT ON TB_BPM_SVC_EVT (service_path, message_class, correlation_key, def_id);
CREATE INDEX I1TB_BPM_SVC_EVT ON TB_BPM_SVC_EVT (def_id);

EXIT;
