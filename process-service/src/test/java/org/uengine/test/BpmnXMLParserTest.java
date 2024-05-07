package org.uengine.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.uengine.contexts.HtmlFormContext;
import org.uengine.contexts.MappingContext;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.FormActivity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.Role;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.SubProcess;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Paths;

public class BpmnXMLParserTest {
    @Test
    public void testParse() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<bpmn:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"sample-definitions\">\n"
                +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "    <bpmn:task id=\"task1\" name=\"Sample Task\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            Map<String, Activity> tasks = processDefinition.getWholeChildActivities();
            assertEquals(2, tasks.size());
            assertTrue(tasks.containsKey("task1"));
            assertEquals("Sample Task", ((Activity) tasks.get("task1")).getName());
        } catch (Exception e) {
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseSequenceFlow() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<bpmn:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"sample-definitions\">\n"
                +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "    <bpmn:task id=\"task1\" name=\"First Task\" />\n" +
                "    <bpmn:task id=\"task2\" name=\"Second Task\" />\n" +
                "    <bpmn:sequenceFlow id=\"flow1\" sourceRef=\"task1\" targetRef=\"task2\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            assertEquals("The process should have exactly one sequence flow.", 1,
                    processDefinition.getSequenceFlows().size());
            assertTrue("The process should contain a sequence flow with ID 'flow1'.",
                    processDefinition.getSequenceFlows().stream()
                            .anyMatch(flow -> "flow1".equals(flow.getTracingTag())));
            SequenceFlow flow1 = processDefinition.getSequenceFlows().stream()
                    .filter(flow -> "flow1".equals(flow.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Sequence flow 'flow1' should not be null", flow1);
            assertEquals("The sourceRef of sequence flow 'flow1' should be 'task1'", "task1", flow1.getSourceRef());
        } catch (Exception e) {
            fail("Parsing sequence flow failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseSequenceFlowWithCondition() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"sample-definitions\">\n"
                +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "    <bpmn:userTask id=\"task1\" name=\"User Task\">\n" +
                "      <bpmn:outgoing>Flow_1</bpmn:outgoing>\n" +
                "    </bpmn:userTask>\n" +
                "    <bpmn:task id=\"task2\" name=\"Task 2\">\n" +
                "      <bpmn:incoming>Flow_1</bpmn:incoming>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"task1\" targetRef=\"task2\">\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>\n" +
                "          {\n" +
                "            \"condition\": {\n" +
                "              \"_type\": \"org.uengine.kernel.Evaluate\",\n" +
                "              \"key\": \"troubleType\", \n" +
                "              \"value\": \"sw\"\n" +
                "            }\n" +
                "          }\n" +
                "        </uengine:json>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:sequenceFlow>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            SequenceFlow flow1 = processDefinition.getSequenceFlows().stream()
                    .filter(flow -> "Flow_1".equals(flow.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Sequence flow 'Flow_1' should not be null", flow1);

            // Assuming there's a method in SequenceFlow to get condition
            // Replace 'getCondition' with the actual method name
            assertNotNull("Condition should not be null", flow1.getCondition());
            assertEquals("Condition key should be 'troubleType'", "troubleType",
                    ((Evaluate) flow1.getCondition()).getKey());
            assertEquals("Condition value should be 'sw'", "sw", ((Evaluate) flow1.getCondition()).getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Parsing sequence flow with condition failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseGatewaysAndEvents() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<bpmn:definitions xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"sample-definitions\">\n"
                +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "    <bpmn:exclusiveGateway id=\"gateway1\" name=\"Exclusive Gateway\" />\n" +
                "    <bpmn:startEvent id=\"startEvent1\" name=\"Start Event\" />\n" +
                "    <bpmn:endEvent id=\"endEvent1\" name=\"End Event\" />\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            Map<String, Activity> flowElements = processDefinition.getWholeChildActivities();

            // Test for Gateway
            assertTrue("The process should contain a gateway with ID 'gateway1'.",
                    flowElements.containsKey("gateway1"));
            assertEquals("Exclusive Gateway", ((Gateway) flowElements.get("gateway1")).getName());

            // Test for Start Event
            assertTrue("The process should contain a start event with ID 'startEvent1'.",
                    flowElements.containsKey("startEvent1"));
            assertEquals("Start Event", ((Event) flowElements.get("startEvent1")).getName());

            // Test for End Event
            assertTrue("The process should contain an end event with ID 'endEvent1'.",
                    flowElements.containsKey("endEvent1"));
            assertEquals("End Event", ((Event) flowElements.get("endEvent1")).getName());
        } catch (Exception e) {
            e.printStackTrace();

            fail("Parsing gateways and events failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseUserTaskWithJsonProperties() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" id=\"sample-definitions\">\n"
                +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "    <bpmn:userTask id=\"task1\" name=\"User Task\">\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>\n" +
                "          {\"role\": {\"name\": \"initiator\"}, \"parameters\": [{\"argument\":{\"text\":\"test\"}, \"variable\":{\"name\": \"string\"}, \"direction\": \"out\"}]}\n"
                +
                "        </uengine:json>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:userTask>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            HumanActivity userTask = (HumanActivity) processDefinition.getChildActivities().get(0);
            // Assuming there's a method in Activity to get parameters or properties
            // Replace 'getParameters' with the actual method name
            ParameterContext[] parameters = userTask.getParameters();
            assertNotNull("Parameters should not be null", parameters);
            boolean containsTest = Arrays.stream(parameters)
                    .anyMatch(param -> "test".equals(param.getArgument().getText()));
            assertTrue("Parameters should contain 'test'", containsTest);
            // Additional assertions can be added based on the expected properties

            Role role = userTask.getRole();
            assertNotNull("Role should not be null", role);
            assertEquals("initiator", role.getName());
        } catch (Exception e) {
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    // @Test
    public void testParseProcessVariables() throws Exception {
        String xml = "<definitions>" +
                "  <bpmn:process id=\"process1\" name=\"Sample Process\">\n" +
                "<bpmn:extensionElements>\n" +
                "<uengine:properties>\n" +
                "<uengine:variable name=\"variable1\" type=\"java.lang.String\"/>\n" +
                "<uengine:variable name=\"variable2\" type=\"java.lang.Integer\"/>\n" +
                "</uengine:properties>\n" +
                "</bpmn:extensionElements>\n" +
                "  </bpmn:process>\n" +
                "</definitions>";

        BpmnXMLParser parser = new BpmnXMLParser();
        ProcessDefinition processDefinition = parser.parse(xml);

        assertTrue("Should have 2 process variable", processDefinition.getProcessVariables().length == 2);

        ProcessVariable variable1 = processDefinition.getProcessVariable("variable1");
        assertEquals("Variable1 should be of type String", "java.lang.String", variable1.getType().getName());

        ProcessVariable variable2 = processDefinition.getProcessVariable("variable2");
        assertEquals("Variable2 should be of type Integer", "java.lang.Integer", variable2.getType().getName());
    }

    @Test
    public void testParseSubProcessWithNestedActivities() {
        BpmnXMLParser parser = new BpmnXMLParser();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("org/uengine/test/TroubleAlertTest-Parser.bpmn").getFile());
        String xml = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            xml = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            assertNotNull("Process definition should not be null", processDefinition);

            Activity subProcessActivity = processDefinition.getActivity("Activity_0gf1pdp");
            assertNotNull("SubProcess Activity_0gf1pdp should not be null", subProcessActivity);
            assertTrue("Activity_0gf1pdp should be an instance of SubProcess",
                    subProcessActivity instanceof SubProcess);

            SubProcess subProcess = (SubProcess) subProcessActivity;
            assertFalse("SubProcess should have child activities", subProcess.getChildActivities().isEmpty());

            // Check for nested SubProcess
            Activity nestedSubProcessActivity = subProcess.getChildActivities().stream()
                    .filter(activity -> "Activity_0hon0vy".equals(activity.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Nested SubProcess Activity_0hon0vy should not be null", nestedSubProcessActivity);
            // assertTrue("Activity_0hon0vy should be an instance of SubProcess",
            //         nestedSubProcessActivity instanceof SubProcess);

            // SubProcess nestedSubProcess = (SubProcess) nestedSubProcessActivity;
            // assertFalse("Nested SubProcess should have child activities",
            //         nestedSubProcess.getChildActivities().isEmpty());

            // Additional checks can be performed here for other activities and properties
            // within the nested subprocess

        } catch (Exception e) {
            e.printStackTrace();
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseFormActivityWithMappingContext() {
        BpmnXMLParser parser = new BpmnXMLParser();

        try {
            String xmlFilePath = "src/test/java/org/uengine/test/formProcess.xml";
            String xml = new String(Files.readAllBytes(Paths.get(xmlFilePath)));

            String testJsonPath = "src/test/java/org/uengine/test/mappingTest.json";
            String testJsonXml = new String(Files.readAllBytes(Paths.get(testJsonPath)));

            // ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
            // HtmlFormContext result = objectMapper.readValue(testJsonXml, HtmlFormContext.class);

            ProcessDefinition processDefinition = parser.parse(xml);
            Map<String, Activity> activities = processDefinition.getWholeChildActivities();
            assertTrue("The process should contain a FormActivity with id 'formActivity1'.",
                    activities.containsKey("formActivity1") && activities.get("formActivity1") instanceof FormActivity);
            FormActivity formActivity = (FormActivity) activities.get("formActivity1");
            assertNotNull("FormActivity's MappingContext should not be null", formActivity.getMappingContext());
            // Further assertions can be made here to verify the details of the
            // MappingContext
        } catch (Exception e) {
            fail("Parsing FormActivity with MappingContext failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseProcessVariablesWithComplexDefaultValue() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n" +
                "  <bpmn:process id=\"Process_1oscmbn\" isExecutable=\"false\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:variable name=\"장애신고\" type=\"Form\">\n" +
                "          <uengine:json>\n" +
                "            {\n" +
                "              \"defaultValue\": {\n" +
                "                \"_type\": \"org.uengine.contexts.HtmlFormContext\",\n"+
                "                \"formDefId\": \"form11\",\n" +
                "                \"filePath\": \"\"\n" +
                "              }\n" +
                "            }\n" +
                "          </uengine:json>\n" +
                "        </uengine:variable>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            ProcessVariable variable = processDefinition.getProcessVariable("장애신고");
            assertNotNull("Process variable '장애신고' should not be null", variable);

            // Assuming ProcessVariable has a method to get complex defaultValue
            // Replace 'getDefaultValue' and 'getValueMap' with actual method names if different
            Map<String, Object> defaultValue = (Map<String, Object>) variable.getDefaultValue();
            assertNotNull("defaultValue should not be null", defaultValue);

            assertEquals("formDefId should be 'form11'", "form11", defaultValue.get("formDefId"));
            assertTrue("valueMap should contain fields", ((Map) defaultValue.get("valueMap")).containsKey("fields"));
        } catch (Exception e) {
            fail("Parsing process variables with complex default values failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetFormDefinition() {
        BpmnXMLParser parser = new BpmnXMLParser();

        try {
            String xmlFilePath = "src/test/java/org/uengine/test/formProcess.xml";
            String xml = new String(Files.readAllBytes(Paths.get(xmlFilePath)));

            ProcessDefinition processDefinition = parser.parse(xml);
            WorklistEntity workItem = new WorklistEntity();
            workItem.setTool("formHandler:testForm");

            String formName = workItem.getTool().split(":")[1];
            String formFilePath = "src/test/java/org/uengine/test/" + formName + ".form";

            // Read the content of the form file
            String formContent = new String(Files.readAllBytes(Paths.get(formFilePath)));
            System.out.println("Form Content: \n" + formContent);
        } catch (Exception e) {
            fail("Parsing FormActivity with MappingContext failed with exception: " + e.getMessage());
        }
    }


    @Test
    public void testSerializeHtml() {
        BpmnXMLParser parser = new BpmnXMLParser();

        try {
            HtmlFormContext htmlFormContext = new HtmlFormContext();
            htmlFormContext.setValueMap(new HashMap<>());
            htmlFormContext.getValueMap().put("troubletype", "sw");
            htmlFormContext.setFormDefId("troubleTicketForm");

            System.out.println(BpmnXMLParser.createTypedJsonObjectMapper().writeValueAsString(htmlFormContext));
        } catch (Exception e) {
            fail("Parsing FormActivity with MappingContext failed with exception: " + e.getMessage());
        }
    }

}
