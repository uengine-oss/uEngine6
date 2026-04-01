package org.uengine.five.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CatchEvent {
    @Column(name = "message_class", length = 255)
    String messageClass;
    @Column(name = "correlation_key", length = 255)
    String correlationKey;
    @Column(name = "def_id", length = 255)
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
