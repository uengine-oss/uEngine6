package org.uengine.five.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkDelegateWorkItemResult {

    private int total;
    private int successCount;
    private int failureCount;
    private List<ItemResult> results = new ArrayList<>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<ItemResult> getResults() {
        return results;
    }

    public void setResults(List<ItemResult> results) {
        this.results = results;
    }

    public void addSuccess(String taskId) {
        results.add(new ItemResult(taskId, true, null));
        successCount++;
        total++;
    }

    public void addFailure(String taskId, String reason) {
        results.add(new ItemResult(taskId, false, reason));
        failureCount++;
        total++;
    }

    public static class ItemResult {
        private String taskId;
        private boolean success;
        private String reason;

        public ItemResult() {
        }

        public ItemResult(String taskId, boolean success, String reason) {
            this.taskId = taskId;
            this.success = success;
            this.reason = reason;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
