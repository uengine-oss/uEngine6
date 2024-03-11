package org.uengine.kernel;

public interface IComponentFactory {

    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters);

}
