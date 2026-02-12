package org.uengine.five.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 감사 로그 기록 서비스 구현.
 * 구성된 AuditSink에 이벤트를 전달. Sink는 설정(jpa/file/composite)에 따라 AuditConfig에서 주입됨.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditSink sink;

    public AuditServiceImpl(AuditSink auditSink) {
        this.sink = auditSink != null ? auditSink : new NoOpAuditSink();
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) return;
        try {
            sink.record(event);
        } catch (Exception e) {
            log.warn("Audit record failed: {} - {}", event.getEventType(), e.getMessage());
        }
    }

    @Override
    public List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        if (rootInstId == null) return Collections.emptyList();
        try {
            if (sink.supportsQuery()) return sink.listByRootInstanceId(rootInstId, limit);
        } catch (Exception e) {
            log.warn("Audit listByRootInstanceId failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
