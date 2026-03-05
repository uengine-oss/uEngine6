package org.uengine.five.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PATCH /definition/element-comments/{commentId}/resolve 요청 body.
 */
public class ElementCommentResolveRequest {

    private Boolean resolved;
    @JsonProperty("resolve_action_text")
    private String resolveActionText;

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolveActionText() {
        return resolveActionText;
    }

    public void setResolveActionText(String resolveActionText) {
        this.resolveActionText = resolveActionText;
    }
}
