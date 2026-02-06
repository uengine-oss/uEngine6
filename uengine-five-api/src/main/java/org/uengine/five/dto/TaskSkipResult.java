/**
 * 파일 역할: 태스크 SKIP 실행 결과 DTO
 *
 * 기능:
 * - POST /work-item/{taskId}/skip 응답 모델
 * - SKIP 이후 생성된 현재 workitem(taskId) 목록을 반환(있으면)
 */
package org.uengine.five.dto;

import java.util.List;

public class TaskSkipResult {

    String instanceId;
    Long rootInstId;
    String skippedTracingTag;
    List<Long> currentTaskIds;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Long getRootInstId() {
        return rootInstId;
    }

    public void setRootInstId(Long rootInstId) {
        this.rootInstId = rootInstId;
    }

    public String getSkippedTracingTag() {
        return skippedTracingTag;
    }

    public void setSkippedTracingTag(String skippedTracingTag) {
        this.skippedTracingTag = skippedTracingTag;
    }

    public List<Long> getCurrentTaskIds() {
        return currentTaskIds;
    }

    public void setCurrentTaskIds(List<Long> currentTaskIds) {
        this.currentTaskIds = currentTaskIds;
    }
}

