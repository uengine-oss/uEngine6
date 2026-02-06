/**
 * 파일 역할: 태스크 반송 실행 요청 Body를 표현하는 DTO
 *
 * 기능:
 * - POST /work-item/{taskId}/return 요청 모델
 * - targetTaskId 또는 tracingTag(둘 중 하나)로 반송 대상을 지정
 * - reason은 감사/이력용으로 저장(옵션)
 * - execScope는 서브프로세스/스코프 환경에서 대상 스코프 지정(옵션)
 */
package org.uengine.five.dto;

public class TaskReturnCommand {

    Long taskId;
    String tracingTag;
    String execScope;
    String reason;

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

    public String getExecScope() {
        return execScope;
    }

    public void setExecScope(String execScope) {
        this.execScope = execScope;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

