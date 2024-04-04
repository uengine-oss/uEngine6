package org.uengine.five.dto;

import java.io.Serializable;

public class ProcessVariableValue {
    String name;
    Serializable[] values;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Serializable[] getValues() {
        return values;
    }

    public void setValues(Serializable[] values) {
        this.values = values;
    }

    public org.uengine.kernel.ProcessVariableValue toKernelProcessVariableValue() {
        org.uengine.kernel.ProcessVariableValue kernelProcessVariableValue = new org.uengine.kernel.ProcessVariableValue();
        kernelProcessVariableValue.setName(name);
        for (Serializable value : values) {
            kernelProcessVariableValue.setValue(value);
            kernelProcessVariableValue.moveToAdd();
        }
        return kernelProcessVariableValue;
    }
}
