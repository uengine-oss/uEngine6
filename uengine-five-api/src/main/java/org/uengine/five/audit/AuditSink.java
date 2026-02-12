package org.uengine.five.audit;

import java.util.List;

/**
 * 감사 이벤트 저장소 추상화.
 * 구현체별로 DB(JPA), 파일, 다른 DB, 복수 저장소 등으로 유연하게 구성 가능.
 */
public interface AuditSink {

    /**
     * 단일 감사 이벤트 기록.
     * @param event 기록할 이벤트 (non-null)
     */
    void record(AuditEvent event);

    /**
     * 인스턴스 기준 감사 로그 조회 (선택 구현).
     * 저장소가 조회를 지원하지 않으면 빈 목록 또는 UnsupportedOperationException.
     *
     * @param rootInstId 루트 프로세스 인스턴스 ID (규칙적 조회용)
     * @param limit 최대 건수 (0 이하면 구현체 기본값)
     * @return 해당 인스턴스의 감사 이벤트 목록 (최신순 등 구현체 정의)
     */
    default List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        throw new UnsupportedOperationException("listByRootInstanceId not supported by " + getClass().getName());
    }

    /**
     * 이 Sink가 인스턴스별 감사 로그 조회를 지원하는지 여부.
     * true이면 GET /instance/{id}/audit 호출 시 이 Sink의 listByRootInstanceId가 사용되고,
     * false이면 조회 시 이 Sink는 사용되지 않고(빈 목록 또는 다른 Sink 결과 사용) 기록(record)만 수행한다.
     */
    default boolean supportsQuery() {
        return false;
    }
}
