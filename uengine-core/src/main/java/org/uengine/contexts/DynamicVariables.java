package org.uengine.contexts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.uengine.kernel.BeanPropertyResolver;

public class DynamicVariables implements BeanPropertyResolver, Serializable {
    private Map<String, Object> properties = new HashMap<>();

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void setBeanProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object getBeanProperty(String key) {
        return properties.get(key);
    }
}