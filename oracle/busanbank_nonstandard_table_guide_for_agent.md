# 부산은행 비표준 테이블 생성 가이드 요약 (에이전트용)

## 목적
이 문서는 부산은행 비표준 테이블 생성 가이드를 **에이전트가 빠르게 판단하고 DDL을 수정**할 수 있도록 규칙 중심으로 정리한 요약본이다.  
원본은 `02. 비표준테이블 생성 가이드.xlsx` 기준이다.

---

## 1. 먼저 확인할 정보
DDL 작성 전 아래 정보를 먼저 확보해야 한다.

### 1.1 부산은행 적용 값 (확정)
다음 값은 부산은행 환경에서 적용된 확정 값이다. DDL 플레이스홀더 치환 시 사용한다.

| 항목 | 값 |
|------|-----|
| **OBJECT OWNER** | `PAL_DB` |
| **DATA TABLESPACE** | `TS_CQI_D001` |
| **INDEX TABLESPACE** | `TS_CQI_D001` |
| **LOB TABLESPACE** | `TS_CQI_D001` |

### 1.2 필수 확인 항목
1. **Object Owner명**
   - 기존 시스템 고도화라면 기존에 사용하던 schema 확인
   - 부산은행 적용: `PAL_DB`
2. **서비스 운영 계정**
   - 자체 데몬/에이전트 기동인지
   - 제우스, 웹로직 등 미들웨어 기동인지
3. **권한 분리 여부**
   - Object Owner와 서비스 운영 계정은 **분리 관리가 원칙**
   - 계정이 분리되면 별도 권한 부여, synonym 생성 등이 필요할 수 있음

### 1.3 계정 권한 (부산은행 확정)
Role 및 계정별 권한은 아래와 같다.

| 구분 | 부여 대상 | Role | 권한 |
|------|-----------|------|------|
| **개발** | 행번유저, PAL_AP | RL_PAL_AP | SELECT, INSERT, UPDATE, DELETE 모두 부여 가능 |
| **운영** | PAL_AP | RL_PAL_AP | SELECT, INSERT, UPDATE, DELETE 모두 부여 가능 |
| **운영** | 행번유저 | RL_PAL_SL | **SELECT만** 부여 가능 |

- 개발: RL_PAL_AP role을 **행번유저** 및 **PAL_AP** 계정이 부여받음.
- 운영: RL_PAL_AP role은 **PAL_AP** 계정이 부여받고, RL_PAL_SL role은 **행번유저**가 부여받음.

---

## 2. 핵심 원칙
### 2.1 컬럼 표준
- 비메타 테이블이어도 **메타/도메인 표준만 제외**하고 나머지 컬럼 작성 원칙은 최대한 맞춘다.
- 솔루션/패키지 성격 테이블은 DBA와 협의 후 일부 융통성 허용 가능
- 고객정보 관련 데이터는 **암호화 필요**
- 비표준 컬럼 사용 시 정보보호부에 **마스킹 요청 가능성** 고려

### 2.2 적재량/보관주기
- **모든 테이블별 예상 적재량**을 먼저 파악해야 한다.
- 적재량과 보관주기를 기준으로 **파티션 전환 여부**를 판단한다.

---

## 3. 네이밍 규칙
### 3.1 테이블명
- 기본 접두어: **`TB_`**
- 인덱스를 고려하여 **28 byte 내**에서 생성 권장
- 예시:
  - `TB_BPM_PROCINST`
  - `TB_BPM_WORKLIST`

### 3.2 인덱스명
- 유니크 인덱스 기본명: **`I0 + 테이블명`**
- 일반 인덱스: **`I1 ~ I9, IA ~ IZ`** 범위 사용 가능
- 원칙:
  - `I0TB_XXX` = 기본 유니크 인덱스
  - `I1TB_XXX`, `I2TB_XXX` = 일반 인덱스

### 3.3 LOB 명명
- CLOB/LOB 저장명 기본 접두어: **`LS_`**
- 규칙:
  - `LS_테이블명_순번`
- 예시:
  - `LS_TB_XXX_00`
  - `LS_TB_XXX_01`

---

