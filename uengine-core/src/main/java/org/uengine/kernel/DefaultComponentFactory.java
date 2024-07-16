package org.uengine.kernel;

import java.lang.reflect.Constructor;
import java.util.Map;
public class DefaultComponentFactory implements IComponentFactory {

    @Override
    public <T> T getComponent(Class<T> clazz, Object[] constructorParameters) {
        try {
            if(clazz == ProcessInstance.class) {
                clazz = (Class<T>) DefaultProcessInstance.class;
            }
            if (constructorParameters == null || constructorParameters.length == 0) {
                return clazz.newInstance();
            } else {
                Class<?>[] parameterTypes = new Class<?>[constructorParameters.length];
                for (int i = 0; i < constructorParameters.length; i++) {
                    if (constructorParameters[i] != null) {
                        parameterTypes[i] = constructorParameters[i].getClass();
                    } else {
                        parameterTypes[i] = null;
                    }
                }
                Constructor<?>[] constructors = clazz.getConstructors();
                Constructor<?> bestMatch = null;
                for (Constructor<?> constructor : constructors) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    if (paramTypes.length == parameterTypes.length) {
                        boolean match = true;
                        for (int i = 0; i < paramTypes.length; i++) {
                            if (parameterTypes[i] != null && !paramTypes[i].isAssignableFrom(parameterTypes[i])) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            bestMatch = constructor;
                            break;
                        }
                    }
                }
                if (bestMatch != null) {
                    return (T) bestMatch.newInstance(constructorParameters);
                } else {
                    throw new NoSuchMethodException("No suitable constructor found for " + clazz.getName());
                }
            }
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instantiate class: " + clazz.getName()
                    + ". The class might be an interface, abstract class, or might not have a default constructor.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access while trying to instantiate class: " + clazz.getName()
                    + ". Check if the constructor is accessible.", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument provided for the constructor of class: " + clazz.getName(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get component for class: " + clazz.getName(), e);
        }
    }

    @Override
    public <T> Map<String, T> getComponents(Class<T> clazz, Object[] constructorParameters) throws Exception {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getComponents'");
        return null;
    }


}
