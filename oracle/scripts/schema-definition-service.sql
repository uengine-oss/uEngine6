-- ============================================================================
-- uEngine BPM Oracle 스키마 DDL — definition-service 전용
-- ============================================================================
-- 목적    : definition-service Oracle 프로필에서 사용하는 테이블만 정의
-- 원칙    : TB_ 네이밍 / PK 제약 미사용 / I0 유니크 인덱스 / NOT NULL+DEFAULT / 문자열 일시 컬럼
-- 적용    : Object Owner PAL_DB 접속 후 실행. 서비스 계정은 synonym/권한 부여 후 사용
-- 참고    : 부산은행 확정값 - OBJECT OWNER: PAL_DB, TABLESPACE: TS_CQI_D001
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 시퀀스 (ID 생성)
-- ---------------------------------------------------------------------------
CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ---------------------------------------------------------------------------
-- 1. TB_BPM_DEF_INFO (ProcessDefinitionEntity)
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
-- 2. TB_BPM_PROCDEF (ProcDefEntity)
-- LOB 보유. 정의 저장(현재 버전/raw).
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
-- 3. TB_BPM_PDEF_VER (ProcDefVersionEntity)
-- 이력성 + LOB. 파티션 적용.
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
-- 4. TB_BPM_PDEF_CMT (ProcDefCommentEntity)
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
-- 5. TB_BPM_DEF_LOCK (DefinitionLockEntity)
-- ---------------------------------------------------------------------------
CREATE TABLE PAL_DB.TB_BPM_DEF_LOCK (
    resource_id         VARCHAR2(512) DEFAULT ' ' NOT NULL,
    user_id             VARCHAR2(255) DEFAULT ' ' NOT NULL,
    updated_at          VARCHAR2(14) DEFAULT '00000000000000' NOT NULL
) TABLESPACE TS_CQI_D001;

CREATE UNIQUE INDEX PAL_DB.I0TB_BPM_DEF_LOCK
    ON PAL_DB.TB_BPM_DEF_LOCK (resource_id)
    TABLESPACE TS_CQI_D001;
