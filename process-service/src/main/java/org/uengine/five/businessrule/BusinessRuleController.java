package org.uengine.five.businessrule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.five.framework.ProcessTransactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Business rule CRUD API.
 *
 * Backing store is definition-service via getRawDefinition/putRawDefinition (.rule).
 */
@RestController
public class BusinessRuleController {

    private final BusinessRuleStore store;
    private final ObjectMapper plainObjectMapper = new ObjectMapper();

    public BusinessRuleController(BusinessRuleStore store) {
        this.store = store;
    }

    @GetMapping("/business-rules")
    @ProcessTransactional(readOnly = true)
    public List<Map<String, Object>> listBusinessRules() throws Exception {
        List<BusinessRuleStore.BusinessRuleFile> all = store.loadAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (BusinessRuleStore.BusinessRuleFile file : all) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("id", file.getId());
            summary.put("name", file.getName());
            summary.put("description", file.getDescription());
            result.add(summary);
        }
        return result;
    }

    @GetMapping("/business-rules/{ruleId}")
    @ProcessTransactional(readOnly = true)
    public Map<String, Object> getBusinessRule(@PathVariable("ruleId") String ruleId) throws Exception {
        BusinessRuleStore.BusinessRuleFile file = store.loadOrThrow(ruleId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", file.getId());
        detail.put("name", file.getName());
        detail.put("description", file.getDescription());
        detail.put("ruleJson", file.getRuleJson());
        return detail;
    }

    @PostMapping("/business-rules")
    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    public Map<String, Object> createBusinessRule(@RequestBody Map<String, Object> request) throws Exception {
        validateBusinessRuleRequest(request);
        String name = readOptionalString(request, "name");
        if (name == null) {
            name = readOptionalString(request, "title");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        name = name.trim();
        String description = readOptionalString(request, "description");
        JsonNode ruleJson = readRequiredRuleJson(request);

        Optional<BusinessRuleStore.BusinessRuleFile> duplicated = findByName(name);
        if (duplicated.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business rule name already exists");
        }

        String id = java.util.UUID.randomUUID().toString();
        BusinessRuleStore.BusinessRuleFile file = new BusinessRuleStore.BusinessRuleFile(id, name, description, ruleJson);
        store.save(file);

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        return response;
    }

    @PutMapping("/business-rules/{ruleId}")
    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    public Map<String, Object> updateBusinessRule(@PathVariable("ruleId") String ruleId,
            @RequestBody Map<String, Object> request)
            throws Exception {
        validateBusinessRuleRequest(request);
        String name = readOptionalString(request, "name");
        if (name == null) {
            name = readOptionalString(request, "title");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        name = name.trim();
        String description = readOptionalString(request, "description");
        JsonNode ruleJson = readRequiredRuleJson(request);

        BusinessRuleStore.BusinessRuleFile existing = store.loadOrThrow(ruleId);

        Optional<BusinessRuleStore.BusinessRuleFile> duplicated = findByName(name);
        if (duplicated.isPresent() && !existing.getId().equals(duplicated.get().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Business rule name already exists");
        }

        BusinessRuleStore.BusinessRuleFile updated = new BusinessRuleStore.BusinessRuleFile(existing.getId(), name,
                description, ruleJson);
        store.save(updated);

        Map<String, Object> response = new HashMap<>();
        response.put("id", updated.getId());
        return response;
    }

    private Optional<BusinessRuleStore.BusinessRuleFile> findByName(String name) throws Exception {
        List<BusinessRuleStore.BusinessRuleFile> all = store.loadAll();
        for (BusinessRuleStore.BusinessRuleFile file : all) {
            if (file.getName() != null && file.getName().equals(name)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    private void validateBusinessRuleRequest(Map<String, Object> request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        String name = readOptionalString(request, "name");
        if (name == null) {
            name = readOptionalString(request, "title");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }

        JsonNode ruleJson = readRequiredRuleJson(request);
        if (ruleJson == null || !ruleJson.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson must be a JSON object");
        }
    }

    private String readOptionalString(Map<String, Object> request, String key) {
        Object v = request != null ? request.get(key) : null;
        if (v == null) {
            return null;
        }
        if (v instanceof String) {
            String s = ((String) v).trim();
            return s.isEmpty() ? null : s;
        }
        return v.toString();
    }

    private JsonNode readRequiredRuleJson(Map<String, Object> request) {
        Object v = request != null ? request.get("ruleJson") : null;
        if (v == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson is required");
        }

        try {
            if (v instanceof String) {
                String s = ((String) v).trim();
                if (s.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson is required");
                }
                JsonNode parsed = plainObjectMapper.readTree(s);
                if (parsed == null || !parsed.isObject()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson must be a JSON object");
                }
                return parsed;
            }

            JsonNode node = plainObjectMapper.valueToTree(v);
            if (node == null || !node.isObject()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson must be a JSON object");
            }
            return node;
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ruleJson must be a JSON object");
        }
    }
}

