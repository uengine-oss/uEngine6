package org.uengine.five.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POST /definition/element-comments 요청 body.
 */
public class ElementCommentCreateRequest {

    @JsonProperty("proc_def_id")
    private String procDefId;
    @JsonProperty("element_id")
    private String elementId;
    @JsonProperty("element_type")
    private String elementType;
    @JsonProperty("element_name")
    private String elementName;
    private String content;
    @JsonProperty("parent_comment_id")
    private String parentCommentId;

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
}
