/**
 * 파일 역할: 태스크 SKIP 가능여부/사유를 전달하는 DTO
 *
 * 기능:
 * - GET /work-item/{taskId}/skip/availability 응답 모델
 * - enabled=false인 경우 reason으로 UI에 불가 사유를 표시
 */
package org.uengine.five.dto;

public class TaskSkipAvailability {

    boolean enabled;
    String reason;

    public static TaskSkipAvailability enabled() {
        TaskSkipAvailability res = new TaskSkipAvailability();
        res.setEnabled(true);
        return res;
    }

    public static TaskSkipAvailability disabled(String reason) {
        TaskSkipAvailability res = new TaskSkipAvailability();
        res.setEnabled(false);
        res.setReason(reason);
        return res;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

