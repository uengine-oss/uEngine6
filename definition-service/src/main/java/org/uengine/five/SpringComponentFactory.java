package org.uengine.five;

import java.util.Map;

import org.uengine.five.DefinitionServiceApplication;
import org.uengine.kernel.IComponentFactory;

public class SpringComponentFactory implements IComponentFactory {

    @Override
    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters) {
        return DefinitionServiceApplication.getApplicationContext().getBean(clazz, constructorParameters);
    }

    @Override
    public <T> Map<String, T> getComponents(Class<T> clazz, Object[] constructorParameters) {
        return DefinitionServiceApplication.getApplicationContext().getBeansOfType(clazz);
    }
}
