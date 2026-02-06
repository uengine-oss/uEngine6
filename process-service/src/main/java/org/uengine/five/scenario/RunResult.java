package org.uengine.five.scenario;

import java.io.Serializable;
import java.util.Map;

/**
 * 시나리오 실행 결과 (1단계 API 응답)
 */
public class RunResult implements Serializable {

    private boolean success;
    private String message;
    private String instanceId;
    private Map<String, Object> expected;
    private Map<String, Object> actual;
    private Map<String, Object> diff;
    private String logs;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, Object> getExpected() {
        return expected;
    }

    public void setExpected(Map<String, Object> expected) {
        this.expected = expected;
    }

    public Map<String, Object> getActual() {
        return actual;
    }

    public void setActual(Map<String, Object> actual) {
        this.actual = actual;
    }

    public Map<String, Object> getDiff() {
        return diff;
    }

    public void setDiff(Map<String, Object> diff) {
        this.diff = diff;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
