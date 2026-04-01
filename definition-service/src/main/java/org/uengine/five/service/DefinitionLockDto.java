package org.uengine.five.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Lock API 요청/응답 DTO.
 * GET 200, PUT 200: { "id": "...", "user_id": "..." }
 * PUT body: { "id": "...", "user_id": "..." } (user_id는 서버에서 JWT로 채워도 됨)
 */
public class DefinitionLockDto {
    private String id;

    @JsonProperty("user_id")
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
