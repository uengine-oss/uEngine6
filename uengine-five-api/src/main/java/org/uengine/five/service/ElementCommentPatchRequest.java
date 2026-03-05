package org.uengine.five.service;

/**
 * PATCH /definition/element-comments/{commentId} 요청 body (content 수정).
 */
public class ElementCommentPatchRequest {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
