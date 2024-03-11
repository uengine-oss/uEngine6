package org.uengine.five.overriding;

import org.uengine.five.ProcessServiceApplication;
import org.uengine.kernel.IComponentFactory;

public class SpringComponentFactory implements IComponentFactory {

    @Override
    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters) {
        return ProcessServiceApplication.getApplicationContext().getBean(clazz, constructorParameters);
    }
}
