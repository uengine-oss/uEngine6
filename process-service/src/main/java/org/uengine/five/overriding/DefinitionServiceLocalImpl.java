package org.uengine.five.overriding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.modeling.resource.ContainerResource;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IContainer;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.processmanager.DefinitionServiceLocal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Local (filesystem-based) {@link DefinitionService} implementation for
 * process-service.
 *
 * Why:
 * - process-service already reads BPMN definitions from local filesystem (see
 * {@code DefinitionXMLServiceImpl})
 * - business rule files (*.rule / legacy *.json) were still fetched via
 * definition-service (Feign),
 * causing failures when definition-service is not reachable (e.g. local dev).
 *
 * Enable:
 * - default: enabled (matchIfMissing = true)
 * - to force remote Feign client, set: uengine.definition.service.mode=remote
 */
@Service
@Primary
@ConditionalOnProperty(name = "uengine.definition.service.mode", havingValue = "local", matchIfMissing = true)
public class DefinitionServiceLocalImpl implements DefinitionServiceLocal {

    private static final String RESOURCE_ROOT = "definitions";

    @Autowired
    ResourceManager resourceManager;

    private final ObjectMapper plainObjectMapper = new ObjectMapper();

    @Override
    public String listDefinitionRaw(String basePath) throws Exception {
        if (basePath == null) {
            basePath = "";
        }

        IContainer container = new ContainerResource();
        container.setPath(RESOURCE_ROOT + "/" + basePath);
        List<IResource> resources;
        try {
            resources = resourceManager.listFiles(container);
        } catch (Exception e) {
            // align with remote behavior: directory might not exist
            return plainObjectMapper.createObjectNode()
                    .set("_embedded",
                            plainObjectMapper.createObjectNode().set("definitions",
                                    plainObjectMapper.createArrayNode()))
                    .toString();
        }

        ObjectNode root = plainObjectMapper.createObjectNode();
        ObjectNode embedded = plainObjectMapper.createObjectNode();
        ArrayNode defs = plainObjectMapper.createArrayNode();

        for (IResource r : resources) {
            if (r == null || r.getPath() == null) {
                continue;
            }
            ObjectNode def = plainObjectMapper.createObjectNode();
            def.put("name", r.getName());

            // definition-service strips "definitions/" prefix from path before returning
            String path = r.getPath().replace("\\", "/");
            if (path.startsWith(RESOURCE_ROOT + "/")) {
                path = path.substring((RESOURCE_ROOT + "/").length());
            }
            def.put("path", path);
            defs.add(def);
        }

        embedded.set("definitions", defs);
        root.set("_embedded", embedded);
        return plainObjectMapper.writeValueAsString(root);
    }

    @Override
    public Object getRawDefinition(String definitionPath) throws Exception {
        String normalized = normalizeDefinitionPath(definitionPath);
        DefaultResource resource = new DefaultResource(normalized);
        if (!resourceManager.exists(resource) || resourceManager.isContainer(resource)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Definition not found: " + definitionPath);
        }

        try (InputStream in = resourceManager.getInputStream(resource);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    @Override
    public Object putRawDefinition(String definitionPath, String definition) throws Exception {
        if (definitionPath == null || definitionPath.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "defPath is required");
        }

        String dp = definitionPath.trim().replace("\\", "/");
        if (dp.startsWith("/")) {
            dp = dp.substring(1);
        }

        // Folder creation semantics (definition-service behavior)
        if (dp.indexOf('.') == -1) {
            IContainer container = new ContainerResource();
            container.setPath(RESOURCE_ROOT + "/" + dp);
            resourceManager.createFolder(container);
            return null;
        }

        String normalized = normalizeDefinitionPath(dp);
        DefaultResource resource = new DefaultResource(normalized);
        String content = (definition == null ? "" : definition);
        resourceManager.save(resource, content);
        return null;
    }

    private static String normalizeDefinitionPath(String definitionPath) {
        String p = definitionPath == null ? "" : definitionPath.trim();
        p = p.replace("\\", "/");
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.startsWith(RESOURCE_ROOT + "/")) {
            return p;
        }
        return RESOURCE_ROOT + "/" + p;
    }
}
