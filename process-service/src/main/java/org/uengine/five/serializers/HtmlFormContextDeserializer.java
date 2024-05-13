package org.uengine.five.serializers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.uengine.contexts.HtmlFormContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                    continue; // _type 필드는 무시

                JsonNode fieldValueNode = valueMapNode.get(fieldName);
                if (fieldValueNode.isArray()) {
                    // 배열인 경우
                    List<HashMap<String, Serializable>> list = new ArrayList<>();
                    fieldValueNode.forEach(item -> {
                        list.add(parseNodeToMap(item));
                    });
                    valueMap.put(fieldName, (Serializable) list);
                } else if (fieldValueNode.isObject()) {
                    // 단일 객체인 경우
                    valueMap.put(fieldName, parseNodeToMap(fieldValueNode));
                }
            }
        }

        formContext.setFormDefId(formDefId);
        formContext.setFilePath(filePath);
        formContext.setValueMap(valueMap);

        return formContext;
    }

    private HashMap<String, Serializable> parseNodeToMap(JsonNode node) {
        HashMap<String, Serializable> map = new HashMap<>();
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = node.get(fieldName);
            map.put(fieldName, fieldValue.asText()); // 간단한 문자열 값으로 가정
        });
        return map;
    }
}