## 4. 컬럼 작성 규칙
### 4.1 NOT NULL + DEFAULT는 반드시 한 쌍
- **NOT NULL 단독 사용 금지**
- NOT NULL이면 반드시 DEFAULT와 함께 사용
- 예:
```sql
A CHAR(2) DEFAULT '01' NOT NULL,
B VARCHAR2(30) DEFAULT ' ' NOT NULL
```

### 4.2 차후 컬럼 추가 시 기본 원칙
- 컬럼 추가는 기본적으로
  - `NULL 허용`
  - `DEFAULT 없음`
  - `Constraint 없음`
- 이유:
  - 제약조건을 걸고 컬럼을 추가하면 **장시간 테이블 락**이 발생할 수 있음

### 4.3 TIMESTAMP 사용
- **EDWDB 제외 원칙적으로 TIMESTAMP 사용 불가**
- 날짜/일시는 가급적 **VARCHAR2**로 설계
- 실무 해석 예:
  - 일자: `VARCHAR2(8)` → `YYYYMMDD`
  - 일시: `VARCHAR2(14)` → `YYYYMMDDHH24MISS`

---

## 5. PK / 인덱스 규칙
### 5.1 PK 제약 사용 금지
- **PK 제약조건은 사용하지 않는다**
- 대신 아래 조합으로 처리:
  - **유니크 인덱스**
  - **NOT NULL**

즉, 다음 방식이 원칙이다.
```sql
-- PK 제약 대신
CREATE UNIQUE INDEX XXX_DB.I0TB_XXX
ON XXX_DB.TB_XXX (A, B, C);
```

### 5.2 불필요 인덱스 생성 금지
- 인덱스 신청 시 **중복/불필요 인덱스 여부** 확인 필요
- 예:
  - `I0(A,B,C)`가 이미 있으면
  - `I1(A,B)`는 불필요할 수 있음
- 이유:
  - `I0`에서 이미 `(A,B)` 정렬이 선행됨

---

## 6. 파티션 규칙
### 6.1 파티션 적용 기준 (부산은행 확정)
다음 **둘 중 하나**에 해당하면 파티션 적용 대상이다.
- **LOB 컬럼을 사용하는 테이블**
- **월 10만 건 이상** 데이터가 적재되는 테이블

**예외:** LOB 컬럼을 사용하나 데이터가 **지속적으로 증가하지 않는 마스터성 테이블**인 경우, **DBA 협의 후 일반 테이블 생성 가능**하다.

### 6.2 파티션 고려 대상 (일반)
아래 테이블은 파티션 생성 여부를 적극 검토한다.
- 데이터가 지속적으로 쌓이는 **내역성 테이블**
- 데이터가 지속적으로 쌓이는 **로그성 테이블**
- **LOB 컬럼(CLOB 등)** 을 사용하는 내역성/로그성 테이블 → **무조건 파티션 고려**

### 6.3 파티션 키
- 기본적으로 **날짜 컬럼**을 파티션 키로 사용
- 보통 `RANGE (월)` 구조를 사용
- 예:
```sql
PARTITION BY RANGE (B)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
```

### 6.4 파티션 키와 유니크 인덱스
- **파티션 키 컬럼은 반드시 UNIQUE 인덱스(I0)에 포함**
- 이유:
  - LOCAL 인덱스 생성을 위함

### 6.5 보관주기 고려
- 데이터량이 많으면 **보관주기**까지 함께 설계
- 예:
  - 영구 보관
  - 6개월 보관

---

## 7. CLOB / LOB 규칙
### 7.1 사용 전 협의
- **CLOB 사용 시 DBA와 사전 협의 필수**

### 7.2 저장 방식
- SecureFile 방식 예시:
```sql
LOB (EX_CLOB1) STORE AS SECUREFILE LS_TB_XXX_00 (TABLESPACE TS_LOB_D001)
LOB (EX_CLOB2) STORE AS SECUREFILE LS_TB_XXX_01 (TABLESPACE TS_LOB_D001)
```

### 7.3 실무 해석
다음 조건이면 사실상 파티션 + LOB 설계를 같이 봐야 한다.
- 로그성/내역성
- 적재량 많음
- CLOB 존재

