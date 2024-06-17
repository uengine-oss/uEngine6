package org.uengine.five.dto;

import java.io.Serializable;
import java.util.Map;

public class DryRunExecutionCommand implements Serializable {

    private ProcessExecutionCommand processExecutionCommand;
    private WorkItemResource workItem;
    private Map<String, String> variables;

    public ProcessExecutionCommand getProcessExecutionCommand() {
        return processExecutionCommand;
    }
    public void setProcessExecutionCommand(ProcessExecutionCommand processExecutionCommand) {
        this.processExecutionCommand = processExecutionCommand;
    }

    public WorkItemResource getWorkItem() {
        return workItem;
    }
    public void setWorkItem(WorkItemResource workItem) {
        this.workItem = workItem;
    }   

    public Map<String, String> getVariables() {
        return variables;
    }
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

}
