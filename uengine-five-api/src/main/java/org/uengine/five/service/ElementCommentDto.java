package org.uengine.five.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * 요소 댓글 API 요청/응답 DTO.
 * JSON 필드명은 snake_case (proc_def_id 등).
 */
public class ElementCommentDto {

    private String id;
    @JsonProperty("proc_def_id")
    private String procDefId;
    @JsonProperty("element_id")
    private String elementId;
    @JsonProperty("element_type")
    private String elementType;
    @JsonProperty("element_name")
    private String elementName;
    @JsonProperty("author_id")
    private String authorId;
    @JsonProperty("author_name")
    private String authorName;
    private String content;
    @JsonProperty("parent_comment_id")
    private String parentCommentId;
    @JsonProperty("is_resolved")
    private Boolean isResolved;
    @JsonProperty("resolved_by")
    private String resolvedBy;
    @JsonProperty("resolved_at")
    private Date resolvedAt;
    @JsonProperty("resolve_action_text")
    private String resolveActionText;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("tenant_id")
    private String tenantId;

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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
