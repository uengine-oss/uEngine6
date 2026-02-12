package org.uengine.five.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 여러 Sink에 동시에 기록. (예: DB + 파일)
 */
@Component
public class CompositeAuditSink implements AuditSink {

    private static final Logger log = LoggerFactory.getLogger(CompositeAuditSink.class);

    private final List<AuditSink> sinks = new ArrayList<>();

    public CompositeAuditSink() { }

    public void addSink(AuditSink sink) {
        if (sink != null && sink != this) sinks.add(sink);
    }

    @Override
    public void record(AuditEvent event) {
        for (AuditSink s : sinks) {
            try {
                s.record(event);
            } catch (Exception e) {
                log.warn("CompositeAuditSink: one sink failed ({}): {}", s.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        for (AuditSink s : sinks) {
            if (s.supportsQuery()) return s.listByRootInstanceId(rootInstId, limit);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean supportsQuery() {
        return sinks.stream().anyMatch(AuditSink::supportsQuery);
    }
}