---

## 8. TABLESPACE 규칙
### 8.1 테이블 테이블스페이스
- 테이블은 DBA와 협의된 테이블스페이스 사용
- **부산은행 적용:** `TS_CQI_D001`

### 8.2 인덱스 테이블스페이스
- 인덱스도 DBA와 협의된 별도 테이블스페이스 사용
- **부산은행 적용:** `TS_CQI_D001`

### 8.3 LOB 테이블스페이스
- LOB는 별도 LOB 테이블스페이스 사용
- **부산은행 적용:** `TS_CQI_D001`

---

## 9. DDL 템플릿 예시
부산은행 적용 시 Object Owner `PAL_DB`, TABLESPACE `TS_CQI_D001` 사용.
```sql
CREATE TABLE PAL_DB.TB_XXX
(
    A CHAR(2) DEFAULT '01' NOT NULL,
    B VARCHAR2(30) DEFAULT ' ' NOT NULL,
    C CHAR(8) DEFAULT ' ' NOT NULL,
    D VARCHAR2(9),
    EX_CLOB1 CLOB,
    EX_CLOB2 CLOB
) TABLESPACE TS_CQI_D001
PARTITION BY RANGE (B)
(
    PARTITION P202501 VALUES LESS THAN ('202502')
)
LOB (EX_CLOB1) STORE AS SECUREFILE LS_TB_XXX_00 (TABLESPACE TS_CQI_D001)
LOB (EX_CLOB2) STORE AS SECUREFILE LS_TB_XXX_01 (TABLESPACE TS_CQI_D001);

CREATE UNIQUE INDEX PAL_DB.I0TB_XXX
ON PAL_DB.TB_XXX (A, B, C)
TABLESPACE TS_CQI_D001;
```

---

## 10. 에이전트용 변환 규칙
기존 JPA/Hibernate DDL을 부산은행 표준에 맞게 바꿀 때 아래 순서로 점검한다.

### 10.1 테이블명 점검
- `TB_` 접두어 여부 확인
- 28 byte 초과 여부 확인

### 10.2 PK 제거
- `PRIMARY KEY (...)` 제거
- 해당 컬럼들에 `NOT NULL + DEFAULT` 적용 여부 확인
- `CREATE UNIQUE INDEX I0...`로 대체

### 10.3 날짜/시간형 변환
- `TIMESTAMP` → `VARCHAR2(14)` 또는 협의된 날짜형 문자 컬럼
- `DATE`도 가능하면 가이드에 맞는 문자형 기준 검토

### 10.4 파티션 대상 식별
**부산은행 기준:** LOB 컬럼 사용 **또는** 월 10만 건 이상 적재 → 파티션 적용. LOB 사용하나 마스터성(증가 없음)이면 DBA 협의 후 일반 테이블 가능.

일반 점검 기준:
- 로그성 / 이력성
- 월 누적 적재
- CLOB 존재
- 보관주기 분리 필요

### 10.5 LOB 처리
- CLOB가 있으면 DBA 협의 대상 표시
- `LOB STORE AS SECUREFILE LS_...` 구문 추가 검토

### 10.6 인덱스 중복 제거
- `I0`가 이미 커버하는 선두 컬럼 조합이 있으면
- `I1` 이하 인덱스 불필요 여부 검토

---

## 11. 에이전트 체크리스트
DDL 검토 시 아래 체크리스트를 그대로 사용한다.

### 필수 체크
- [ ] Object Owner / 서비스 운영 계정 확인 (부산은행: PAL_DB, RL_PAL_AP/RL_PAL_SL)
- [ ] 권한 분리 필요 여부 확인
- [ ] 테이블명 `TB_` 규칙 준수
- [ ] 테이블명 28 byte 이내 확인
- [ ] PK 제약 제거 여부 확인
- [ ] `I0` 유니크 인덱스로 대체 여부 확인
- [ ] NOT NULL 컬럼에 DEFAULT가 함께 있는지 확인
- [ ] TIMESTAMP 사용 여부 확인
- [ ] 로그/이력/LOB 테이블의 파티션 여부 확인
- [ ] 파티션 키가 `I0` 유니크 인덱스에 포함되는지 확인
- [ ] CLOB 사용 시 DBA 협의 항목 표기
- [ ] TABLESPACE / INDEX TABLESPACE / LOB TABLESPACE 정의 여부 확인
- [ ] 불필요 중복 인덱스 제거 여부 확인
- [ ] 고객정보 암호화/마스킹 필요 여부 확인

