package org.uengine.five.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

import org.uengine.five.entity.converter.OracleBooleanConverter;
import org.uengine.five.entity.converter.OracleDateTimeStringConverter;

/**
 * 프로세스 정의 요소별 댓글 (BPMN 요소 코멘트).
 * 테이블: proc_def_comments
 */
@Entity
@Table(name = "TB_BPM_PDEF_CMT")
public class ProcDefCommentEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "proc_def_id", nullable = false, length = 512)
    private String procDefId;

    @Column(name = "element_id", nullable = false, length = 255)
    private String elementId;

    @Column(name = "element_type", length = 128)
    private String elementType;

    @Column(name = "element_name", length = 512)
    private String elementName;

    @Column(name = "author_id", nullable = false, length = 255)
    private String authorId;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "parent_comment_id", length = 36)
    private String parentCommentId;

    @Convert(converter = OracleBooleanConverter.class)
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "resolved_at")
    private Date resolvedAt;

    @Column(name = "resolve_action_text", length = 1000)
    private String resolveActionText;

    @Column(name = "tenant_id", length = 255)
    private String tenantId;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Convert(converter = OracleDateTimeStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = new Date();
    }

    // getters / setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolveActionText() {
        return resolveActionText;
    }

    public void setResolveActionText(String resolveActionText) {
        this.resolveActionText = resolveActionText;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
