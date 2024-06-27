package org.uengine.five.serializers;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.uengine.contexts.HtmlFormContext;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.overriding.IAMRoleResolutionContext;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.Role;
import org.uengine.kernel.RoleResolutionContext;
import org.uengine.kernel.ScopeActivity;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.SubProcess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BpmnXMLParser {

    static ObjectMapper objectMapper = createTypedJsonObjectMapper();

    public static String convertToJavaType(String simpleTypeName) {
        switch (simpleTypeName) {
            case "Text":
                return "java.lang.String";
            case "Number":
                return "java.lang.Number";
            case "Date":
                return "java.util.Date";
            case "Form":
                return "org.uengine.kernel.FormActivity";
            default:
                throw new IllegalArgumentException("Unknown type: " + simpleTypeName);
        }
    }

    public static ObjectMapper createTypedJsonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignore null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT); // ignore zero and false when it is int
                                                                                 // or boolean

        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "_type");

        return objectMapper;
    }

    public static ObjectMapper createTypedJsonArrayObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignore null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT); // ignore zero and false when it is int
                                                                                 // or boolean

        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "_type");

        SimpleModule module = new SimpleModule();
        module.addDeserializer(HtmlFormContext.class, new HtmlFormContextDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }

    // protected void parseProcessVariables(Node dataNode, ScopeActivity
    // processDefinition) {
    // if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList variableNodes = dataNode.getChildNodes();
    // for (int j = 0; j < variableNodes.getLength(); j++) {
    // Node variableNode = variableNodes.item(j);
    // if (variableNode.getNodeType() == Node.ELEMENT_NODE
    // && variableNode.getNodeName().equals("uengine:variable")) {
    // Element variableElement = (Element) variableNode;
    // String name = variableElement.getAttribute("name");
    // String type = variableElement.getAttribute("type");

    // // Create a new ProcessVariable instance
    // ProcessVariable variable = new ProcessVariable();
    // variable.setName(name);
    // try {
    // // Assuming the type attribute is a fully qualified class name
    // variable.setType(Class.forName(type));
    // } catch (ClassNotFoundException e) {
    // throw new RuntimeException("Class not found for type: " + type);
    // }

    // // Add the variable to the process definition
    // processDefinition.addProcessVariable(variable);
    // }
    // }
    // }
    // }
    void parseActivities(Node processNode, LaneInfo laneInfo, ScopeActivity processDefinition,
            ScopeActivity mainProcessDefinition)
            throws Exception {
        if (processNode.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        NodeList childNodes = processNode.getChildNodes();

        for (int j = 0; j < childNodes.getLength(); j++) {
            Node node = childNodes.item(j);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;
            String nodeName = element.getNodeName();

            switch (nodeName) {
                case "bpmn:laneSet":
                    parseLaneSet(element, laneInfo, processDefinition);
                    break;
                case "bpmn:extensionElements":
                    parseExtensionElements(element, processDefinition);
                    break;
                default:
                    parseNode(element, laneInfo, processDefinition, mainProcessDefinition);
                    break;
            }
        }
    }

    class LaneInfo {
        public HashMap<String, String> taskToLaneMap;
        public HashMap<String, Map<String, Integer>> laneCoordinate;
        public HashMap<String, Integer> laneYValue;

        LaneInfo() {
            this.taskToLaneMap = new HashMap<>();
            this.laneCoordinate = new HashMap<>();
            this.laneYValue = new HashMap<>();
        }
    }

    void parseActivities(Node processNode, ScopeActivity processDefinition) throws Exception {
        if (processNode.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        NodeList childNodes = processNode.getChildNodes();
        LaneInfo laneInfo = new LaneInfo();

        for (int j = 0; j < childNodes.getLength(); j++) {
            Node node = childNodes.item(j);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;
            String nodeName = element.getNodeName();

            switch (nodeName) {
                case "bpmn:laneSet":
                    parseLaneSet(element, laneInfo, processDefinition);
                    break;
                case "bpmn:extensionElements":
                    parseExtensionElements(element, processDefinition);
                    break;
                default:
                    parseNode(element, laneInfo, processDefinition, null);
                    break;
            }
        }
    }

    private void parseLaneSet(Element element, LaneInfo laneInfo, ScopeActivity processDefinition)
            throws Exception {
        laneInfo.laneCoordinate = extractLaneCoordinate(element);
        laneInfo.laneYValue = extractFirstYValueForBPMNDI(element);
        NodeList lanes = element.getElementsByTagName("bpmn:lane");
        for (int k = 0; k < lanes.getLength(); k++) {
            Node laneNode = lanes.item(k);
            if (laneNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element laneElement = (Element) laneNode;
            parseLane(laneElement, laneInfo, processDefinition);
        }
    }

    private void parseLane(Element laneElement, LaneInfo laneInfo, ScopeActivity processDefinition)
            throws Exception {
        NodeList flowNodeRefs = laneElement.getElementsByTagName("bpmn:flowNodeRef");
        for (int flowIndex = 0; flowIndex < flowNodeRefs.getLength(); flowIndex++) {
            Element flowNodeRef = (Element) flowNodeRefs.item(flowIndex);
            laneInfo.taskToLaneMap.put(flowNodeRef.getTextContent(), laneElement.getAttribute("name"));
        }

        String laneName = laneElement.getAttribute("name");
        Role role = parseRole(laneElement);
        role.setName(laneName);
        processDefinition.addRole(role);
    }

    private Role parseRole(Element laneElement) throws Exception {
        NodeList propertiesNodes = laneElement.getElementsByTagName("uengine:properties");
        for (int l = 0; l < propertiesNodes.getLength(); l++) {
            Node propertiesNode = propertiesNodes.item(l);
            if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList jsonNodes = ((Element) propertiesNode).getElementsByTagName("uengine:json");
                for (int m = 0; m < jsonNodes.getLength(); m++) {
                    Node jsonNode = jsonNodes.item(m);
                    if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE || jsonNode.getNodeType() == Node.TEXT_NODE
                            || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                        String jsonText = jsonNode.getTextContent();
                        return objectMapper.readValue(jsonText, Role.class);
                    }
                }
            }
        }
        return new Role(); // Return a default role if no JSON is found
    }

    public String getRoleNameInLocation(Map<String, Map<String, Integer>> laneInfo, int y) {
        for (Map.Entry<String, Map<String, Integer>> entry : laneInfo.entrySet()) {
            Map<String, Integer> dimensions = entry.getValue();
            int minY = (int) dimensions.get("minY");
            int maxY = (int) dimensions.get("maxY");
            if (y >= minY && y <= maxY) {
                return entry.getKey();
            }
        }
        return null;
    }

    public HashMap<String, Map<String, Integer>> extractLaneCoordinate(Element element) throws Exception {
        Document document = element.getOwnerDocument();
        NodeList lanes = document.getElementsByTagName("bpmndi:BPMNShape");
        HashMap<String, Map<String, Integer>> laneInfo = new HashMap<>();

        Map<String, String> laneIdToNameMap = new HashMap<>();
        NodeList bpmnLanes = document.getElementsByTagName("bpmn:lane");
        for (int i = 0; i < bpmnLanes.getLength(); i++) {
            Element lane = (Element) bpmnLanes.item(i);
            String laneId = lane.getAttribute("id");
            String laneName = lane.getAttribute("name");
            laneIdToNameMap.put(laneId, laneName);
        }

        for (int i = 0; i < lanes.getLength(); i++) {
            Element lane = (Element) lanes.item(i);
            String bpmnElement = lane.getAttribute("bpmnElement");
            if (laneIdToNameMap.containsKey(bpmnElement)) {
                Element bounds = (Element) lane.getElementsByTagName("dc:Bounds").item(0);
                int y = Integer.parseInt(bounds.getAttribute("y"));
                int height = Integer.parseInt(bounds.getAttribute("height"));
                int maxY = y + height;

                String name = laneIdToNameMap.get(bpmnElement);

                Map<String, Integer> dimensions = new HashMap<>();
                dimensions.put("minY", y);
                dimensions.put("maxY", maxY);

                laneInfo.put(name, dimensions);
            }
        }

        return laneInfo;
    }

    public HashMap<String, Integer> extractFirstYValueForBPMNDI(Element element) throws Exception {
        Document document = element.getOwnerDocument();
        NodeList shapes = document.getElementsByTagName("*");
        HashMap<String, Integer> yValues = new HashMap<>();

        for (int i = 0; i < shapes.getLength(); i++) {
            Element shape = (Element) shapes.item(i);
            if (shape.getNodeName().startsWith("bpmndi:")) {
                String bpmnElement = shape.getAttribute("bpmnElement");
                if (bpmnElement == null || bpmnElement.isEmpty()) {
                    continue;
                }
                Element bounds = (Element) shape.getElementsByTagName("dc:Bounds").item(0);
                if (bounds != null) {
                    int y = Integer.parseInt(bounds.getAttribute("y"));
                    yValues.put(bpmnElement, y);
                }
            }
        }

        return yValues;
    }

    private void parseExtensionElements(Element element, ScopeActivity processDefinition) throws Exception {
        NodeList extensionNodes = element.getChildNodes();
        for (int k = 0; k < extensionNodes.getLength(); k++) {
            Node extensionNode = extensionNodes.item(k);
            if (extensionNode.getNodeName().equals("uengine:properties")) {
                parseVariables(extensionNode, processDefinition);
            }
        }
    }

    private void parseVariables(Node extensionNode, ScopeActivity processDefinition) throws Exception {
        NodeList variableNodes = extensionNode.getChildNodes();
        for (int i = 0; i < variableNodes.getLength(); i++) {
            Node variableNode = variableNodes.item(i);
            if (variableNode.getNodeName().equals("uengine:variable")) {
                parseVariable((Element) variableNode, processDefinition);
            }
        }
    }

    private void parseVariable(Element variableElement, ScopeActivity processDefinition) throws Exception {
        String varName = variableElement.getAttribute("name");
        String type = variableElement.getAttribute("type");
        ProcessVariable variable = new ProcessVariable();

        NodeList jsonNodes = variableElement.getElementsByTagName("uengine:json");
        for (int m = 0; m < jsonNodes.getLength(); m++) {
            Node jsonNode = jsonNodes.item(m);
            if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE || jsonNode.getNodeType() == Node.TEXT_NODE
                    || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                String jsonText = jsonNode.getTextContent();
                variable = objectMapper.readValue(jsonText, ProcessVariable.class);
                variable.setName(varName);
                String javaType = convertToJavaType(type);
                variable.setType(Class.forName(javaType));
            }
        }

        processDefinition.addProcessVariable(variable);
    }

    private void parseNode(Element element, LaneInfo laneInfo, ScopeActivity processDefinition,
            ScopeActivity mainProcessDefinition)
            throws Exception {
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String nodeName = element.getNodeName();
        if (nodeName.contains(":")) {
            nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
        }

        switch (nodeName) {
            case "sequenceFlow":
                parseSequenceFlow(element, processDefinition);
                break;
            case "incoming":
            case "outgoing":
                // Skip processing for incoming or outgoing nodes
                break;
            default:
                parseActivity(element, laneInfo, processDefinition);
                break;
        }
    }

    private void parseSequenceFlow(Element element, ScopeActivity processDefinition) throws Exception {
        String id = element.getAttribute("id");
        String sourceRef = element.getAttribute("sourceRef");
        String targetRef = element.getAttribute("targetRef");
        SequenceFlow sequenceFlow = new SequenceFlow();

        NodeList propertiesNodes = element.getElementsByTagName("uengine:properties");
        for (int k = 0; k < propertiesNodes.getLength(); k++) {
            Node propertiesNode = propertiesNodes.item(k);
            if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList jsonNodes = ((Element) propertiesNode).getElementsByTagName("uengine:json");
                for (int l = 0; l < jsonNodes.getLength(); l++) {
                    Node jsonNode = jsonNodes.item(l);
                    if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE || jsonNode.getNodeType() == Node.TEXT_NODE
                            || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                        String jsonText = jsonNode.getTextContent();
                        SequenceFlow jsonSequenceFlow = objectMapper.readValue(jsonText, SequenceFlow.class);
                        BeanUtils.copyProperties(sequenceFlow, jsonSequenceFlow);
                    }
                }
            }
        }

        sequenceFlow.setTracingTag(id);
        sequenceFlow.setSourceRef(sourceRef);
        sequenceFlow.setTargetRef(targetRef);

        processDefinition.addSequenceFlow(sequenceFlow);
    }

    private void parseActivity(Element element, LaneInfo laneInfo, ScopeActivity processDefinition)
            throws Exception {
        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String nodeName = element.getNodeName();
        if (nodeName.contains(":")) {
            nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
        }

        String className = nodeName.substring(0, 1).toUpperCase() + nodeName.substring(1);
        String fullClassName = parseFullClassName(element, className);

        Class<?> clazz = Class.forName(fullClassName);
        Activity task = (Activity) clazz.getDeclaredConstructor().newInstance();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                if ("bpmn:extensionElements".equals(childElement.getTagName())) {
                    NodeList propertiesNodes = childElement.getElementsByTagName("uengine:properties");
                    for (int j = 0; j < propertiesNodes.getLength(); j++) {
                        Node propertiesNode = propertiesNodes.item(j);
                        if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element propertiesElement = (Element) propertiesNode;
                            NodeList jsonNodes = propertiesElement.getElementsByTagName("uengine:json");
                            for (int k = 0; k < jsonNodes.getLength(); k++) {
                                Node jsonNode = jsonNodes.item(k);
                                if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
                                        || jsonNode.getNodeType() == Node.TEXT_NODE
                                        || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                                    String jsonText = jsonNode.getTextContent();
                                    if (jsonText.contains("_type")) {
                                        clazz = Activity.class;
                                    }

                                    Object jsonObject = objectMapper.readValue(jsonText, clazz);
                                    if (className.equals("SubProcess") && jsonObject instanceof SubProcess) {
                                        task = (SubProcess) jsonObject;
                                    } else if (className.equals("BoundaryEvent")) {
                                        task = (Event) jsonObject;
                                        ((Event) task).setAttachedToRef(element.getAttribute("attachedToRef"));
                                    } else {
                                        task = (Activity) jsonObject;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (task instanceof SubProcess) {
            parseActivities(element, laneInfo, (SubProcess) task, processDefinition);
        }

        if (task instanceof HumanActivity) {
            if (((HumanActivity) task).getRole() == null) {
                Role role = createRoleInLane(laneInfo, id);
                ((HumanActivity) task).setRole(role);
            }

        }

        task.setTracingTag(id);
        task.setName(name);
        processDefinition.addChildActivity(task);

    }

    private String parseFullClassName(Element element, String className) {
        String fullClassName;
        if (className.equals("Task")) {
            fullClassName = "org.uengine.kernel.DefaultActivity";
        } else if (className.equals("UserTask") || className.equals("ManualTask")) {
            fullClassName = "org.uengine.kernel.HumanActivity";
        } else if (className.equals("ScriptTask")) {
            fullClassName = "org.uengine.kernel.ScriptActivity";
        } else if (className.equals("BoundaryEvent")) {
            List<String> eventTypes = Arrays.asList("timer", "signal", "error",
                    "message");
            fullClassName = eventTypes.stream()
                    .filter(eventType -> element.getElementsByTagName(eventType +
                            "EventDefinition")
                            .getLength() > 0
                            || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
                                    .getLength() > 0)
                    .findFirst()
                    .map(eventType -> "org.uengine.kernel.bpmn."
                            + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
                            + "Event")
                    .orElse(null); // 혹은 기본값을 설정하거나 예외를 던질 수 있습니다.

        } else if (className.equals("IntermediateCatchEvent")) {
            List<String> eventTypes = Arrays.asList("timer", "signal", "error",
                    "message");
            fullClassName = eventTypes.stream()
                    .filter(eventType -> element.getElementsByTagName(eventType +
                            "EventDefinition")
                            .getLength() > 0
                            || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
                                    .getLength() > 0)
                    .findFirst()
                    .map(eventType -> "org.uengine.kernel.bpmn."
                            + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
                            + className)
                    .orElse(null);
        } else if (className.equals("IntermediateThrowEvent")) {
            List<String> eventTypes = Arrays.asList("timer", "signal", "error",
                    "message");
            fullClassName = eventTypes.stream()
                    .filter(eventType -> element.getElementsByTagName(eventType +
                            "EventDefinition")
                            .getLength() > 0
                            || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
                                    .getLength() > 0)
                    .findFirst()
                    .map(eventType -> "org.uengine.kernel.bpmn."
                            + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
                            + className)
                    .orElse(null);
        } else if (className.equals("StartEvent")) {
            List<String> eventTypes = Arrays.asList("timer", "signal", "error",
                    "message");
            fullClassName = eventTypes.stream()
                    .filter(eventType -> element.getElementsByTagName(eventType +
                            "EventDefinition")
                            .getLength() > 0
                            || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
                                    .getLength() > 0)
                    .findFirst()
                    .map(eventType -> "org.uengine.kernel.bpmn."
                            + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
                            + className)
                    .orElse("org.uengine.kernel.bpmn." + className);
        } else {
            fullClassName = "org.uengine.kernel.bpmn." + className;
        }

        return fullClassName;
    }
    // void parseActivities(Node processNode, ScopeActivity processDefinition)
    // throws Exception {

    // if (processNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList childNodes = processNode.getChildNodes();
    // HashMap<String, String> taskToLaneMap = new HashMap<>();

    // for (int j = 0; j < childNodes.getLength(); j++) {
    // Node node = childNodes.item(j);
    // if (node.getNodeType() == Node.ELEMENT_NODE) {
    // Element element = (Element) node;
    // String nodeName = element.getNodeName();

    // // LaneSet은 무시
    // if (nodeName.equals("bpmn:laneSet")) {
    // // String laneSetId = element.getAttribute("id");
    // NodeList lanes = element.getElementsByTagName("bpmn:lane");
    // for (int k = 0; k < lanes.getLength(); k++) {
    // Node laneNode = lanes.item(k);
    // if (laneNode.getNodeType() == Node.ELEMENT_NODE) {
    // Element laneElement = (Element) laneNode;
    // NodeList flowNodeRefs = laneElement.getElementsByTagName("bpmn:flowNodeRef");
    // for (int flowIndex = 0; flowIndex < flowNodeRefs.getLength(); flowIndex++) {
    // Element flowNodeRef = (Element) flowNodeRefs.item(flowIndex);
    // taskToLaneMap.put(flowNodeRef.getTextContent(),
    // laneElement.getAttribute("name"));
    // }
    // // String laneId = laneElement.getAttribute("id");
    // String laneName = laneElement.getAttribute("name");
    // Role role = new Role();

    // NodeList propertiesNodes =
    // laneElement.getElementsByTagName("uengine:properties");
    // for (int l = 0; l < propertiesNodes.getLength(); l++) {
    // Node propertiesNode = propertiesNodes.item(l);
    // if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList jsonNodes = ((Element) propertiesNode)
    // .getElementsByTagName("uengine:json");
    // for (int m = 0; m < jsonNodes.getLength(); m++) {
    // Node jsonNode = jsonNodes.item(m);
    // if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
    // || jsonNode.getNodeType() == Node.TEXT_NODE
    // || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
    // String jsonText = jsonNode.getTextContent();
    // try {
    // Role roleContext = objectMapper.readValue(jsonText, Role.class);
    // BeanUtils.copyProperties(role, roleContext);
    // } catch (Exception e) {
    // throw new RuntimeException("Error parsing lane JSON", e);
    // }
    // }
    // }
    // }
    // }
    // role.setName(laneName);

    // processDefinition.addRole(role);
    // }
    // }
    // continue;
    // }
    // if (nodeName.equals("bpmn:extensionElements")) {
    // // TODO: Process Variable Parse
    // NodeList extensionNodes = element.getChildNodes();
    // for (int k = 0; k < extensionNodes.getLength(); k++) {
    // Node extensionNode = extensionNodes.item(k);
    // if (extensionNode.getNodeName().equals("uengine:properties")) {
    // NodeList variableNodes = extensionNode.getChildNodes();
    // for (int i = 0; i < variableNodes.getLength(); i++) {
    // Node variableNode = variableNodes.item(i);
    // if (variableNode.getNodeName().equals("uengine:variable")) {
    // Element variableElement = (Element) variableNode;
    // String varName = variableElement.getAttribute("name");
    // String type = variableElement.getAttribute("type");

    // // Create a new ProcessVariable instance
    // ProcessVariable variable = new ProcessVariable();

    // if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList jsonNodes = ((Element) variableNode)
    // .getElementsByTagName("uengine:json");
    // for (int m = 0; m < jsonNodes.getLength(); m++) {
    // Node jsonNode = jsonNodes.item(m);
    // if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
    // || jsonNode.getNodeType() == Node.TEXT_NODE
    // || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
    // String jsonText = jsonNode.getTextContent();
    // try {
    // variable = objectMapper.readValue(jsonText,
    // ProcessVariable.class);

    // } catch (Exception e) {
    // throw new RuntimeException("Error parsing lane JSON", e);
    // }
    // }
    // }
    // }

    // variable.setName(varName);
    // String javaType = convertToJavaType(type);
    // try {
    // // Assuming the type attribute is a fully qualified class name
    // variable.setType(Class.forName(javaType));
    // } catch (ClassNotFoundException e) {
    // throw new RuntimeException("Class not found for type: " + type);
    // }

    // // Add the variable to the process definition
    // processDefinition.addProcessVariable(variable);
    // }
    // }
    // }
    // }
    // } else {
    // String id = element.getAttribute("id");
    // String name = element.getAttribute("name");
    // nodeName = element.getNodeName();
    // if (nodeName.contains(":")) {
    // nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
    // }
    // if (nodeName.equals("sequenceFlow")) {
    // String sourceRef = element.getAttribute("sourceRef");
    // String targetRef = element.getAttribute("targetRef");
    // SequenceFlow sequenceFlow = new SequenceFlow();

    // // JSON parsing and property setting logic for sequenceFlow
    // NodeList propertiesNodes =
    // element.getElementsByTagName("uengine:properties");
    // for (int k = 0; k < propertiesNodes.getLength(); k++) {
    // Node propertiesNode = propertiesNodes.item(k);
    // if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList jsonNodes = ((Element) propertiesNode)
    // .getElementsByTagName("uengine:json");
    // for (int l = 0; l < jsonNodes.getLength(); l++) {
    // Node jsonNode = jsonNodes.item(l);
    // if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
    // || jsonNode.getNodeType() == Node.TEXT_NODE
    // || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
    // String jsonText = jsonNode.getTextContent();
    // try {
    // // Assuming the JSON structure matches the SequenceFlow class structure
    // SequenceFlow jsonSequenceFlow = objectMapper.readValue(jsonText,
    // SequenceFlow.class);
    // // Use the JSON object to set properties on the SequenceFlow object
    // BeanUtils.copyProperties(sequenceFlow, jsonSequenceFlow);
    // } catch (Exception e) {
    // throw new RuntimeException("Error parsing sequenceFlow JSON", e);
    // }
    // }
    // }
    // }
    // }

    // sequenceFlow.setTracingTag(id);
    // sequenceFlow.setSourceRef(sourceRef);
    // sequenceFlow.setTargetRef(targetRef);

    // processDefinition.addSequenceFlow(sequenceFlow);
    // } else {
    // if (nodeName.equals("incoming") || nodeName.equals("outgoing")) {
    // // Skip processing for incoming or outgoing nodes
    // continue;
    // }

    // String className = nodeName.substring(0, 1).toUpperCase() +
    // nodeName.substring(1);

    // String fullClassName = null;
    // if (className.equals("Task")) {
    // fullClassName = "org.uengine.kernel.DefaultActivity";
    // } else if (className.equals("UserTask") || className.equals("ManualTask")) {
    // fullClassName = "org.uengine.kernel.HumanActivity";
    // } else if (className.equals("ScriptTask")) {
    // fullClassName = "org.uengine.kernel.ScriptActivity";
    // } else if (className.equals("BoundaryEvent")) {
    // List<String> eventTypes = Arrays.asList("timer", "signal", "error",
    // "message");
    // fullClassName = eventTypes.stream()
    // .filter(eventType -> element.getElementsByTagName(eventType +
    // "EventDefinition")
    // .getLength() > 0
    // || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
    // .getLength() > 0)
    // .findFirst()
    // .map(eventType -> "org.uengine.kernel.bpmn."
    // + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
    // + "Event")
    // .orElse(null); // 혹은 기본값을 설정하거나 예외를 던질 수 있습니다.

    // } else {
    // fullClassName = "org.uengine.kernel.bpmn." + className;
    // }

    // try {
    // Class<?> clazz = Class.forName(fullClassName);
    // Object instance = clazz.getDeclaredConstructor().newInstance();
    // Activity task = (Activity) instance;

    // // if ("SubProcess".equals(className)) {
    // // parseActivities(element, (SubProcess) task);
    // // }

    // // JSON parsing and property setting logic
    // NodeList propertiesNodes =
    // element.getElementsByTagName("uengine:properties");
    // for (int k = 0; k < propertiesNodes.getLength(); k++) {
    // Node propertiesNode = propertiesNodes.item(k);
    // if (propertiesNode.getParentNode().getParentNode().getNodeName()
    // .equals(element.getNodeName())) {
    // if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
    // NodeList jsonNodes = ((Element) propertiesNode)
    // .getElementsByTagName("uengine:json");
    // for (int l = 0; l < jsonNodes.getLength(); l++) {
    // Node jsonNode = jsonNodes.item(l);
    // if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
    // || jsonNode.getNodeType() == Node.TEXT_NODE
    // || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
    // String jsonText = jsonNode.getTextContent();

    // Class castingClass = clazz;
    // if (jsonText.contains("_type")) {
    // castingClass = Activity.class;
    // }

    // Object jsonObject = objectMapper.readValue(jsonText, castingClass);
    // // Use the JSON object to set properties on the Activity object
    // // BeanUtils.copyProperties(task, jsonObject)d;

    // if (className.equals("BoundaryEvent")) {
    // task = (Event) jsonObject;
    // ((Event) task)
    // .setAttachedToRef(
    // element.getAttribute("attachedToRef"));
    // } else {

    // task = (Activity) jsonObject;
    // }

    // }
    // }
    // }
    // } else {
    // // subProcess의 childActivity에 넣기
    // // parseActivities(propertiesNode, (SubProcess) task);
    // Node subNode = propertiesNode.getParentNode().getParentNode();
    // String subId = subNode.getAttributes().getNamedItem("id").getTextContent();
    // String subName =
    // subNode.getAttributes().getNamedItem("name").getTextContent();
    // String subNodeName = subNode.getNodeName();
    // if (subNodeName.contains(":")) {
    // subNodeName = subNodeName.substring(subNodeName.indexOf(":") + 1);
    // }
    // String subClassName = subNodeName.substring(0, 1).toUpperCase()
    // + subNodeName.substring(1);

    // String fullSubClassName = null;
    // if (subClassName.equals("Task")) {
    // fullSubClassName = "org.uengine.kernel.DefaultActivity";
    // } else if (subClassName.equals("UserTask")
    // || subClassName.equals("ManualTask")) {
    // fullSubClassName = "org.uengine.kernel.HumanActivity";
    // } else if (subClassName.equals("ScriptTask")) {
    // fullSubClassName = "org.uengine.kernel.ScriptActivity";
    // } else if (subClassName.equals("BoundaryEvent")) {

    // List<String> eventTypes = Arrays.asList("timer", "signal");

    // fullSubClassName = eventTypes.stream()
    // .filter(eventType -> element
    // .getElementsByTagName(eventType + "EventDefinition")
    // .getLength() > 0
    // || element
    // .getElementsByTagName(
    // "bpmn:" + eventType + "EventDefinition")
    // .getLength() > 0)
    // .findFirst()
    // .map(eventType -> "org.uengine.kernel.bpmn."
    // + Character.toUpperCase(eventType.charAt(0))
    // + eventType.substring(1)
    // + "Event")
    // .orElse(null); // 혹은 기본값을 설정하거나 예외를 던질 수 있습니다.

    // } else {
    // fullSubClassName = "org.uengine.kernel.bpmn." + subClassName;
    // }
    // Class<?> subClazz = Class.forName(fullSubClassName);
    // Object subInstance = subClazz.getDeclaredConstructor().newInstance();
    // Activity subTask = (Activity) subInstance;
    // NodeList jsonNodes = ((Element) propertiesNode)
    // .getElementsByTagName("uengine:json");
    // for (int l = 0; l < jsonNodes.getLength(); l++) {
    // Node jsonNode = jsonNodes.item(l);
    // if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
    // || jsonNode.getNodeType() == Node.TEXT_NODE
    // || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
    // String jsonText = jsonNode.getTextContent();

    // Class castingClass = subClazz;
    // if (jsonText.contains("_type")) {
    // castingClass = Activity.class;
    // }

    // Object jsonObject = objectMapper.readValue(jsonText, castingClass);
    // // Use the JSON object to set properties on the Activity object
    // // BeanUtils.copyProperties(task, jsonObject)d;

    // if (subClassName.equals("BoundaryEvent")) {
    // subTask = (Event) jsonObject;
    // ((Event) subTask)
    // .setAttachedToRef(
    // element.getAttribute("attachedToRef"));
    // } else {
    // subTask = (Activity) jsonObject;
    // }

    // }
    // }
    // subTask.setTracingTag(subId);
    // subTask.setName(subName);
    // // subTask.setName
    // ((SubProcess) task).addChildActivity(subTask);

    // }

    // }

    // if (task instanceof HumanActivity) {
    // Role role = createRoleInLane(taskToLaneMap, id);
    // ((HumanActivity) task).setRole(role);
    // }
    // task.setTracingTag(id);
    // task.setName(name);

    // // if ("SubProcess".equals(className)) {
    // // parseActivities(node, (SubProcess) task);
    // // }

    // processDefinition.addChildActivity(task);

    // } catch (ClassNotFoundException | IllegalAccessException |
    // InstantiationException
    // | NoSuchMethodException | InvocationTargetException e) {
    // throw new RuntimeException("Error parsing task JSON:" + e.getMessage(), e);
    // }

    // }
    // }
    // }
    // }
    // }
    // }

    public Role createRoleInLane(LaneInfo laneInfo, String id) {
        Role role = new Role();
        String laneRoleName = laneInfo.taskToLaneMap.get(id);
        if (laneRoleName == null || laneRoleName.equals("")) {
            int yValue = laneInfo.laneYValue.get(id);
            laneRoleName = getRoleNameInLocation(laneInfo.laneCoordinate, yValue);
        }
        role.setName(laneRoleName);
        return role;
    }

    public ProcessDefinition parse(String xml) throws Exception {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // XML Tag Inner $type Cast to type
            xml = xml.replace("$type", "type");
            Document document = builder.parse(new InputSource(new StringReader(xml)));
    
            ProcessDefinition processDefinition = new ProcessDefinition();
            // // Process variables parsing
            // NodeList dataNodes = document.getElementsByTagName("uengine:data");
            // for (int i = 0; i < dataNodes.getLength(); i++) {
            // Node dataNode = dataNodes.item(i);
            // parseProcessVariables(dataNode, processDefinition);
    
            // }
    
            // All gateway types handling code
            NodeList bpmnProcessNodes = document.getElementsByTagName("bpmn:process");
            NodeList processNodes = document.getElementsByTagName("process");
            if (bpmnProcessNodes.getLength() > 0) {
                processNodes = bpmnProcessNodes;
            } else if (processNodes.getLength() == 0) {
                processNodes = bpmnProcessNodes; // Fallback to bpmn:process if both are empty
            }
            if (processNodes.getLength() == 0) {
                throw new RuntimeException("No process tag found in the XML");
            }
    
            for (int i = 0; i < processNodes.getLength(); i++) {
                Node processNode = processNodes.item(i);
                boolean isExecutable = Boolean
                        .parseBoolean(processNode.getAttributes().getNamedItem("isExecutable").getTextContent());
                if (isExecutable) {
                    parseActivities(processNode, processDefinition);
                }
            }
    
            processDefinition.afterDeserialization();
    
            return processDefinition;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing BPMN XML", e);
        }
    }
}
