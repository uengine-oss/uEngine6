package org.uengine.five.audit;

/**
 * 감사 로그 이벤트 유형.
 * 확장 시 이 열거형에 추가하거나, 동적 타입은 문자열로 기록할 수 있음.
 */
public enum AuditEventType {

    /** 액티비티 실행 (기존 활동 감사) */
    ACTIVITY_EXECUTION,

    /** 태스크 담당자 변경(위임) */
    TASK_DELEGATION,

    /** 프로세스 변수(전역변수) 변경 */
    VARIABLE_CHANGE,

    /** 기타 확장용 (payload에 subType 등으로 구체 타입 명시 가능) */
    CUSTOM
}
