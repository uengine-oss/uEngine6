package org.uengine.five.businessrule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.processmanager.DefinitionServiceLocal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Loads/saves business rule definitions via definition-service raw definition
 * APIs.
 *
 * Storage path: definitions/businessRules/*.rule (client passes
 * "businessRules/*.rule")
 * Legacy fallback: definitions/buisnessRules/*.json (typo directory + json ext)
 */
@Component
public class BusinessRuleStore {

    static final String BUSINESS_RULES_DIR = "businessRules";
    static final String BUSINESS_RULES_DIR_LEGACY_TYPO = "buisnessRules";
    static final String RULE_EXT = ".rule";
    static final String LEGACY_JSON_EXT = ".json";

    private final DefinitionServiceLocal definitionService;
    private final ObjectMapper plainObjectMapper;

    public BusinessRuleStore(@Autowired DefinitionServiceLocal definitionService) {
        this.definitionService = definitionService;
        this.plainObjectMapper = new ObjectMapper();
    }

    public BusinessRuleFile loadOrThrow(String ruleId) throws Exception {
        for (String root : getReadDirs()) {
            // preferred: .rule
            String path = root + "/" + ruleId + RULE_EXT;
            BusinessRuleFile preferred = readIfExists(path);
            if (preferred != null) {
                return preferred;
            }

            // legacy: .json
            String legacyPath = root + "/" + ruleId + LEGACY_JSON_EXT;
            BusinessRuleFile legacy = readIfExists(legacyPath);
            if (legacy != null) {
                return legacy;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Business rule not found: " + ruleId);
    }

    public List<BusinessRuleFile> loadAll() throws Exception {
        Map<String, BusinessRuleFile> byId = new LinkedHashMap<>();

        // Prefer "businessRules" directory, and fallback to legacy typo directory.
        // We list files from definition-service, then read each file via
        // getRawDefinition.
        loadAllFromDir(byId, BUSINESS_RULES_DIR);
        loadAllFromDir(byId, BUSINESS_RULES_DIR_LEGACY_TYPO);

        return new ArrayList<>(byId.values());
    }

    public void save(BusinessRuleFile file) throws Exception {
        ObjectNode node = plainObjectMapper.createObjectNode();
        node.put("id", file.getId());
        node.put("name", file.getName());
        if (file.getDescription() != null) {
            node.put("description", file.getDescription());
        } else {
            node.putNull("description");
        }
        node.set("ruleJson", file.getRuleJson());

        // Ensure directory exists (create folder when no extension)
        try {
            definitionService.putRawDefinition(BUSINESS_RULES_DIR, null);
        } catch (Exception ignore) {
        }

        String path = BUSINESS_RULES_DIR + "/" + file.getId() + RULE_EXT;
        String json = plainObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        definitionService.putRawDefinition(path, json);
    }

    private List<String> getReadDirs() {
        List<String> dirs = new ArrayList<>();
        dirs.add(BUSINESS_RULES_DIR);
        dirs.add(BUSINESS_RULES_DIR_LEGACY_TYPO);

        return dirs;
    }

    private BusinessRuleFile readIfExists(String path) throws Exception {
        try {
            Object raw = definitionService.getRawDefinition(path);
            JsonNode node = toJsonNode(raw);
            if (node == null || !node.isObject()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Invalid business rule file: " + path);
            }

            String id = readText(node, "id");
            String name = readText(node, "name");
            String description = readOptionalText(node, "description");
            JsonNode ruleJson = node.get("ruleJson");
            if (ruleJson == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Invalid business rule file (missing ruleJson): " + path);
            }

            return new BusinessRuleFile(id, name, description, ruleJson);
        } catch (ResponseStatusException rse) {
            if (rse.getStatus() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw rse;
        } catch (Exception e) {
            // For Feign errors and other IO issues, treat as not found only if it looks
            // like 404;
            // otherwise propagate to surface real problems.
            if (e != null && e.getMessage() != null && e.getMessage().contains("404")) {
                return null;
            }
            throw e;
        }
    }

    private void loadAllFromDir(Map<String, BusinessRuleFile> byId, String basePath) throws Exception {
        String json;
        try {
            json = definitionService.listDefinitionRaw(basePath);
        } catch (Exception e) {
            // directory might not exist on server
            return;
        }

        JsonNode root = plainObjectMapper.readTree(json);
        JsonNode defs = root != null ? root.path("_embedded").path("definitions") : null;
        if (defs == null || !defs.isArray()) {
            return;
        }

        // Prefer .rule over .json when both exist for same id
        Map<String, String> pathById = new LinkedHashMap<>();
        for (JsonNode d : defs) {
            String name = d.path("name").asText(null);
            String path = d.path("path").asText(null);
            if (name == null || path == null) {
                continue;
            }
            if (!(name.endsWith(RULE_EXT) || name.endsWith(LEGACY_JSON_EXT))) {
                continue;
            }

            String id = name;
            int dot = id.lastIndexOf('.');
            if (dot != -1) {
                id = id.substring(0, dot);
            }
            String existing = pathById.get(id);
            if (existing == null) {
                pathById.put(id, path);
            } else {
                // upgrade legacy json to rule if present
                if (existing.endsWith(LEGACY_JSON_EXT) && path.endsWith(RULE_EXT)) {
                    pathById.put(id, path);
                }
            }
        }

        for (String id : pathById.keySet()) {
            if (byId.containsKey(id)) {
                continue;
            }
            String relPath = pathById.get(id); // e.g. "businessRules/<id>.rule"
            BusinessRuleFile br = readIfExists(relPath);
            if (br != null && br.getId() != null && !byId.containsKey(br.getId())) {
                byId.put(br.getId(), br);
            }
        }
    }

    private JsonNode toJsonNode(Object raw) throws Exception {
        if (raw == null) {
            return null;
        }
        if (raw instanceof JsonNode) {
            return (JsonNode) raw;
        }
        if (raw instanceof String) {
            return plainObjectMapper.readTree(((String) raw));
        }
        return plainObjectMapper.valueToTree(raw);
    }

    private String readText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || !v.isTextual() || v.asText().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Invalid business rule file (missing " + field + ")");
        }
        return v.asText();
    }

    private String readOptionalText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isTextual()) {
            return v.asText();
        }
        return v.toString();
    }

    public static class BusinessRuleFile {
        private final String id;
        private final String name;
        private final String description;
        private final JsonNode ruleJson;

        public BusinessRuleFile(String id, String name, String description, JsonNode ruleJson) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.ruleJson = ruleJson;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public JsonNode getRuleJson() {
            return ruleJson;
        }
    }
}
