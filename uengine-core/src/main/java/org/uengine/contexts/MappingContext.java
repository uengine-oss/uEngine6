package org.uengine.contexts;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.uengine.kernel.Activity;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.MappingElement;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessInstance;


public class MappingContext implements Serializable {

    public MappingContext() {
    }



    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    MappingElement[] mappingElements;

    public MappingElement[] getMappingElements() {
        return mappingElements;
    }

    public void setMappingElements(MappingElement[] mappingElements) {
        this.mappingElements = mappingElements;
    }


    String id;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}