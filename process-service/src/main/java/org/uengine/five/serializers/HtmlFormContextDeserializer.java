package org.uengine.five.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.uengine.contexts.HtmlFormContext;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HtmlFormContextDeserializer extends JsonDeserializer<HtmlFormContext> {

    @Override
    public HtmlFormContext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode rootNode = mapper.readTree(p);

        HtmlFormContext formContext = new HtmlFormContext();

        String formDefId = rootNode.get("formDefId").asText();
        String filePath = rootNode.get("filePath").asText();
        JsonNode valueMapNode = rootNode.get("valueMap");

        HashMap<String, Serializable> valueMap = new HashMap<>();
        if (valueMapNode != null) {
            Iterator<String> fieldNames = valueMapNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (fieldName.equals("_type"))
                    continue;

                JsonNode fieldValueNode = valueMapNode.get(fieldName);
                if (fieldValueNode.isArray()) {
                    List<Serializable> list = new ArrayList<>();
                    fieldValueNode.forEach(item -> {
                        list.add(parseNode(item, mapper));
                    });
                    valueMap.put(fieldName, (Serializable) list);
                } else if (fieldValueNode.isObject()) {
                    valueMap.put(fieldName, parseNode(fieldValueNode, mapper));
                } else if (fieldValueNode.isTextual()) {
                    valueMap.put(fieldName, fieldValueNode.asText());
                }
            }
        }

        formContext.setFormDefId(formDefId);
        formContext.setFilePath(filePath);
        formContext.setValueMap(valueMap);

        return formContext;
    }

    private Serializable parseNode(JsonNode node, ObjectMapper mapper) {
        if (node.has("_type")) {
            String type = node.get("_type").asText();
            try {
                Class<?> clazz = Class.forName(type);
                return (Serializable) mapper.treeToValue(node, clazz);
            } catch (ClassNotFoundException | JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to deserialize node with _type: " + type, e);
            }
        } else {
            return parseNodeToMap(node);
        }
    }

    private HashMap<String, Serializable> parseNodeToMap(JsonNode node) {
        HashMap<String, Serializable> map = new HashMap<>();
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = node.get(fieldName);
            if (fieldValue.isTextual()) {
                map.put(fieldName, fieldValue.asText());
            } else if (fieldValue.isObject()) {
                map.put(fieldName, parseNode(fieldValue, new ObjectMapper()));
            } else if (fieldValue.isArray()) {
                List<HashMap<String, Serializable>> list = new ArrayList<>();
                fieldValue.forEach(item -> {
                    list.add(parseNodeToMap(item));
                });
                map.put(fieldName, (Serializable) list);
            }
        });
        return map;
    }
}
