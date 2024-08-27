package org.uengine.kernel;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.uengine.contexts.TextContext;
import org.uengine.processdesigner.mapper.TransformerMapping;

/**
 * @author Jinyoung Jang
 */
public class ParameterContext implements Serializable {

    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    public static final String DIRECTION_IN = "in".intern();
    public static final String DIRECTION_OUT = "out".intern();
    public static final String DIRECTION_INOUT = "in-out".intern();
    private boolean multipleInput;

    public ParameterContext() {
    }

    // MappingElement - Target
    // ParameterContext - Source
    TextContext argument = org.uengine.contexts.TextContext.createInstance();

    public TextContext getArgument() {
        if (argument != null && argument.getText() == null && getVariable() != null) {
            return getVariable().getDisplayName();
        }

        return argument;
    }

    public void setArgument(TextContext string) {
        argument = string;
    }

    // Boolean split;

    // public Boolean getSplit() {
    //     return split;
    // }

    // public void setSplit(Boolean split) {
    //     this.split = split;
    // }

    // MappingElement - Source
    // ParameterContext - Target
    ProcessVariable variable;

    public ProcessVariable getVariable() {
        return variable;
    }

    public void setVariable(ProcessVariable variable) {
        this.variable = variable;
    }

    transient Object type;

    public Object getType() {
        return type;
    }

    public void setType(Object name) {
        type = name;
    }

    String direction;

    // @Range(options={"IN-OUT", "IN", "OUT", }, values={ "in-out", "in", "out",})
    public String getDirection() {
        return direction;
    }

    public void setDirection(String i) {
        direction = i;
    }

    TransformerMapping transformerMapping;

    // @Hidden
    public TransformerMapping getTransformerMapping() {
        return transformerMapping;
    }

    public void setTransformerMapping(TransformerMapping transformerMapping) {
        this.transformerMapping = transformerMapping;
    }

    public boolean isMultipleInput() {
        return multipleInput;
    }

    public void setMultipleInput(boolean multipleInput) {
        this.multipleInput = multipleInput;
    }
}
