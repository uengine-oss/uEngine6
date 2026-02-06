/**
 * 파일 역할: 태스크 반송 실행 결과를 UI에 전달하는 DTO
 *
 * 기능:
 * - POST /work-item/{taskId}/return 응답 모델
 * - 반송 후 현재 활성(worklist NEW/RUNNING) 태스크 목록을 함께 제공하여 UI 갱신에 사용
 */
package org.uengine.five.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskReturnResult {

    String instanceId;
    Long rootInstId;
    String targetTracingTag;
    List<Long> currentTaskIds;

    public TaskReturnResult() {
        this.currentTaskIds = new ArrayList<>();
    }

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

    public String getTargetTracingTag() {
        return targetTracingTag;
    }

    public void setTargetTracingTag(String targetTracingTag) {
        this.targetTracingTag = targetTracingTag;
    }

    public List<Long> getCurrentTaskIds() {
        return currentTaskIds;
    }

    public void setCurrentTaskIds(List<Long> currentTaskIds) {
        this.currentTaskIds = currentTaskIds;
    }
}

