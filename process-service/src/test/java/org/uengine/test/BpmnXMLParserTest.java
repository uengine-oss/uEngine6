package org.uengine.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.Role;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;

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

    @Test
    public void testParseProcessVariables() throws Exception {
        String xml = "<definitions>" +
                "<uengine:data>" +
                "<uengine:variable name=\"variable1\" type=\"java.lang.String\"/>" +
                "<uengine:variable name=\"variable2\" type=\"java.lang.Integer\"/>" +
                "</uengine:data>" +
                "</definitions>";

        BpmnXMLParser parser = new BpmnXMLParser();
        ProcessDefinition processDefinition = parser.parse(xml);

        assertTrue("Should have 2 process variable", processDefinition.getProcessVariables().length == 2);

        ProcessVariable variable1 = processDefinition.getProcessVariable("variable1");
        assertEquals("Variable1 should be of type String", "java.lang.String", variable1.getType().getName());

        ProcessVariable variable2 = processDefinition.getProcessVariable("variable2");
        assertEquals("Variable2 should be of type Integer", "java.lang.Integer", variable2.getType().getName());
    }

}
