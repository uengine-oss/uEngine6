package org.uengine.five.audit;

import java.util.List;

/**
 * 감사 로그 기록의 진입점.
 * 구현체에서 설정된 AuditSink(DB, 파일, 복수 등)로 전달하도록 구성.
 */
public interface AuditService {

    /**
     * 감사 이벤트를 등록한다. 실제 저장은 구성된 Sink에 위임.
     *
     * @param event 기록할 이벤트 (non-null)
     */
    void record(AuditEvent event);

    /**
     * 루트 인스턴스 ID로 감사 로그 조회 (구현체/저장소가 지원할 경우).
     *
     * @param rootInstId 루트 프로세스 인스턴스 ID
     * @param limit 최대 건수 (0 이하면 구현체 기본값)
     * @return 감사 이벤트 목록
     */
    List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit);
}