---

## 12. 파티션 참고 예시
원본 시트의 예시상 다음과 같은 패턴이 반복된다.

### 대표 패턴
- 파티션 타입: `RANGE (월)`
- 파티션 키: 기준일자 / 거래발생일자 / 승인신청일자 등 날짜성 컬럼
- 월 적재량:
  - 수백만 건
  - 수천만 건
  - `CLOB`
  - `CLOB (12,500)`
  - `CLOB (150,000)`
- 보관주기:
  - 영구
  - 6개월

### 해석
다음 테이블은 거의 무조건 파티션 검토 대상이다.
- 월 단위 누적 건수가 큰 테이블
- CLOB 포함 테이블
- 발송로그 / IF전문 / 게시판내역 같은 로그/내역성 테이블

---

## 13. 에이전트가 바로 내려야 할 판단 기준
### 그대로 제출 가능
- 거의 없음
- 단순 참조성 소규모 테이블만 일부 가능

### 수정 후 제출
다음 중 하나라도 해당하면 수정 필요:
- `PRIMARY KEY` 사용
- `TIMESTAMP` 사용
- `TB_` 접두어 없음
- NOT NULL인데 DEFAULT 없음
- CLOB 있는데 LOB 저장 정의 없음
- 로그/이력/누적성인데 파티션 없음
- 인덱스명이 `I0/I1...` 규칙이 아님

### DBA 협의 필요
- CLOB 사용
- 대량 적재 예상
- 보관주기 정의 필요
- 운영계정/Owner 분리
- 고객정보 포함
- 솔루션/패키지 특성으로 표준 완전 준수가 어려운 경우

---

## 14. 한 줄 요약
부산은행 비표준 테이블 생성 가이드는 다음 한 문장으로 요약할 수 있다.

> **“TB_ 네이밍, PK 대신 I0 유니크 인덱스, NOT NULL+DEFAULT 세트, TIMESTAMP 지양, 로그/LOB 테이블 파티션, TABLESPACE/LOB 명명 규칙 준수”**

---

## 15. 에이전트용 실행 프롬프트 예시
아래 문장을 에이전트 지시문으로 그대로 사용할 수 있다.

```md
당신은 부산은행 비표준 테이블 생성 가이드에 맞춰 Oracle DDL을 검토/수정하는 에이전트다.

반드시 다음 규칙을 적용한다.
1. 테이블명은 TB_ 접두어를 사용하고 28 byte 내를 우선 고려한다.
2. PRIMARY KEY 제약은 제거하고, 유니크 인덱스 I0 + NOT NULL 방식으로 바꾼다.
3. NOT NULL 컬럼은 반드시 DEFAULT와 함께 선언한다.
4. TIMESTAMP는 사용하지 않고 VARCHAR2 기반 날짜/일시 컬럼으로 전환을 우선 검토한다.
5. 로그성/내역성/LOB 포함 테이블은 파티션 생성 여부를 반드시 검토한다.
6. 파티션 키는 날짜 컬럼을 사용하고, I0 유니크 인덱스에 포함시킨다.
7. CLOB 사용 시 LOB STORE AS SECUREFILE LS_... 구문과 LOB TABLESPACE를 검토한다.
8. TABLESPACE / INDEX TABLESPACE / LOB TABLESPACE를 누락하지 않는다.
9. 중복되거나 불필요한 인덱스를 제거한다.
10. 고객정보가 있으면 암호화/마스킹 협의 필요 항목으로 표시한다.

출력 시 다음 형식으로 정리한다.
- 미부합 항목
- 수정 이유
- 수정 전/수정 후 예시
- DBA 협의 필요 항목
- 최종 DDL 초안
```

---

## 출처
- `02. 비표준테이블 생성 가이드.xlsx`
  - `1.오브젝트`
  - `2.파티션`
