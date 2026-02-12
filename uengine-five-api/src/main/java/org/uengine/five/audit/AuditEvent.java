package org.uengine.five.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 저장소에 무관한 감사 이벤트 모델.
 * 인스턴스 기준 조회·확장 가능한 페이로드를 위해 설계.
 * payload에는 직렬화 가능한 값만 넣을 것 (문자열, 숫자, Map, List 등).
 */
public class AuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 이벤트 유형 */
    private AuditEventType eventType;

    /** 루트 프로세스 인스턴스 ID (규칙적 조회용) */
    private Long rootInstId;

    /** (해당 시점) 프로세스 인스턴스 ID */
    private Long instId;

    /** 발생 시각 */
    private Date occurredAt;

    /** 액티비티 추적 태그 (해당 시) */
    private String tracingTag;

    /** 작업 수행자(예: 로그인 사용자 ID) */
    private String actor;

    /** 확장 데이터. 이벤트별 상세 내용을 key-value로 저장 (직렬화 가능한 값만) */
    private Map<String, Object> payload;

    public AuditEvent() {
        this.occurredAt = new Date();
        this.payload = new LinkedHashMap<>();
    }

    public static AuditEvent of(AuditEventType type, Long rootInstId, Long instId) {
        AuditEvent e = new AuditEvent();
        e.setEventType(type);
        e.setRootInstId(rootInstId);
        e.setInstId(instId);
        return e;
    }

    public AuditEvent withTracingTag(String tracingTag) {
        setTracingTag(tracingTag);
        return this;
    }

    public AuditEvent withActor(String actor) {
        setActor(actor);
        return this;
    }

    public AuditEvent withPayload(String key, Object value) {
        if (payload == null) payload = new LinkedHashMap<>();
        payload.put(key, value);
        return this;
    }

    public AuditEvent withPayloadMap(Map<String, Object> map) {
        if (map != null) {
            if (payload == null) payload = new LinkedHashMap<>();
            payload.putAll(map);
        }
        return this;
    }

    // --- getters/setters ---

    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }

    public Long getRootInstId() { return rootInstId; }
    public void setRootInstId(Long rootInstId) { this.rootInstId = rootInstId; }

    public Long getInstId() { return instId; }
    public void setInstId(Long instId) { this.instId = instId; }

    public Date getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Date occurredAt) { this.occurredAt = occurredAt; }

    public String getTracingTag() { return tracingTag; }
    public void setTracingTag(String tracingTag) { this.tracingTag = tracingTag; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
