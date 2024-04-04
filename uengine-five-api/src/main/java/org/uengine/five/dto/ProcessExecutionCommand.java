package org.uengine.five.dto;

import java.io.Serializable;

// import lombok.Data;

//@Data
public class ProcessExecutionCommand implements Serializable {
    private String processDefinitionId;

    private String instanceName;
    private boolean isSimulation;

    private RoleMapping[] roleMappings;
    private ProcessVariableValue[] processVariableValues;

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public RoleMapping[] getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(RoleMapping[] roleMappings) {
        this.roleMappings = roleMappings;
    }

    public ProcessVariableValue[] getProcessVariableValues() {
        return processVariableValues;
    }

    public void setProcessVariableValues(ProcessVariableValue[] processVariableValues) {
        this.processVariableValues = processVariableValues;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public boolean isSimulation() {
        return isSimulation;
    }

    public void setSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

}
