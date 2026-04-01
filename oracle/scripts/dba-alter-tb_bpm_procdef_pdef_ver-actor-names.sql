-- ============================================================================
-- DBA 인계용: TB_BPM_PROCDEF / TB_BPM_PDEF_VER 변경자 표시명 컬럼 추가
-- ============================================================================
-- 적용    : Object Owner PAL_DB (또는 동일 오브젝트 소유 스키마) 접속 후 실행
-- 원칙    : busanbank_nonstandard_table_guide_for_agent.md §4.2
--           (차후 컬럼: NULL 허용, DEFAULT 없음, 제약 없음)
-- 선행    : 운영 백업 권장. 애플리케이션 배포는 스키마 적용 후 또는 동시 창구에서 협의.
-- 롤백 예 :
--   ALTER TABLE PAL_DB.TB_BPM_PROCDEF DROP COLUMN created_by_name;
--   ALTER TABLE PAL_DB.TB_BPM_PROCDEF DROP COLUMN updated_by_name;
--   ALTER TABLE PAL_DB.TB_BPM_PDEF_VER DROP COLUMN created_by_name;
--   ALTER TABLE PAL_DB.TB_BPM_PDEF_VER DROP COLUMN updated_by_name;
-- ============================================================================

ALTER TABLE PAL_DB.TB_BPM_PROCDEF ADD (created_by_name VARCHAR2(255));
ALTER TABLE PAL_DB.TB_BPM_PROCDEF ADD (updated_by_name VARCHAR2(255));

ALTER TABLE PAL_DB.TB_BPM_PDEF_VER ADD (created_by_name VARCHAR2(255));
ALTER TABLE PAL_DB.TB_BPM_PDEF_VER ADD (updated_by_name VARCHAR2(255));
