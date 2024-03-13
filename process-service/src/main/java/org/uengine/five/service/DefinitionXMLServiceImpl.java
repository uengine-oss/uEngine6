package org.uengine.five.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;

@Service
public class DefinitionXMLServiceImpl implements DefinitionXMLService {

    @Autowired
    ResourceManager resourceManager;

    @Override
    public String getXMLDefinition(String definitionPath, boolean includeDependencies) {
        final String RESOURCE_ROOT = "";

        try {
            if (definitionPath.indexOf(".") == -1) {
                definitionPath = definitionPath + ".bpmn";
            }

            System.out.println(
                    new File(definitionPath).getAbsolutePath());

            IResource resource = new DefaultResource(
                    (definitionPath.startsWith(RESOURCE_ROOT) ? definitionPath : RESOURCE_ROOT + "/" + definitionPath));
            InputStream inputStream = resourceManager.getInputStream(resource);

            StringBuilder xmlStringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    xmlStringBuilder.append(line).append("\n");
                }
            }
            String xmlContent = xmlStringBuilder.toString();

            return xmlContent;

        } catch (Exception e) {
            throw new RuntimeException("Error when to load definition: " + definitionPath, e);
        }
    }

}
