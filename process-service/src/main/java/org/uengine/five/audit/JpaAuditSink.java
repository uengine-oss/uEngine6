package org.uengine.five.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.uengine.five.entity.AuditLogEntity;
import org.uengine.five.repository.AuditLogEntityRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA(bpm_audit_log) 기반 감사 Sink.
 * uengine.audit.sink=jpa 일 때만 빈 생성 → 감사 테이블이 없어도 코어 실행 가능.
 */
@Component
@ConditionalOnProperty(name = "uengine.audit.sink", havingValue = "jpa")
public class JpaAuditSink implements AuditSink {

    private final AuditLogEntityRepository repository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_QUERY_LIMIT = 500;

    public JpaAuditSink(AuditLogEntityRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) return;
        AuditLogEntity entity = toEntity(event);
        repository.save(entity);
    }

    @Override
    public List<AuditEvent> listByRootInstanceId(Long rootInstId, int limit) {
        if (rootInstId == null) return List.of();
        int size = limit > 0 ? limit : DEFAULT_QUERY_LIMIT;
        return repository.findByRootInstIdOrderByOccurredAtDesc(rootInstId, org.springframework.data.domain.PageRequest.of(0, size))
                .stream()
                .map(this::toEvent)
                .collect(Collectors.toList());
    }

    @Override
    public boolean supportsQuery() { return true; }

    private AuditLogEntity toEntity(AuditEvent event) {
        AuditLogEntity e = new AuditLogEntity();
        e.setEventType(event.getEventType() != null ? event.getEventType().name() : AuditEventType.CUSTOM.name());
        e.setRootInstId(event.getRootInstId());
        e.setInstId(event.getInstId());
        e.setTracingTag(event.getTracingTag());
        e.setOccurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : new java.util.Date());
        e.setActor(event.getActor());
        if (event.getPayload() != null && !event.getPayload().isEmpty()) {
            try {
                e.setPayload(objectMapper.writeValueAsString(event.getPayload()));
            } catch (JsonProcessingException ex) {
                e.setPayload("{ \"_serializeError\": \"" + ex.getMessage() + "\" }");
            }
        }
        return e;
    }

    private AuditEvent toEvent(AuditLogEntity e) {
        AuditEvent ev = new AuditEvent();
        try {
            ev.setEventType(AuditEventType.valueOf(e.getEventType()));
        } catch (Exception ignored) {
            ev.setEventType(AuditEventType.CUSTOM);
        }
        ev.setRootInstId(e.getRootInstId());
        ev.setInstId(e.getInstId());
        ev.setTracingTag(e.getTracingTag());
        ev.setOccurredAt(e.getOccurredAt());
        ev.setActor(e.getActor());
        if (e.getPayload() != null && !e.getPayload().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = objectMapper.readValue(e.getPayload(), java.util.Map.class);
                ev.setPayload(map);
            } catch (JsonProcessingException ignored) { }
        }
        return ev;
    }
}
