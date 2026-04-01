@echo off
setlocal EnableExtensions EnableDelayedExpansion
rem Package a Docker build context without requiring Docker on Windows.
rem Usage: scripts\package-release-build-context.bat [version] [--normalize-lf] scripts\package-release-build-context.bat --normalize-lf

set "RESULT_CODE=0"
set "RELEASE_VERSION="
set "NORMALIZE_LF=0"

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--normalize-lf" (
  set "NORMALIZE_LF=1"
) else if not defined RELEASE_VERSION (
  set "RELEASE_VERSION=%~1"
) else (
  echo FATAL: unknown argument: %~1
  set "RESULT_CODE=1"
  goto cleanup
)
shift
goto parse_args

:args_done
if not defined RELEASE_VERSION set "RELEASE_VERSION=1.1.0"

set "ROOT_DIR="
if exist "%CD%\definition-service\Dockerfile" if exist "%CD%\process-service\Dockerfile" if exist "%CD%\keycloak-gateway\Dockerfile" set "ROOT_DIR=%CD%"
if not defined ROOT_DIR (
  pushd "%~dp0.." || goto fail
  set "ROOT_DIR=%CD%"
  popd
)
set "OUT_DIR=%ROOT_DIR%\offline-images"
set "PACKAGE_NAME=uengine-release-build-context-%RELEASE_VERSION%.tgz"
set "PACKAGE_PATH=%OUT_DIR%\%PACKAGE_NAME%"
set "WORK_DIR=%TEMP%\uengine-release-build-context.%RELEASE_VERSION%.%RANDOM%%RANDOM%"
set "STAGING_NAME=release-build-context-%RELEASE_VERSION%"
set "STAGING_DIR=%WORK_DIR%\%STAGING_NAME%"

call :require_command mvn || goto fail
call :require_command tar || goto fail

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%" || goto fail
if exist "%PACKAGE_PATH%" del /f /q "%PACKAGE_PATH%" || goto fail

mkdir "%STAGING_DIR%\definition-service\target" || goto fail
mkdir "%STAGING_DIR%\definition-service\src\main\resources" || goto fail
mkdir "%STAGING_DIR%\process-service\target" || goto fail
mkdir "%STAGING_DIR%\process-service\src\main\resources" || goto fail
mkdir "%STAGING_DIR%\keycloak-gateway\target" || goto fail
mkdir "%STAGING_DIR%\infra\keycloak" || goto fail
mkdir "%STAGING_DIR%\manifest" || goto fail

pushd "%ROOT_DIR%" || goto fail

echo === Package release build context: %RELEASE_VERSION% ^(root: %ROOT_DIR%^)
echo.
echo --- Build definition-service with Maven ---
call mvn clean package -DskipTests -pl definition-service -am -q || goto fail_popd

echo.
echo --- Build process-service with Maven ---
call mvn clean package -DskipTests -pl process-service -am -q || goto fail_popd

echo.
echo --- Build keycloak-gateway with Maven ---
call mvn clean package -DskipTests -f keycloak-gateway\pom.xml -q || goto fail_popd

call :require_file "%ROOT_DIR%\definition-service\Dockerfile" || goto fail_popd
call :require_file "%ROOT_DIR%\definition-service\src\main\resources\application.yml" || goto fail_popd
call :require_file "%ROOT_DIR%\process-service\Dockerfile" || goto fail_popd
call :require_file "%ROOT_DIR%\process-service\src\main\resources\application.yml" || goto fail_popd
call :require_file "%ROOT_DIR%\keycloak-gateway\Dockerfile" || goto fail_popd
call :require_file "%ROOT_DIR%\infra\keycloak\Dockerfile" || goto fail_popd
call :require_file "%ROOT_DIR%\infra\keycloak\realm-export.json" || goto fail_popd

call :copy_single_file "%ROOT_DIR%\definition-service\target\definition-service-*.jar" "%STAGING_DIR%\definition-service\target" || goto fail_popd
call :copy_single_file "%ROOT_DIR%\process-service\target\*-SNAPSHOT.jar" "%STAGING_DIR%\process-service\target" || goto fail_popd
call :copy_single_file "%ROOT_DIR%\keycloak-gateway\target\gateway-*.jar" "%STAGING_DIR%\keycloak-gateway\target" || goto fail_popd

