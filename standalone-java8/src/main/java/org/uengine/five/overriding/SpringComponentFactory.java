package org.uengine.five.overriding;

import java.util.Map;

import org.uengine.five.StandaloneApplication;
import org.uengine.kernel.IComponentFactory;

public class SpringComponentFactory implements IComponentFactory {

    @Override
    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters) {
        return StandaloneApplication.getApplicationContext().getBean(clazz, constructorParameters);
    }

    @Override
    public <T> Map<String, T> getComponents(Class<T> clazz, Object[] constructorParameters) {
        return StandaloneApplication.getApplicationContext().getBeansOfType(clazz);
    }
}
