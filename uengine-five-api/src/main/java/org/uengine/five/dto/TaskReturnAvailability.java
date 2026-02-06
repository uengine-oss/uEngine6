/**
 * 파일 역할: 태스크 반송 가능여부/사유/후보목록을 한 번에 전달하는 DTO
 *
 * 기능:
 * - GET /work-item/{taskId}/return/availability 응답 모델
 * - enabled=false인 경우 reason으로 UI에 불가 사유를 표시
 * - enabled=true인 경우 candidates로 반송 가능한 후보 태스크 목록을 제공
 */
package org.uengine.five.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskReturnAvailability {

    boolean enabled;
    String reason;
    List<TaskReturnCandidate> candidates;

    public static TaskReturnAvailability enabled(List<TaskReturnCandidate> candidates) {
        TaskReturnAvailability res = new TaskReturnAvailability();
        res.setEnabled(true);
        res.setCandidates(candidates == null ? new ArrayList<>() : candidates);
        return res;
    }

    public static TaskReturnAvailability disabled(String reason) {
        TaskReturnAvailability res = new TaskReturnAvailability();
        res.setEnabled(false);
        res.setReason(reason);
        res.setCandidates(new ArrayList<>());
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

    public List<TaskReturnCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<TaskReturnCandidate> candidates) {
        this.candidates = candidates;
    }
}

