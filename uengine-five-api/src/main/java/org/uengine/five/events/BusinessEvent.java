package org.uengine.five.events;

/**
 * Created by uengine on 2018. 11. 17..
 */
public class BusinessEvent {
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;

    public BusinessEvent() {
        setType(getClass().getName());
    }

    public boolean checkMyEvent(){
        return getClass().getName().equals(getType());
    }
}
