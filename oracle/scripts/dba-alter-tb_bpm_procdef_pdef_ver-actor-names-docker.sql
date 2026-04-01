-- ============================================================================
-- Docker Oracle XE (schema-docker.sql) — SYSTEM 스키마용 변경자 컬럼 추가
-- ============================================================================
-- 적용    : system 계정 등 TB_BPM_* 테이블 소유자로 접속 후 실행
-- 전제    : 테이블명 TB_BPM_PROCDEF / TB_BPM_PDEF_VER (스키마 접두어 없음)
-- ============================================================================

ALTER TABLE TB_BPM_PROCDEF ADD (created_by_name VARCHAR2(255));
ALTER TABLE TB_BPM_PROCDEF ADD (updated_by_name VARCHAR2(255));

ALTER TABLE TB_BPM_PDEF_VER ADD (created_by_name VARCHAR2(255));
ALTER TABLE TB_BPM_PDEF_VER ADD (updated_by_name VARCHAR2(255));
