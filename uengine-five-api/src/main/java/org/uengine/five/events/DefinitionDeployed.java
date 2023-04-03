package org.uengine.five.events;

/**
 * Created by uengine on 2018. 11. 16..
 */
public class DefinitionDeployed extends BusinessEvent {

    public String getDefintionId() {
        return defintionId;
    }

    public void setDefintionId(String defintionId) {
        this.defintionId = defintionId;
    }

    String defintionId;

}
