/**
 * 파일 역할: 태스크 반송 후보(이전 업무) 1건을 표현하는 DTO
 *
 * 기능:
 * - 반송 가능 후보 리스트 조회 API에서 UI로 내려주는 데이터 모델
 * - 최소 표시 정보(태스크ID/활동명/담당자/완료시각/execScope 등)를 포함
 */
package org.uengine.five.dto;

import java.util.Date;

public class TaskReturnCandidate {

    Long taskId;
    String tracingTag;
    String activityName;
    String endpoint;
    Date completedAt;
    String execScope;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTracingTag() {
        return tracingTag;
    }

    public void setTracingTag(String tracingTag) {
        this.tracingTag = tracingTag;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getExecScope() {
        return execScope;
    }

    public void setExecScope(String execScope) {
        this.execScope = execScope;
    }
}

