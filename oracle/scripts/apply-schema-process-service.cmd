@echo off
chcp 65001 >nul
set SCRIPT_DIR=%~dp0
REM process-service Oracle 프로필 접속 정보 (application.yml oracle 프로필과 동일)
if "%ORACLE_USER%"=="" set ORACLE_USER=system
if "%ORACLE_PASSWORD%"=="" set ORACLE_PASSWORD=oracle

echo === process-service 스키마 적용 (TB_BPM_AUDIT 등) ===
echo Container: uengine-oracle-xe, User: %ORACLE_USER%
type "%SCRIPT_DIR%schema-process-service-docker.sql" | docker exec -i uengine-oracle-xe sqlplus -s %ORACLE_USER%/%ORACLE_PASSWORD%@//localhost:1521/XE
echo.
echo process-service 스키마 적용 완료.
pause
