package org.uengine.five.entity;

import javax.persistence.Embeddable;

@Embeddable
public class CatchEvent {
    String messageClass;
    String correlationKey;
    String defId;
    public String getMessageClass() {
        return messageClass;
    }
    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }
    public String getCorrelationKey() {
        return correlationKey;
    }
    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }
    public String getDefId() {
        return defId;
    }
    public void setDefId(String defId) {
        this.defId = defId;
    }

}
