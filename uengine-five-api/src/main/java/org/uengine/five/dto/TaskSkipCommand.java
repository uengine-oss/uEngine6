/**
 * 파일 역할: 태스크 SKIP 요청 바디 DTO
 *
 * 기능:
 * - POST /work-item/{taskId}/skip 요청 모델
 * - reason: SKIP 사유(선택)
 */
package org.uengine.five.dto;

public class TaskSkipCommand {

    String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

