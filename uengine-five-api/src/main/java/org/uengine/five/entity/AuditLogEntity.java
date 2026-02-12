package org.uengine.five.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * 확장 가능한 감사 로그 엔티티.
 * 이벤트 유형별 상세는 payload(JSON)에 저장하여 스키마 변경 없이 확장 가능.
 * 인스턴스 번호(rootInstId, instId)로 규칙적 조회 가능.
 *
 * <p><b>저장 정책 (append-only)</b>: 이 테이블은 감사 목적으로 <b>삽입(INSERT)만</b> 수행하며,
 * 수정(UPDATE) 및 삭제(DELETE)는 애플리케이션에서 수행하지 않는다.
 * 오래된 데이터 정리는 별도 아카이브/배치 정책으로만 처리하는 것을 권장한다.
 */
@Entity
@Table(name = "bpm_audit_log", indexes = {
    @Index(name = "idx_audit_log_root_inst", columnList = "rootInstId"),
    @Index(name = "idx_audit_log_inst_occurred", columnList = "instId, occurredAt")
    // 필요 시 '누가' 조회용: @Index(name = "idx_audit_log_actor", columnList = "actor")
})
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false)
    private Long rootInstId;

    private Long instId;

    @Column(length = 512)
    private String tracingTag;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date occurredAt;

    @Column(length = 255)
    private String actor;

    @Lob
    @Column(length = 65535)
    private String payload;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getRootInstId() { return rootInstId; }
    public void setRootInstId(Long rootInstId) { this.rootInstId = rootInstId; }

    public Long getInstId() { return instId; }
    public void setInstId(Long instId) { this.instId = instId; }

    public String getTracingTag() { return tracingTag; }
    public void setTracingTag(String tracingTag) { this.tracingTag = tracingTag; }

    public Date getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Date occurredAt) { this.occurredAt = occurredAt; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
