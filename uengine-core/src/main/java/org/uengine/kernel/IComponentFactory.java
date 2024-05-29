package org.uengine.kernel;

import java.util.Map;

public interface IComponentFactory {

    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters);
    public <T> Map<String, T> getComponents(Class<T> clazz, Object[] constructorParameters) throws Exception;

}
