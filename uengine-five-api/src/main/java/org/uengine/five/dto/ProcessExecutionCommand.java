package org.uengine.five.dto;

import java.io.Serializable;

// import lombok.Data;

//@Data
public class ProcessExecutionCommand implements Serializable {
    private String processDefinitionId;

    private String instanceName;
    private boolean simulation;

    private RoleMapping[] roleMappings;
    private ProcessVariableValue[] processVariableValues;

    private String groups;

    private String correlationKeyValue;

    public String getCorrelationKeyValue() {
        return correlationKeyValue;
    }

    public void setCorrelationKeyValue(String correlationKeyValue) {
        this.correlationKeyValue = correlationKeyValue;
    }

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

    public boolean getSimulation() {
        return simulation;
    }

    public void setSimulation(boolean simulation) {
        this.simulation = simulation;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

}
