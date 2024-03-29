package org.uengine.five.serializers;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ScopeActivity;
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

public class BpmnXMLParser {

    static ObjectMapper objectMapper = createTypedJsonObjectMapper();

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

    void parseActivities(Node processNode, ScopeActivity processDefinition) throws Exception {

        if (processNode.getNodeType() == Node.ELEMENT_NODE) {
            NodeList childNodes = processNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String nodeName = element.getNodeName();
                    if (nodeName.equals("bpmn:extensionElements")) {
                        // TODO: Process Variable Parse
                        NodeList extensionNodes = element.getChildNodes();
                        for (int k = 0; k < extensionNodes.getLength(); k++) {
                            Node extensionNode = extensionNodes.item(k);
                            if (extensionNode.getNodeName().equals("uengine:properties")) {
                                NodeList variableNodes = extensionNode.getChildNodes();
                                for (int i = 0; i < variableNodes.getLength(); i++) {
                                    Node variableNode = variableNodes.item(i);
                                    if (variableNode.getNodeName().equals("uengine:variable")) {
                                        Element variableElement = (Element) variableNode;
                                        String varName = variableElement.getAttribute("name");
                                        String type = variableElement.getAttribute("type");

                                        // Create a new ProcessVariable instance
                                        ProcessVariable variable = new ProcessVariable();
                                        variable.setName(varName);
                                        try {
                                            // Assuming the type attribute is a fully qualified class name
                                            variable.setType(Class.forName(type));
                                        } catch (ClassNotFoundException e) {
                                            throw new RuntimeException("Class not found for type: " + type);
                                        }

                                        // Add the variable to the process definition
                                        processDefinition.addProcessVariable(variable);
                                    }
                                }
                            }
                        }
                    } else {
                        String id = element.getAttribute("id");
                        String name = element.getAttribute("name");
                        nodeName = element.getNodeName();
                        if (nodeName.contains(":")) {
                            nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
                        }
                        if (nodeName.equals("sequenceFlow")) {
                            String sourceRef = element.getAttribute("sourceRef");
                            String targetRef = element.getAttribute("targetRef");
                            SequenceFlow sequenceFlow = new SequenceFlow();

                            // JSON parsing and property setting logic for sequenceFlow
                            NodeList propertiesNodes = element.getElementsByTagName("uengine:properties");
                            for (int k = 0; k < propertiesNodes.getLength(); k++) {
                                Node propertiesNode = propertiesNodes.item(k);
                                if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                                    NodeList jsonNodes = ((Element) propertiesNode)
                                            .getElementsByTagName("uengine:json");
                                    for (int l = 0; l < jsonNodes.getLength(); l++) {
                                        Node jsonNode = jsonNodes.item(l);
                                        if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
                                                || jsonNode.getNodeType() == Node.TEXT_NODE
                                                || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                                            String jsonText = jsonNode.getTextContent();
                                            try {
                                                // Assuming the JSON structure matches the SequenceFlow class structure
                                                SequenceFlow jsonSequenceFlow = objectMapper.readValue(jsonText,
                                                        SequenceFlow.class);
                                                // Use the JSON object to set properties on the SequenceFlow object
                                                BeanUtils.copyProperties(sequenceFlow, jsonSequenceFlow);
                                            } catch (Exception e) {
                                                throw new RuntimeException("Error parsing sequenceFlow JSON", e);
                                            }
                                        }
                                    }
                                }
                            }

                            sequenceFlow.setTracingTag(id);
                            sequenceFlow.setSourceRef(sourceRef);
                            sequenceFlow.setTargetRef(targetRef);

                            processDefinition.addSequenceFlow(sequenceFlow);
                        } else {
                            if (nodeName.equals("incoming") || nodeName.equals("outgoing")) {
                                // Skip processing for incoming or outgoing nodes
                                continue;
                            }

                            String className = nodeName.substring(0, 1).toUpperCase() + nodeName.substring(1);

                            String fullClassName = null;

                            if (className.equals("Task")) {
                                fullClassName = "org.uengine.kernel.DefaultActivity";
                            } else if (className.equals("UserTask")) {
                                fullClassName = "org.uengine.kernel.HumanActivity";
                            } else if (className.equals("ScriptTask")) {
                                fullClassName = "org.uengine.kernel.ScriptActivity";
                            } else if (className.equals("BoundaryEvent")) {

                                List<String> eventTypes = Arrays.asList("timer", "signal");

                                fullClassName = eventTypes.stream()
                                        .filter(eventType -> element.getElementsByTagName(eventType + "EventDefinition")
                                                .getLength() > 0
                                                || element.getElementsByTagName("bpmn:" + eventType + "EventDefinition")
                                                        .getLength() > 0)
                                        .findFirst()
                                        .map(eventType -> "org.uengine.kernel.bpmn."
                                                + Character.toUpperCase(eventType.charAt(0)) + eventType.substring(1)
                                                + "Event")
                                        .orElse(null); // 혹은 기본값을 설정하거나 예외를 던질 수 있습니다.

                            } else
                                fullClassName = "org.uengine.kernel.bpmn." + className;

                            try {
                                Class<?> clazz = Class.forName(fullClassName);
                                Object instance = clazz.getDeclaredConstructor().newInstance();

                                Activity task = (Activity) instance;
                                // JSON parsing and property setting logic
                                NodeList propertiesNodes = element.getElementsByTagName("uengine:properties");
                                for (int k = 0; k < propertiesNodes.getLength(); k++) {
                                    Node propertiesNode = propertiesNodes.item(k);
                                    if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                                        NodeList jsonNodes = ((Element) propertiesNode)
                                                .getElementsByTagName("uengine:json");
                                        for (int l = 0; l < jsonNodes.getLength(); l++) {
                                            Node jsonNode = jsonNodes.item(l);
                                            if (jsonNode.getNodeType() == Node.CDATA_SECTION_NODE
                                                    || jsonNode.getNodeType() == Node.TEXT_NODE
                                                    || jsonNode.getNodeType() == Node.ELEMENT_NODE) {
                                                String jsonText = jsonNode.getTextContent();
                                                Class castingClass = clazz;
                                                if (jsonText.contains("_type")) {
                                                    castingClass = Activity.class;
                                                }
                                                Object jsonObject = objectMapper.readValue(jsonText, castingClass);
                                                // Use the JSON object to set properties on the Activity object
                                                // BeanUtils.copyProperties(task, jsonObject);
                                                task = (Activity) jsonObject;
                                            }
                                        }
                                    }
                                }

                                task.setTracingTag(id);
                                task.setName(name);

                                if ("SubProcess".equals(className)) {

                                    parseActivities(node, (SubProcess) task);
                                }

                                processDefinition.addChildActivity(task);

                            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                                    | NoSuchMethodException | InvocationTargetException e) {
                                throw new RuntimeException("Error parsing task JSON:" + e.getMessage(), e);
                            }

                        }
                    }
                }
            }
        }
    }

    public ProcessDefinition parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
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
            parseActivities(processNode, processDefinition);
        }

        processDefinition.afterDeserialization();

        return processDefinition;
    }
}
