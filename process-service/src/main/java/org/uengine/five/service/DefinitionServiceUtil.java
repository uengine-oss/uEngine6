package org.uengine.five.service;

import org.springframework.stereotype.Component;

/**
 * Created by uengine on 2017. 11. 15..
 */
@Component
public interface DefinitionServiceUtil {
    public Object getDefinition(String defPath) throws Exception;

    public Object getDefinition(String defPath, String version) throws Exception;

    public Object getDefinition(String defPath, String version, String authToken) throws Exception;

}
