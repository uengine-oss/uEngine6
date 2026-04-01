@echo off
chcp 65001 >nul
set SCRIPT_DIR=%~dp0

echo === 1. Oracle 스키마 적용 ===
type "%SCRIPT_DIR%schema-docker.sql" | docker exec -i uengine-oracle-xe sqlplus -s system/oracle@//localhost:1521/XE
echo.
echo 스키마 적용 완료. 부산은행 정의 적재는 definition-service 기동 후 아래 중 하나 실행:
echo   PowerShell:  .\apply-schema-and-seed.ps1
echo   Git Bash:    ./seed-definitions-busanbank.sh
pause
