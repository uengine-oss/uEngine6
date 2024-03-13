package org.uengine.five.serializers;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.bpmn.SequenceFlow;
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

    public ProcessDefinition parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        ProcessDefinition processDefinition = new ProcessDefinition();

        // Process variables parsing
        NodeList dataNodes = document.getElementsByTagName("uengine:data");
        for (int i = 0; i < dataNodes.getLength(); i++) {
            Node dataNode = dataNodes.item(i);
            if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList variableNodes = dataNode.getChildNodes();
                for (int j = 0; j < variableNodes.getLength(); j++) {
                    Node variableNode = variableNodes.item(j);
                    if (variableNode.getNodeType() == Node.ELEMENT_NODE
                            && variableNode.getNodeName().equals("uengine:variable")) {
                        Element variableElement = (Element) variableNode;
                        String name = variableElement.getAttribute("name");
                        String type = variableElement.getAttribute("type");

                        // Create a new ProcessVariable instance
                        ProcessVariable variable = new ProcessVariable();
                        variable.setName(name);
                        try {
                            // Assuming the type attribute is a fully qualified class name
                            variable.setType(Class.forName(type));
                        } catch (ClassNotFoundException e) {
                            System.err.println("Class not found for type: " + type);
                            continue; // Skip this variable if the class type is not found
                        }

                        // Add the variable to the process definition
                        processDefinition.addProcessVariable(variable);
                    }
                }
            }
        }

        // All gateway types handling code
        NodeList processNodes = document.getElementsByTagName("bpmn:process");
        for (int i = 0; i < processNodes.getLength(); i++) {
            Node processNode = processNodes.item(i);
            if (processNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList childNodes = processNode.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node node = childNodes.item(j);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String nodeName = element.getNodeName();
                        try {
                            String id = element.getAttribute("id");
                            String name = element.getAttribute("name");
                            nodeName = element.getNodeName();
                            if (nodeName.contains(":")) {
                                nodeName = nodeName.substring(nodeName.indexOf(":") + 1);
                            }

                            if (nodeName.equals("sequenceFlow")) {
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
                            } else
                                fullClassName = "org.uengine.kernel.bpmn." + className;

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
                                            Object jsonObject = objectMapper.readValue(jsonText, task.getClass());
                                            // Use the JSON object to set properties on the Activity object
                                            BeanUtils.copyProperties(task, jsonObject);
                                        }
                                    }
                                }
                            }

                            task.setTracingTag(id);
                            task.setName(name);

                            processDefinition.addChildActivity(task);

                        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                                | NoSuchMethodException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        NodeList sequenceFlowNodes = document.getElementsByTagName("bpmn:sequenceFlow");

        for (int i = 0; i < sequenceFlowNodes.getLength(); i++) {
            Node node = sequenceFlowNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String id = element.getAttribute("id");
                String sourceRef = element.getAttribute("sourceRef");
                String targetRef = element.getAttribute("targetRef");
                SequenceFlow sequenceFlow = new SequenceFlow();
                sequenceFlow.setTracingTag(id);
                sequenceFlow.setSourceRef(sourceRef);
                sequenceFlow.setTargetRef(targetRef);

                // JSON parsing and property setting logic for sequenceFlow
                NodeList propertiesNodes = element.getElementsByTagName("uengine:properties");
                for (int k = 0; k < propertiesNodes.getLength(); k++) {
                    Node propertiesNode = propertiesNodes.item(k);
                    if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList jsonNodes = ((Element) propertiesNode).getElementsByTagName("uengine:json");
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
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                processDefinition.addSequenceFlow(sequenceFlow);
            }
        }

        processDefinition.afterDeserialization();

        return processDefinition;
    }
}
