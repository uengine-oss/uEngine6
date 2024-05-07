package org.uengine.contexts;

import org.uengine.kernel.FieldDescriptor;

public class EventSynchronization {
    String eventType;
    FieldDescriptor[] attributes;
    MappingContext mappingContext;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public FieldDescriptor[] getAttributes() {
        return attributes;
    }

    public void setAttributes(FieldDescriptor[] attributes) {
        this.attributes = attributes;
    }

    public MappingContext getMappingContext() {
        return mappingContext;
    }

    public void setMappingContext(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }
}