copy /Y "%ROOT_DIR%\definition-service\Dockerfile" "%STAGING_DIR%\definition-service\Dockerfile" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\definition-service\src\main\resources\application.yml" "%STAGING_DIR%\definition-service\src\main\resources\application.yml" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\process-service\Dockerfile" "%STAGING_DIR%\process-service\Dockerfile" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\process-service\src\main\resources\application.yml" "%STAGING_DIR%\process-service\src\main\resources\application.yml" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\keycloak-gateway\Dockerfile" "%STAGING_DIR%\keycloak-gateway\Dockerfile" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\infra\keycloak\Dockerfile" "%STAGING_DIR%\infra\keycloak\Dockerfile" >nul || goto fail_popd
copy /Y "%ROOT_DIR%\infra\keycloak\realm-export.json" "%STAGING_DIR%\infra\keycloak\realm-export.json" >nul || goto fail_popd

call :write_release_manifest "%STAGING_DIR%\manifest\release.env" || goto fail_popd

pushd "%OUT_DIR%" || goto fail_popd
call tar -czf "%PACKAGE_NAME%" -C "%WORK_DIR%" "%STAGING_NAME%" || goto fail_popd2
popd

popd

echo.
echo === Package complete ===
echo Output file: %PACKAGE_PATH%
echo Note: build dependency images must be prepared on a Docker-capable machine:
echo   uengine-release-build-dependencies-%RELEASE_VERSION%.tar
goto cleanup

:fail_popd2
set "RESULT_CODE=1"
popd
goto fail_popd_after_pop

:fail_popd
set "RESULT_CODE=1"
popd

:fail_popd_after_pop
goto cleanup

:fail
set "RESULT_CODE=1"
goto cleanup

:cleanup
if exist "%WORK_DIR%" rmdir /s /q "%WORK_DIR%"
exit /b %RESULT_CODE%

:require_command
where "%~1" >nul 2>nul
if errorlevel 1 (
  echo FATAL: required command not found: %~1
  exit /b 1
)
exit /b 0

:require_file
if not exist "%~1" (
  echo FATAL: required file not found: %~1
  exit /b 1
)
exit /b 0

:copy_single_file
set "FILE_PATTERN=%~1"
set "DEST_DIR=%~2"
set /a FILE_COUNT=0
set "MATCHED_FILE="

for %%F in ("%FILE_PATTERN%") do (
  if exist "%%~fF" (
    set /a FILE_COUNT+=1
    set "MATCHED_FILE=%%~fF"
  )
)

if not "!FILE_COUNT!"=="1" (
  echo FATAL: expected exactly one file match for: %FILE_PATTERN%
  exit /b 1
)

if not exist "%DEST_DIR%" mkdir "%DEST_DIR%" || exit /b 1
copy /Y "!MATCHED_FILE!" "%DEST_DIR%\" >nul || exit /b 1
exit /b 0

:write_release_manifest
set "TARGET_FILE=%~1"
if "%NORMALIZE_LF%"=="1" (
  set "PS1_FILE=%TEMP%\write-release-manifest.%RANDOM%%RANDOM%.ps1"
  (
    echo $lines = @^(
    echo   'RELEASE_VERSION=' + $env:RELEASE_VERSION
    echo   'PACKAGE_LAYOUT=release-build-context'
    echo ^)
    echo $content = [string]::Join([char]10, $lines^) + [char]10
    echo [System.IO.File]::WriteAllText($env:TARGET_FILE, $content, [System.Text.UTF8Encoding]::new($false^)^)
  ) > "!PS1_FILE!"
  powershell -NoProfile -File "!PS1_FILE!" >nul
  del /f /q "!PS1_FILE!" >nul 2>nul
  if errorlevel 1 exit /b 1
) else (
  (
    echo RELEASE_VERSION=%RELEASE_VERSION%
    echo PACKAGE_LAYOUT=release-build-context
  ) > "%TARGET_FILE%"
  if errorlevel 1 exit /b 1
)
exit /b 0
