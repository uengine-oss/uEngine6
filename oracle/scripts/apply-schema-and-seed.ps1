# Docker Oracle 스키마 적용 + 부산은행 정의 적재 (PowerShell)
# uengine-oracle-xe 컨테이너가 실행 중인 상태에서 이 스크립트 실행
Add-Type -AssemblyName System.Web -ErrorAction SilentlyContinue

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptDir)
$DefDir = Join-Path $ProjectRoot "definitions\부산은행"
$BaseUrl = if ($env:DEFINITION_SERVICE_URL) { $env:DEFINITION_SERVICE_URL } else { "http://localhost:9093" }

Write-Host "=== 1. Oracle 스키마 적용 ===" -ForegroundColor Cyan
Get-Content "$ScriptDir\schema-docker.sql" | docker exec -i uengine-oracle-xe sqlplus -s system/oracle@//localhost:1521/XE
if ($LASTEXITCODE -ne 0) { Write-Host "sqlplus 종료코드: $LASTEXITCODE (일부 메시지는 정상일 수 있음)" }
Write-Host ""

Write-Host "=== 2. 부산은행 정의 적재 (definition-service 필요: $BaseUrl) ===" -ForegroundColor Cyan
if (-not (Test-Path $DefDir)) {
    Write-Host "폴더 없음: $DefDir" -ForegroundColor Yellow
    exit 0
}

# 폴더 생성
$body = '{"definition":""}'
$encPath = [System.Web.HttpUtility]::UrlEncode("부산은행")
try { Invoke-RestMethod -Uri "$BaseUrl/definition/raw?defPath=$encPath" -Method Put -ContentType "application/json; charset=utf-8" -Body $body } catch { }

foreach ($f in Get-ChildItem -Path $DefDir -Filter "*.bpmn") {
    $name = $f.Name
    $path = "부산은행/$name"
    Write-Host "저장: $path"
    $content = Get-Content -Path $f.FullName -Raw -Encoding UTF8
    $json = @{ definition = $content } | ConvertTo-Json
    $encPath = [System.Web.HttpUtility]::UrlEncode($path)
    try {
        Invoke-RestMethod -Uri "$BaseUrl/definition/raw?defPath=$encPath" -Method Put -ContentType "application/json; charset=utf-8" -Body $json
        Write-Host "  OK"
    } catch {
        Write-Host "  실패: $_"
    }
}
Write-Host "완료."
