package org.uengine.five.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        final String RESOURCE_ROOT = "definitions";

        try {
            if (definitionPath.indexOf(".") == -1) {
                definitionPath = definitionPath + ".bpmn";
            }

            if(!(definitionPath.startsWith(RESOURCE_ROOT) || definitionPath.startsWith("/archive"))) {
                definitionPath = RESOURCE_ROOT + "/" + definitionPath;
            }

            System.out.println(
                    new File(definitionPath).getAbsolutePath());

            IResource resource = new DefaultResource(definitionPath);
            InputStream inputStream = resourceManager.getInputStream(resource);

            StringBuilder xmlStringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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
