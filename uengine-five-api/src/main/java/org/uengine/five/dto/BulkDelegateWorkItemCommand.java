package org.uengine.five.dto;

import java.util.List;

public class BulkDelegateWorkItemCommand {

    private List<String> taskIds;
    private RoleMappingCommand delegatedRoleMapping;
    private boolean delegateOnlyForWorkitem;

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public RoleMappingCommand getDelegatedRoleMapping() {
        return delegatedRoleMapping;
    }

    public void setDelegatedRoleMapping(RoleMappingCommand delegatedRoleMapping) {
        this.delegatedRoleMapping = delegatedRoleMapping;
    }

    public boolean isDelegateOnlyForWorkitem() {
        return delegateOnlyForWorkitem;
    }

    public void setDelegateOnlyForWorkitem(boolean delegateOnlyForWorkitem) {
        this.delegateOnlyForWorkitem = delegateOnlyForWorkitem;
    }
}
