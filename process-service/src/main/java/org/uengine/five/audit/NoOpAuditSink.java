package org.uengine.five.audit;

import java.util.Collections;
import java.util.List;

/**
 * 감사 로그를 기록하지 않는 Sink (비활성화 시 또는 테스트용).
 */
public class NoOpAuditSink implements AuditSink {

    @Override
    public void record(AuditEvent event) { }

    @Override
    public List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        return Collections.emptyList();
    }

    @Override
    public boolean supportsQuery() { return false; }
}
