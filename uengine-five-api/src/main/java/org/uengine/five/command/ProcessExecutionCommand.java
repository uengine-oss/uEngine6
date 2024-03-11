package org.uengine.five.command;

import org.uengine.kernel.ProcessVariableValue;
import org.uengine.kernel.RoleMapping;

// import lombok.Data;

//@Data
public class ProcessExecutionCommand {
    private String processDefinitionId;
    private RoleMapping[] roleMappings;
    private ProcessVariableValue[] processVariableValues;
    private String instanceName;
    private boolean isSimulation;

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
