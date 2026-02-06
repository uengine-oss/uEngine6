-- Oracle UDF 초기화 스크립트 (함수 추가 시 이 파일에 DDL 추가)
-- 적용: sqlplus 또는 SQL Developer에서 해당 스키마(system, APP_USER 등)로 접속 후 실행
-- 예: sqlplus system/oracle @oracle/scripts/init.sql

-- ---------------------------------------------------------------------------
-- REGEXP_LIKE_YN: JPQL function('REGEXP_LIKE_YN', ...) 호출용
-- 사용 예 (JPQL): function('REGEXP_LIKE_YN', pi.currEp, :rolePattern) = 1
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION REGEXP_LIKE_YN(p_text IN VARCHAR2, p_pattern IN VARCHAR2)
RETURN NUMBER
IS
BEGIN
  IF p_text IS NULL OR p_pattern IS NULL THEN
    RETURN 0;
  END IF;

  IF REGEXP_LIKE(p_text, p_pattern) THEN
    RETURN 1;
  ELSE
    RETURN 0;
  END IF;
END REGEXP_LIKE_YN;
/
