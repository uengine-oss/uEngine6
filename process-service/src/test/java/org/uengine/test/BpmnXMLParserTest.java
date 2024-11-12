package org.uengine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.uengine.contexts.EventSynchronization;
import org.uengine.contexts.MappingContext;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.FieldDescriptor;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.MappingElement;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.Role;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.SubProcess;

public class BpmnXMLParserTest {

    // 가장 기본적인 태스크 하나만 있는 상태를 체크하는 테스트 코드
    @Test
    public void testParse() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" id=\"Definitions_0bfky9r\" name=\"test/testParser\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser\",\"version\":\"2.0\",\"shortDescription\":{\"text\":\"\"}}</uengine:json>\n"
                +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:task id=\"Activity_08cur5j\" name=\"task1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "    </bpmn:task>\n" +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0cw20l7\">\n" +
                "      <bpmndi:BPMNShape id=\"Activity_08cur5j_di\" bpmnElement=\"Activity_08cur5j\">\n" +
                "        <dc:Bounds x=\"390\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            Map<String, Activity> tasks = processDefinition.getWholeChildActivities();
            assertEquals(2, tasks.size());
            assertTrue(tasks.containsKey("Activity_08cur5j"));
            assertEquals("task1", ((Activity) tasks.get("Activity_08cur5j")).getName());
        } catch (Exception e) {
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    // 파일 경로로 텍스트를 가져와 기본적인 프로세스 Parsing테스트
    @Test
    public void testParsePurchaseRequestBpmn() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String bpmnFilePath = "src/test/java/org/uengine/test/testParser.bpmn";
        try {
            String bpmnContent = new String(Files.readAllBytes(Paths.get(bpmnFilePath)));
            assertNotNull("BPMN content should not be null", bpmnContent);
            ProcessDefinition processDefinition = parser.parse(bpmnContent);
            assertNotNull("ProcessDefinition should not be null", processDefinition);
        } catch (Exception e) {
            fail("Failed to parse PurchaseRequest.bpmn due to: " + e.getMessage());
        }
    }

    // 시퀀스플로우 파싱 테스트
    @Test
    public void testParseSequenceFlow() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" name=\"test/testParser_sequence\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_sequence\",\"version\":\"1.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:task id=\"Activity_08cur5j\" name=\"task1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0zetnwg</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1g1gc4q</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:startEvent id=\"Event_1ksa7fz\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0zetnwg</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_0zetnwg\" sourceRef=\"Event_1ksa7fz\" targetRef=\"Activity_08cur5j\" />\n"
                +
                "    <bpmn:task id=\"Activity_1a5b17t\" name=\"task2\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1g1gc4q</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_108pvu2</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1g1gc4q\" sourceRef=\"Activity_08cur5j\" targetRef=\"Activity_1a5b17t\" />\n"
                +
                "    <bpmn:endEvent id=\"Event_1hf9qk5\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_108pvu2</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_108pvu2\" sourceRef=\"Activity_1a5b17t\" targetRef=\"Event_1hf9qk5\" />\n"
                +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0cw20l7\">\n" +
                "      <bpmndi:BPMNShape id=\"Activity_08cur5j_di\" bpmnElement=\"Activity_08cur5j\">\n" +
                "        <dc:Bounds x=\"390\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1ksa7fz_di\" bpmnElement=\"Event_1ksa7fz\">\n" +
                "        <dc:Bounds x=\"202\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Activity_1a5b17t_di\" bpmnElement=\"Activity_1a5b17t\">\n" +
                "        <dc:Bounds x=\"650\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1hf9qk5_di\" bpmnElement=\"Event_1hf9qk5\">\n" +
                "        <dc:Bounds x=\"912\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0zetnwg_di\" bpmnElement=\"Flow_0zetnwg\">\n" +
                "        <di:waypoint x=\"238\" y=\"180\" />\n" +
                "        <di:waypoint x=\"390\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1g1gc4q_di\" bpmnElement=\"Flow_1g1gc4q\">\n" +
                "        <di:waypoint x=\"490\" y=\"180\" />\n" +
                "        <di:waypoint x=\"650\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_108pvu2_di\" bpmnElement=\"Flow_108pvu2\">\n" +
                "        <di:waypoint x=\"750\" y=\"180\" />\n" +
                "        <di:waypoint x=\"912\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            assertEquals("The process should have exactly one sequence flow.", 3,
                    processDefinition.getSequenceFlows().size());
            assertTrue("The process should contain a sequence flow with ID 'flow1'.",
                    processDefinition.getSequenceFlows().stream()
                            .anyMatch(flow -> "Flow_0zetnwg".equals(flow.getTracingTag())));
            SequenceFlow flow1 = processDefinition.getSequenceFlows().stream()
                    .filter(flow -> "Flow_0zetnwg".equals(flow.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Sequence flow 'flow1' should not be null", flow1);
            assertEquals("The sourceRef of sequence flow 'flow1' should be 'task1'", "Event_1ksa7fz",
                    flow1.getSourceRef());
        } catch (Exception e) {
            fail("Parsing sequence flow failed with exception: " + e.getMessage());
        }
    }

    // Condition 파싱 테스트
    @Test
    public void testParseSequenceFlowWithCondition() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" name=\"test/testParser_condition\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_condition\",\"version\":\"1.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "        <uengine:variable name=\"고장\" type=\"Text\">\n" +
                "          <uengine:json>{\"defaultValue\":\"\"}</uengine:json>\n" +
                "        </uengine:variable>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:task id=\"Activity_08cur5j\" name=\"task1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0zetnwg</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1g1gc4q</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:startEvent id=\"Event_1ksa7fz\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0zetnwg</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_0zetnwg\" sourceRef=\"Event_1ksa7fz\" targetRef=\"Activity_08cur5j\" />\n"
                +
                "    <bpmn:task id=\"Activity_1a5b17t\" name=\"task2\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1g1gc4q</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_108pvu2</bpmn:outgoing>\n" +
                "    </bpmn:task>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1g1gc4q\" name=\"\" sourceRef=\"Activity_08cur5j\" targetRef=\"Activity_1a5b17t\">\n"
                +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{\"condition\":{\"_type\":\"org.uengine.kernel.Evaluate\",\"key\":\"고장\",\"value\":\"true\",\"condition\":\"==\"}}</uengine:json>\n"
                +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "    </bpmn:sequenceFlow>\n" +
                "    <bpmn:endEvent id=\"Event_1hf9qk5\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_108pvu2</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_108pvu2\" sourceRef=\"Activity_1a5b17t\" targetRef=\"Event_1hf9qk5\" />\n"
                +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0cw20l7\">\n" +
                "      <bpmndi:BPMNShape id=\"Activity_08cur5j_di\" bpmnElement=\"Activity_08cur5j\">\n" +
                "        <dc:Bounds x=\"390\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1ksa7fz_di\" bpmnElement=\"Event_1ksa7fz\">\n" +
                "        <dc:Bounds x=\"202\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Activity_1a5b17t_di\" bpmnElement=\"Activity_1a5b17t\">\n" +
                "        <dc:Bounds x=\"650\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1hf9qk5_di\" bpmnElement=\"Event_1hf9qk5\">\n" +
                "        <dc:Bounds x=\"912\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0zetnwg_di\" bpmnElement=\"Flow_0zetnwg\">\n" +
                "        <di:waypoint x=\"238\" y=\"180\" />\n" +
                "        <di:waypoint x=\"390\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1g1gc4q_di\" bpmnElement=\"Flow_1g1gc4q\">\n" +
                "        <di:waypoint x=\"490\" y=\"180\" />\n" +
                "        <di:waypoint x=\"650\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_108pvu2_di\" bpmnElement=\"Flow_108pvu2\">\n" +
                "        <di:waypoint x=\"750\" y=\"180\" />\n" +
                "        <di:waypoint x=\"912\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            SequenceFlow flow1 = processDefinition.getSequenceFlows().stream()
                    .filter(flow -> "Flow_1g1gc4q".equals(flow.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Sequence flow 'Flow_1g1gc4q' should not be null", flow1);

            // Assuming there's a method in SequenceFlow to get condition
            // Replace 'getCondition' with the actual method name
            assertNotNull("Condition should not be null", flow1.getCondition());
            assertEquals("Condition key should be '고장'", "고장",
                    ((Evaluate) flow1.getCondition()).getKey());
            assertEquals("Condition value should be 'true'", "true", ((Evaluate) flow1.getCondition()).getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Parsing sequence flow with condition failed with exception: " + e.getMessage());
        }
    }

    // 게이트웨이 Parsing 테스트
    @Test
    public void testParseGatewaysAndEvents() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
                "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
                "xmlns:uengine=\"http://uengine\" " +
                "xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" " +
                "xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" " +
                "id=\"Definitions_0bfky9r\" name=\"test/testParser_gateway\" " +
                "targetNamespace=\"http://bpmn.io/schema/bpmn\" " +
                "exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n" +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_gateway\",\"version\":\"3.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"Event_1ksa7fz\" name=\"Start Event\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0r4nc6f</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:endEvent id=\"Event_1hf9qk5\" name=\"End Event\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n"
                +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1e0qfjx</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:exclusiveGateway id=\"Gateway_0d2lowi\" name=\"Exclusive Gateway\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0r4nc6f</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1e0qfjx</bpmn:outgoing>\n" +
                "    </bpmn:exclusiveGateway>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_0r4nc6f\" sourceRef=\"Event_1ksa7fz\" targetRef=\"Gateway_0d2lowi\" />\n"
                +
                "    <bpmn:sequenceFlow id=\"Flow_1e0qfjx\" sourceRef=\"Gateway_0d2lowi\" targetRef=\"Event_1hf9qk5\" />\n"
                +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0cw20l7\">\n" +
                "      <bpmndi:BPMNShape id=\"Event_1ksa7fz_di\" bpmnElement=\"Event_1ksa7fz\">\n" +
                "        <dc:Bounds x=\"482\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "        <bpmndi:BPMNLabel>\n" +
                "          <dc:Bounds x=\"473\" y=\"205\" width=\"55\" height=\"14\" />\n" +
                "        </bpmndi:BPMNLabel>\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1hf9qk5_di\" bpmnElement=\"Event_1hf9qk5\">\n" +
                "        <dc:Bounds x=\"912\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "        <bpmndi:BPMNLabel>\n" +
                "          <dc:Bounds x=\"904\" y=\"205\" width=\"52\" height=\"14\" />\n" +
                "        </bpmndi:BPMNLabel>\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Gateway_0d2lowi_di\" bpmnElement=\"Gateway_0d2lowi\" isMarkerVisible=\"true\">\n"
                +
                "        <dc:Bounds x=\"675\" y=\"155\" width=\"50\" height=\"50\" />\n" +
                "        <bpmndi:BPMNLabel>\n" +
                "          <dc:Bounds x=\"677\" y=\"212\" width=\"47\" height=\"27\" />\n" +
                "        </bpmndi:BPMNLabel>\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0r4nc6f_di\" bpmnElement=\"Flow_0r4nc6f\">\n" +
                "        <di:waypoint x=\"518\" y=\"180\" />\n" +
                "        <di:waypoint x=\"675\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1e0qfjx_di\" bpmnElement=\"Flow_1e0qfjx\">\n" +
                "        <di:waypoint x=\"725\" y=\"180\" />\n" +
                "        <di:waypoint x=\"912\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            Map<String, Activity> flowElements = processDefinition.getWholeChildActivities();

            // Test for Gateway
            assertTrue("The process should contain a gateway with ID 'Gateway_0d2lowi'.",
                    flowElements.containsKey("Gateway_0d2lowi"));
            assertEquals("Exclusive Gateway", ((Gateway) flowElements.get("Gateway_0d2lowi")).getName());

            // Test for Start Event
            assertTrue("The process should contain a start event with ID 'Event_1ksa7fz'.",
                    flowElements.containsKey("Event_1ksa7fz"));
            assertEquals("Start Event", ((Event) flowElements.get("Event_1ksa7fz")).getName());

            // Test for End Event
            assertTrue("The process should contain an end event with ID 'Event_1hf9qk5'.",
                    flowElements.containsKey("Event_1hf9qk5"));
            assertEquals("End Event", ((Event) flowElements.get("Event_1hf9qk5")).getName());
        } catch (Exception e) {
            e.printStackTrace();

            fail("Parsing gateways and events failed with exception: " + e.getMessage());
        }
    }

    // Json 첨부 태스트 테스트
    @Test
    public void testParseUserTaskWithJsonProperties() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" name=\"test/testParser_task_json\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:collaboration id=\"Collaboration_1mbz2kw\">\n" +
                "    <bpmn:participant id=\"Participant_1q8r3oa\" name=\"test\" processRef=\"Process_0cw20l7\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "    </bpmn:participant>\n" +
                "  </bpmn:collaboration>\n" +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_task_json\",\"version\":\"1.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:laneSet id=\"LaneSet_0mhorsa\">\n" +
                "      <bpmn:lane id=\"Lane_0sfpp89\" name=\"initiator\">\n" +
                "        <bpmn:extensionElements>\n" +
                "          <uengine:properties>\n" +
                "            <uengine:json>{\"roleResolutionContext\":{\"_type\":\"org.uengine.five.overriding.IAMRoleResolutionContext\",\"scope\":\"initiator\"}}</uengine:json>\n"
                +
                "          </uengine:properties>\n" +
                "        </bpmn:extensionElements>\n" +
                "        <bpmn:flowNodeRef>Event_1hf9qk5</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Activity_08cur5j</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Event_1ksa7fz</bpmn:flowNodeRef>\n" +
                "      </bpmn:lane>\n" +
                "    </bpmn:laneSet>\n" +
                "    <bpmn:endEvent id=\"Event_1hf9qk5\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1g1gc4q</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:userTask id=\"Activity_08cur5j\" name=\"task1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{\"eventSynchronization\":{\"eventType\":\"\",\"attributes\":[{\"name\":\"test\",\"className\":\"String\",\"isKey\":false,\"isCorrKey\":false}],\"mappingContext\":{\"mappingElements\":[]}},\"_type\":\"org.uengine.kernel.HumanActivity\"}</uengine:json>\n"
                +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0zetnwg</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1g1gc4q</bpmn:outgoing>\n" +
                "    </bpmn:userTask>\n" +
                "    <bpmn:startEvent id=\"Event_1ksa7fz\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0zetnwg</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1g1gc4q\" sourceRef=\"Activity_08cur5j\" targetRef=\"Event_1hf9qk5\" />\n"
                +
                "    <bpmn:sequenceFlow id=\"Flow_0zetnwg\" sourceRef=\"Event_1ksa7fz\" targetRef=\"Activity_08cur5j\" />\n"
                +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Collaboration_1mbz2kw\">\n" +
                "      <bpmndi:BPMNShape id=\"Participant_1q8r3oa_di\" bpmnElement=\"Participant_1q8r3oa\" isHorizontal=\"true\">\n"
                +
                "        <dc:Bounds x=\"180\" y=\"80\" width=\"480\" height=\"200\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Lane_0sfpp89_di\" bpmnElement=\"Lane_0sfpp89\" isHorizontal=\"true\">\n" +
                "        <dc:Bounds x=\"210\" y=\"80\" width=\"450\" height=\"200\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1hf9qk5_di\" bpmnElement=\"Event_1hf9qk5\">\n" +
                "        <dc:Bounds x=\"602\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Activity_1uvzss9_di\" bpmnElement=\"Activity_08cur5j\">\n" +
                "        <dc:Bounds x=\"390\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1ksa7fz_di\" bpmnElement=\"Event_1ksa7fz\">\n" +
                "        <dc:Bounds x=\"232\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1g1gc4q_di\" bpmnElement=\"Flow_1g1gc4q\">\n" +
                "        <di:waypoint x=\"490\" y=\"180\" />\n" +
                "        <di:waypoint x=\"602\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0zetnwg_di\" bpmnElement=\"Flow_0zetnwg\">\n" +
                "        <di:waypoint x=\"268\" y=\"180\" />\n" +
                "        <di:waypoint x=\"390\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            HumanActivity userTask = (HumanActivity) processDefinition.getChildActivities().get(1);
            // Assuming there's a method in Activity to get parameters or properties
            // Replace 'getParameters' with the actual method name
            FieldDescriptor[] parameters = userTask.getEventSynchronization().getAttributes();
            assertNotNull("Parameters should not be null", parameters);
            boolean containsTest = Arrays.stream(parameters)
                    .anyMatch(param -> "test".equals(param.getName()));
            assertTrue("Parameters should contain 'test'", containsTest);
            // Additional assertions can be added based on the expected properties

            Role role = userTask.getRole();
            assertNotNull("Role should not be null", role);
            assertEquals("initiator", role.getName());
        } catch (Exception e) {
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    // 프로세스 변수 파싱 관련 테스트코드
    @Test
    public void testParseProcessVariables() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" id=\"Definitions_0bfky9r\" name=\"test/testParser_process_variables\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:process id=\"Process_1sg6eah\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_process_variables\",\"version\":\"1.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "        <uengine:variable name=\"variable1\" type=\"Text\">\n" +
                "          <uengine:json>{\"defaultValue\":\"\"}</uengine:json>\n" +
                "        </uengine:variable>\n" +
                "        <uengine:variable name=\"variable2\" type=\"Number\">\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:variable>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1sg6eah\" />\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        BpmnXMLParser parser = new BpmnXMLParser();
        ProcessDefinition processDefinition = parser.parse(xml);

        assertTrue("Should have 2 process variable", processDefinition.getProcessVariables().length == 2);

        ProcessVariable variable1 = processDefinition.getProcessVariable("variable1");
        assertEquals("Variable1 should be of type Text", "java.lang.String", variable1.getType().getName());

        ProcessVariable variable2 = processDefinition.getProcessVariable("variable2");
        assertEquals("Variable2 should be of type Number", "java.lang.Number", variable2.getType().getName());
    }

    // 서프프로세스 파싱 테스트
    @Test
    public void testParseSubProcessWithNestedActivities() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" name=\"test/testParser_subprocess\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:collaboration id=\"Collaboration_1pnt2vh\">\n" +
                "    <bpmn:participant id=\"Participant_11nwwnv\" name=\"main\" processRef=\"Process_0cw20l7\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "    </bpmn:participant>\n" +
                "  </bpmn:collaboration>\n" +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_subprocess\",\"version\":\"2.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:laneSet id=\"LaneSet_1vm2td3\">\n" +
                "      <bpmn:lane id=\"Lane_17g7xvy\" name=\"sub\">\n" +
                "        <bpmn:extensionElements>\n" +
                "          <uengine:properties>\n" +
                "            <uengine:json>{\"roleResolutionContext\":{\"_type\":\"org.uengine.five.overriding.IAMRoleResolutionContext\",\"scope\":\"manager\"}}</uengine:json>\n"
                +
                "          </uengine:properties>\n" +
                "        </bpmn:extensionElements>\n" +
                "        <bpmn:flowNodeRef>Event_0q70fd9</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Event_0w05915</bpmn:flowNodeRef>\n" +
                "        <bpmn:flowNodeRef>Activity_0y62pf7</bpmn:flowNodeRef>\n" +
                "      </bpmn:lane>\n" +
                "    </bpmn:laneSet>\n" +
                "    <bpmn:startEvent id=\"Event_0q70fd9\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0c2efq8</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:subProcess id=\"Activity_0y62pf7\" name=\"subprocess\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0c2efq8</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1qe49qu</bpmn:outgoing>\n" +
                "      <bpmn:startEvent id=\"Event_19gskc7\">\n" +
                "        <bpmn:extensionElements>\n" +
                "          <uengine:properties>\n" +
                "            <uengine:json>{}</uengine:json>\n" +
                "          </uengine:properties>\n" +
                "        </bpmn:extensionElements>\n" +
                "        <bpmn:outgoing>Flow_0uhezd0</bpmn:outgoing>\n" +
                "      </bpmn:startEvent>\n" +
                "      <bpmn:sequenceFlow id=\"Flow_0uhezd0\" sourceRef=\"Event_19gskc7\" targetRef=\"Activity_1e0od7x\" />\n"
                +
                "      <bpmn:endEvent id=\"Event_064oepn\">\n" +
                "        <bpmn:extensionElements>\n" +
                "          <uengine:properties>\n" +
                "            <uengine:json>{}</uengine:json>\n" +
                "          </uengine:properties>\n" +
                "        </bpmn:extensionElements>\n" +
                "        <bpmn:incoming>Flow_09ak211</bpmn:incoming>\n" +
                "      </bpmn:endEvent>\n" +
                "      <bpmn:sequenceFlow id=\"Flow_09ak211\" sourceRef=\"Activity_1e0od7x\" targetRef=\"Event_064oepn\" />\n"
                +
                "      <bpmn:userTask id=\"Activity_1e0od7x\" name=\"subActivity\">\n" +
                "        <bpmn:extensionElements>\n" +
                "          <uengine:properties>\n" +
                "            <uengine:json>{\"eventSynchronization\":{\"eventType\":\"\",\"attributes\":[],\"mappingContext\":{\"mappingElements\":[]}},\"_type\":\"org.uengine.kernel.HumanActivity\"}</uengine:json>\n"
                +
                "          </uengine:properties>\n" +
                "        </bpmn:extensionElements>\n" +
                "        <bpmn:incoming>Flow_0uhezd0</bpmn:incoming>\n" +
                "        <bpmn:outgoing>Flow_09ak211</bpmn:outgoing>\n" +
                "      </bpmn:userTask>\n" +
                "    </bpmn:subProcess>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_0c2efq8\" sourceRef=\"Event_0q70fd9\" targetRef=\"Activity_0y62pf7\" />\n"
                +
                "    <bpmn:endEvent id=\"Event_0w05915\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1qe49qu</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1qe49qu\" sourceRef=\"Activity_0y62pf7\" targetRef=\"Event_0w05915\" />\n"
                +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Collaboration_1pnt2vh\">\n" +
                "      <bpmndi:BPMNShape id=\"Participant_11nwwnv_di\" bpmnElement=\"Participant_11nwwnv\" isHorizontal=\"true\">\n"
                +
                "        <dc:Bounds x=\"250\" y=\"220\" width=\"630\" height=\"300\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Lane_17g7xvy_di\" bpmnElement=\"Lane_17g7xvy\" isHorizontal=\"true\">\n" +
                "        <dc:Bounds x=\"280\" y=\"220\" width=\"600\" height=\"300\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_0q70fd9_di\" bpmnElement=\"Event_0q70fd9\">\n" +
                "        <dc:Bounds x=\"302\" y=\"262\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Activity_0y62pf7_di\" bpmnElement=\"Activity_0y62pf7\" isExpanded=\"true\">\n"
                +
                "        <dc:Bounds x=\"400\" y=\"240\" width=\"350\" height=\"200\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_19gskc7_di\" bpmnElement=\"Event_19gskc7\">\n" +
                "        <dc:Bounds x=\"432\" y=\"282\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_064oepn_di\" bpmnElement=\"Event_064oepn\">\n" +
                "        <dc:Bounds x=\"672\" y=\"282\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Activity_0nfq6ca_di\" bpmnElement=\"Activity_1e0od7x\">\n" +
                "        <dc:Bounds x=\"520\" y=\"260\" width=\"100\" height=\"80\" />\n" +
                "        <bpmndi:BPMNLabel />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0uhezd0_di\" bpmnElement=\"Flow_0uhezd0\">\n" +
                "        <di:waypoint x=\"468\" y=\"300\" />\n" +
                "        <di:waypoint x=\"520\" y=\"300\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_09ak211_di\" bpmnElement=\"Flow_09ak211\">\n" +
                "        <di:waypoint x=\"620\" y=\"300\" />\n" +
                "        <di:waypoint x=\"672\" y=\"300\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNShape id=\"Event_0w05915_di\" bpmnElement=\"Event_0w05915\">\n" +
                "        <dc:Bounds x=\"812\" y=\"322\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0c2efq8_di\" bpmnElement=\"Flow_0c2efq8\">\n" +
                "        <di:waypoint x=\"338\" y=\"280\" />\n" +
                "        <di:waypoint x=\"400\" y=\"280\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1qe49qu_di\" bpmnElement=\"Flow_1qe49qu\">\n" +
                "        <di:waypoint x=\"750\" y=\"340\" />\n" +
                "        <di:waypoint x=\"812\" y=\"340\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            assertNotNull("Process definition should not be null", processDefinition);

            Activity subProcessActivity = processDefinition.getActivity("Activity_0y62pf7");
            assertNotNull("SubProcess Activity_0y62pf7 should not be null", subProcessActivity);
            assertTrue("Activity_0y62pf7 should be an instance of SubProcess",
                    subProcessActivity instanceof SubProcess);

            SubProcess subProcess = (SubProcess) subProcessActivity;
            assertFalse("SubProcess should have child activities", subProcess.getChildActivities().isEmpty());

            // Check for nested SubProcess
            Activity nestedSubProcessActivity = subProcess.getChildActivities().stream()
                    .filter(activity -> "Activity_1e0od7x".equals(activity.getTracingTag()))
                    .findFirst()
                    .orElse(null);
            assertNotNull("Nested SubProcess Activity_1e0od7x should not be null", nestedSubProcessActivity);
            // assertTrue("Activity_0hon0vy should be an instance of SubProcess",
            // nestedSubProcessActivity instanceof SubProcess);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Parsing failed with exception: " + e.getMessage());
        }
    }

    // 매핑관련 파싱 테스트
    @Test
    public void testParseFormActivityWithMappingContext() {
        BpmnXMLParser parser = new BpmnXMLParser();
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:uengine=\"http://uengine\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"Definitions_0bfky9r\" name=\"test/testParser_mapping\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
                +
                "  <bpmn:process id=\"Process_0cw20l7\" isExecutable=\"true\">\n" +
                "    <bpmn:extensionElements>\n" +
                "      <uengine:properties>\n" +
                "        <uengine:json>{\"definitionName\":\"test/testParser_mapping\",\"version\":\"1.0\",\"shortDescription\":{\"text\":null}}</uengine:json>\n"
                +
                "        <uengine:variable name=\"testValue\" type=\"Text\">\n" +
                "          <uengine:json>{\"defaultValue\":\"\"}</uengine:json>\n" +
                "        </uengine:variable>\n" +
                "      </uengine:properties>\n" +
                "    </bpmn:extensionElements>\n" +
                "    <bpmn:startEvent id=\"Event_1ksa7fz\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:outgoing>Flow_0zetnwg</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_0zetnwg\" sourceRef=\"Event_1ksa7fz\" targetRef=\"Activity_08cur5j\" />\n"
                +
                "    <bpmn:sequenceFlow id=\"Flow_1g1gc4q\" sourceRef=\"Activity_08cur5j\" targetRef=\"Event_1hf9qk5\" />\n"
                +
                "    <bpmn:endEvent id=\"Event_1hf9qk5\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{}</uengine:json>\n" +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_1g1gc4q</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:userTask id=\"Activity_08cur5j\" name=\"task1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <uengine:properties>\n" +
                "          <uengine:json>{\"eventSynchronization\":{\"eventType\":\"\",\"attributes\":[{\"name\":\"test\",\"className\":\"String\",\"isKey\":false,\"isCorrKey\":false}],\"mappingContext\":{\"mappingElements\":[{\"argument\":{\"text\":\"testValue\"},\"direction\":\"out\",\"variable\":{\"name\":\"[Arguments].test\",\"askWhenInit\":false,\"isVolatile\":false},\"isKey\":false}]}},\"_type\":\"org.uengine.kernel.HumanActivity\"}</uengine:json>\n"
                +
                "        </uengine:properties>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>Flow_0zetnwg</bpmn:incoming>\n" +
                "      <bpmn:outgoing>Flow_1g1gc4q</bpmn:outgoing>\n" +
                "    </bpmn:userTask>\n" +
                "  </bpmn:process>\n" +
                "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0cw20l7\">\n" +
                "      <bpmndi:BPMNShape id=\"Activity_08cur5j_di\" bpmnElement=\"Activity_08cur5j\">\n" +
                "        <dc:Bounds x=\"390\" y=\"140\" width=\"100\" height=\"80\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1ksa7fz_di\" bpmnElement=\"Event_1ksa7fz\">\n" +
                "        <dc:Bounds x=\"202\" y=\"162\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNShape id=\"Event_1hf9qk5_di\" bpmnElement=\"Event_1hf9qk5\">\n" +
                "        <dc:Bounds x=\"812\" y=\"322\" width=\"36\" height=\"36\" />\n" +
                "      </bpmndi:BPMNShape>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_0zetnwg_di\" bpmnElement=\"Flow_0zetnwg\">\n" +
                "        <di:waypoint x=\"238\" y=\"180\" />\n" +
                "        <di:waypoint x=\"390\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "      <bpmndi:BPMNEdge id=\"Flow_1g1gc4q_di\" bpmnElement=\"Flow_1g1gc4q\">\n" +
                "        <di:waypoint x=\"490\" y=\"180\" />\n" +
                "        <di:waypoint x=\"650\" y=\"180\" />\n" +
                "      </bpmndi:BPMNEdge>\n" +
                "    </bpmndi:BPMNPlane>\n" +
                "  </bpmndi:BPMNDiagram>\n" +
                "</bpmn:definitions>";

        try {
            ProcessDefinition processDefinition = parser.parse(xml);
            assertNotNull("Process definition should not be null", processDefinition);

            Activity userTaskActivity = processDefinition.getActivity("Activity_08cur5j");
            assertNotNull("UserTask Activity_08cur5j should not be null", userTaskActivity);
            assertTrue("Activity_08cur5j should be an instance of HumanActivity",
                    userTaskActivity instanceof HumanActivity);

            HumanActivity humanActivity = (HumanActivity) userTaskActivity;
            assertNotNull("Event synchronization should not be null", humanActivity.getEventSynchronization());

            EventSynchronization eventSynchronization = humanActivity.getEventSynchronization();
            assertNotNull("Mapping context should not be null", eventSynchronization.getMappingContext());

            MappingContext mappingContext = eventSynchronization.getMappingContext();
            assertFalse("Mapping context should have mapping elements",
                    mappingContext.getMappingElements().length == 0);

            MappingElement mappingElement = mappingContext.getMappingElements()[0];
            assertEquals("Mapping element direction should be 'out'", "out", mappingElement.getDirection());
            assertEquals("Mapping element variable name should be '[Arguments].test'", "[Arguments].test",
                    mappingElement.getVariable().getName());
            assertEquals("Mapping element argument text should be 'testValue'", "testValue",
                    mappingElement.getArgument().getText());

        } catch (Exception e) {
            fail("Parsing FormActivity with MappingContext failed with exception: " + e.getMessage());
        }
    }

    // @Test
    // public void testParseProcessVariablesWithComplexDefaultValue() {
    // BpmnXMLParser parser = new BpmnXMLParser();
    // String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    // "<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
    // xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"
    // xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"
    // xmlns:uengine=\"http://uengine\"
    // xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"
    // xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"
    // id=\"Definitions_0bfky9r\" targetNamespace=\"http://bpmn.io/schema/bpmn\"
    // exporter=\"bpmn-js (https://demo.bpmn.io)\" exporterVersion=\"16.4.0\">\n"
    // +
    // " <bpmn:process id=\"Process_1oscmbn\" isExecutable=\"false\">\n" +
    // " <bpmn:extensionElements>\n" +
    // " <uengine:properties>\n" +
    // " <uengine:variable name=\"장애신고\" type=\"Form\">\n" +
    // " <uengine:json>\n" +
    // " {\n" +
    // " \"defaultValue\": {\n" +
    // " \"_type\": \"org.uengine.contexts.HtmlFormContext\",\n" +
    // " \"formDefId\": \"form11\",\n" +
    // " \"filePath\": \"\"\n" +
    // " }\n" +
    // " }\n" +
    // " </uengine:json>\n" +
    // " </uengine:variable>\n" +
    // " </uengine:properties>\n" +
    // " </bpmn:extensionElements>\n" +
    // " </bpmn:process>\n" +
    // "</bpmn:definitions>";

    // try {
    // ProcessDefinition processDefinition = parser.parse(xml);
    // ProcessVariable variable = processDefinition.getProcessVariable("장애신고");
    // assertNotNull("Process variable '장애신고' should not be null", variable);

    // // Assuming ProcessVariable has a method to get complex defaultValue
    // // Replace 'getDefaultValue' and 'getValueMap' with actual method names if
    // // different
    // Map<String, Object> defaultValue = (Map<String, Object>)
    // variable.getDefaultValue();
    // assertNotNull("defaultValue should not be null", defaultValue);

    // assertEquals("formDefId should be 'form11'", "form11",
    // defaultValue.get("formDefId"));
    // assertTrue("valueMap should contain fields", ((Map)
    // defaultValue.get("valueMap")).containsKey("fields"));
    // } catch (Exception e) {
    // fail("Parsing process variables with complex default values failed with
    // exception: " + e.getMessage());
    // }
    // }

    // @Test
    // public void testGetFormDefinition() {
    // BpmnXMLParser parser = new BpmnXMLParser();

    // try {
    // String xmlFilePath = "src/test/java/org/uengine/test/formProcess.xml";
    // String xml = new String(Files.readAllBytes(Paths.get(xmlFilePath)));

    // ProcessDefinition processDefinition = parser.parse(xml);
    // WorklistEntity workItem = new WorklistEntity();
    // workItem.setTool("formHandler:testForm");

    // String formName = workItem.getTool().split(":")[1];
    // String formFilePath = "src/test/java/org/uengine/test/" + formName + ".form";

    // // Read the content of the form file
    // String formContent = new String(Files.readAllBytes(Paths.get(formFilePath)));
    // System.out.println("Form Content: \n" + formContent);
    // } catch (Exception e) {
    // fail("Parsing FormActivity with MappingContext failed with exception: " +
    // e.getMessage());
    // }
    // }
}
