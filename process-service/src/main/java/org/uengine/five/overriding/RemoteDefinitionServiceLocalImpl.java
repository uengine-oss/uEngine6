package org.uengine.five.overriding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.five.service.DefinitionRequest;
import org.uengine.five.service.DefinitionService;
import org.uengine.processmanager.DefinitionServiceLocal;

/**
 * Oracle/remote 모드에서 raw definition API를 definition-service로 위임한다.
 */
@Service
@Primary
@ConditionalOnProperty(name = "uengine.definition.service.mode", havingValue = "remote")
public class RemoteDefinitionServiceLocalImpl implements DefinitionServiceLocal {

    @Autowired
    private DefinitionService definitionService;

    @Override
    public Object getRawDefinition(String defPath) throws Exception {
        return definitionService.getRawDefinition(defPath);
    }

    @Override
    public String listDefinitionRaw(String basePath) throws Exception {
        return definitionService.listDefinitionRaw(basePath);
    }

    @Override
    public Object putRawDefinition(String defPath, String definition) throws Exception {
        if (defPath == null || defPath.trim().isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "defPath is required");
        }

        DefinitionRequest request = new DefinitionRequest();
        request.setDefinition(definition);
        return definitionService.putRawDefinition(defPath, request);
    }
}
