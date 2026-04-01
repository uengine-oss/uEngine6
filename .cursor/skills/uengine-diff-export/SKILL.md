---
name: uengine-diff-export
description: >-
  Copies selected changed files from the uEngine6 workspace into a uengine-diff
  delivery folder preserving paths relative to the repo root (e.g.
  definition-service/src/..., uengine-five-api/src/..., oracle/scripts/...).
  Use when the user asks for uengine-diff, release diff, 부산은행 배포 복사,
  채팅 변경분 추출, or refresh a paste-into-target-project bundle after edits.
---

# uengine-diff export (변경 파일 배포용 복사)

## 목적

대화·작업으로 수정한 파일만 골라 **대상 폴더(`uengine-diff`)**에 **저장소 루트 기준 상대 경로**를 유지한 채 복사한다.  
받는 쪽은 **대상 uEngine 프로젝트 루트**에 같은 경로로 **파일 단위 전체 덮어쓰기**로 적용한다.

## diff 해석 (프론트·통합 담당자용 — 필수)

- **이 번들은 “라인 단위 패치”가 아니다.** Git diff나 “빨간 줄 = 지워라”로 읽지 않는다.
- **README 표에 나온 경로만** 배포 대상이다. 그 경로의 파일은 **번들 안의 파일 전체로 교체(overwrite)** 한다.
- **번들에 없는 파일·경로는 삭제·되돌리지 않는다.** (누락 = 이번 배포 범위 밖.)
- 수동 병합이 필요하면 **대상 브랜치 파일을 연 뒤**, 번들 파일 내용으로 통째로 바꾸거나 IDE 비교로 합친다. **diff만 보고 기존 코드를 지우는 방식은 금지**에 가깝다(오삭제 위험).
- 에이전트는 README **적용 방법**에 위 문장을 짧게라도 **반복**해 넣는다(수신 측이 스킬을 안 읽어도 README만으로 덮어쓰기가 분명하도록).

## 기본 경로 (uEngine6 관례)

- **소스 루트**: 현재 워크스페이스 저장소 루트 (`uEngine6`).
- **배포 폴더 (기본 예시)**:
  - 워크스페이스 내부: `uengine-diff/` (리포 루트와 나란히 두지 않고, 사용자가 지정한 절대 경로를 써도 됨)
  - 또는 사용자 지정: `D:\uEngineProject\부산은행\uengine-diff` 등  
  사용자가 `{DEST}`를 주면 그 경로를 쓴다. 주지 않으면 `./uengine-diff` 또는 사용자에게 한 번 확인한다.

## 경로 규칙 (process-gpt-vue3의 `src/` 와의 차이)

이 저장소는 **단일 `src/`가 아니다**. 복사 시 **Git 리포지터리 루트부터의 상대 경로**를 그대로 유지한다.

예:

- `definition-service/src/main/java/org/uengine/five/service/OracleStorage.java`
- `uengine-five-api/src/main/java/org/uengine/five/entity/ProcDefEntity.java`
- `oracle/scripts/schema.sql`
- `oracle/scripts/dba-alter-....sql`

`{DEST}` 아래에는 위 경로 전체가 재현되도록 둔다.  
즉 `{DEST}/definition-service/src/.../OracleStorage.java` 형태.

## 수행 절차

1. **포함할 파일 목록 확정**
   - **채팅/요청 범위**: 이번 대화에서 실제로 수정·추가한 경로만 넣는다 (불필요한 전체 `git status` 복사는 하지 않는다).
   - 사용자가 “전부”라고 하면, 그 세션에서 건드린 파일 또는 사용자가 나열한 경로를 따른다.
   - **프론트/로케일 작업**이 있었다면 `ko.json` / `en.json` 등 실제 수정된 로케일 경로를 **반드시** 목록에 포함한다.

2. **디렉터리 생성**
   - `{DEST}` 아래에 각 파일의 **상대 경로의 부모 디렉터리**를 만든다 (`mkdir -p`).

3. **복사**
   - 소스(워크스페이스 내 원본) → `{DEST}/<repo-relative-path>` 로 **항상 덮어쓴다** (기존 `{DEST}` 동일 경로 파일이 있으면 교체).
   - 줄바꿈은 원본 그대로 둔다.

4. **README.md (`{DEST}` 루트)**
   - 제목: 반영 범위(예: 채팅 세션 / 기능명 / 날짜).
   - **포함 파일 표**: 테이블로 **저장소 루트 기준 상대 경로** 전부 나열.
   - **적용 방법** (반드시 명시):
     - 대상 uEngine **저장소 루트**에 표의 경로 그대로 **폴더 구조 유지 후 파일 전체 덮어쓰기**.
     - **Git diff로 삭제 반영하지 말 것** — 표에 있는 파일만 교체, 없는 경로는 유지.
   - 로케일 JSON을 포함했으면 “전체 파일 덮어쓰기이므로 대상에서 로케일 수정이 있으면 diff 비교 권장”을 적는다.

5. **하지 말 것**
   - 사용자가 요청하지 않은 **apply.bat / shell 스크립트**는 넣지 않는다.

## 검증

- 복사 후 `{DEST}` 아래에 **의도한 파일만** 있는지 목록을 확인한다.
- README의 표와 실제 파일이 일치하는지 본다.

## 사용자 트리거 예시 (한국어)

- “uengine-diff에 바뀐 파일 복사해줘”
- “부산은행 uengine-diff 갱신”
- “채팅에서 수정한 것만 패키징”
- “release diff 처럼 변경분만 추출”

이때 이 스킬을 따른다.
