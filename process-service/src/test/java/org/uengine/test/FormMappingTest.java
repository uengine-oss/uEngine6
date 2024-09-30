package org.uengine.test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.uengine.contexts.MappingContext;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.AbstractProcessInstance;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.FormActivity;
import org.uengine.kernel.MappingElement;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.kernel.test.UEngineTest;
import org.uengine.processdesigner.mapper.Transformer;
import org.uengine.processdesigner.mapper.TransformerMapping;
import org.uengine.processdesigner.mapper.transformers.MaxTransformer;
import org.uengine.processdesigner.mapper.transformers.ReplaceTransformer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FormMappingTest extends UEngineTest {

    ProcessDefinition processDefinition;

    /**
     * build a graph as follows:
     * [StartEvent(a1)] -> [ReceiveActivity(a2)] -> [DefaultActivity(a3)]
     ************************** |
     ************************** v
     ********************** [Event(a4)] -> [DefaultActivity(a5)] -> [DefaultActivity(a6)]
     * 
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        AbstractProcessInstance.USE_CLASS = DefaultProcessInstance.class;

        processDefinition = new ProcessDefinition();// 프로세스 변수 생성
        ProcessVariable test11 = new ProcessVariable();
        test11.setName("test11");
        test11.setType(Double.class);

        ProcessVariable test22 = new ProcessVariable();
        test22.setName("test22");
        test22.setType(Double.class);

        ProcessVariable test33 = new ProcessVariable();
        test33.setName("test33");
        test33.setType(Double.class);

        ProcessVariable testname333 = new ProcessVariable();
        testname333.setName("testname333");
        testname333.setType(String.class);

        ProcessVariable testname3333 = new ProcessVariable();
        testname3333.setName("testname3333");
        testname3333.setType(String.class);

        // 프로세스 변수를 processDefinition에 추가
        processDefinition
                .setProcessVariables(new ProcessVariable[] { test11, test22, test33, testname333, testname3333 });
        // StartEvent
        Event startEvent = new StartEvent();
        startEvent.setTracingTag("a1");
        processDefinition.addChildActivity(startEvent);

        // FormActivity 설정
        FormActivity formActivity = new FormActivity();
        formActivity.setTracingTag("a2");
        formActivity.setMessage("receive");
        // 예시: FormActivity에 필요한 추가 설정을 여기에 추가합니다.
        // formActivity.setFormFields(...); // 양식 필드 설정
        processDefinition.addChildActivity(formActivity);

        // SequenceFlows
        processDefinition.addSequenceFlow(new SequenceFlow("a1", "a2"));

        processDefinition.afterDeserialization();
    }

    // @Test
    // public void testDeserializeJson() throws Exception {
    // String resourcePath = "/org/uengine/test/mappingTest.json";
    // InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);

    // String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    // ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
    // MappingContext mappingContext = objectMapper.readValue(json,
    // MappingContext.class);

    // // MappingContext 인스턴스를 JSON 문자열로 직렬화
    // assertTrue(mappingContext.getMappingElements().length > 0);
    // for (ParameterContext mappingElement : mappingContext.getMappingElements()) {
    // assertTrue(mappingElement.getTransformerMapping().getTransformer() instanceof
    // Transformer);
    // }
    // }

    @Test
    public void testCreateMappingContext() throws Exception {
        MappingContext mappingContext = createMappingContext();
        assertNotNull(mappingContext);
        ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
        String sysout = objectMapper.writeValueAsString(mappingContext);

        System.out.println(sysout);
    }

    private static MappingContext createMappingContext() {
        // MaxTransformer 설정
        MaxTransformer maxTransformer = new MaxTransformer();
        maxTransformer.setName("Max");
        HashMap<String, String[]> maxArgumentSourceMap = new HashMap<>();
        maxArgumentSourceMap.put("value1", new String[] { "[instance].instanceId" });
        maxArgumentSourceMap.put("value2", new String[] { "[instance].name" });
        maxTransformer.setArgumentSourceMap(maxArgumentSourceMap);

        // ReplaceTransformer 설정
        ReplaceTransformer replaceTransformer = new ReplaceTransformer();
        replaceTransformer.setName("Replace");
        HashMap<String, String[]> replaceArgumentSourceMap = new HashMap<>();
        replaceArgumentSourceMap.put("input", new String[] { "[roles].Initiator" });
        replaceTransformer.setArgumentSourceMap(replaceArgumentSourceMap);
        replaceTransformer.setOldString("test");
        replaceTransformer.setRegularExp(false);

        // TransformerMapping 설정
        TransformerMapping maxTransformerMapping = new TransformerMapping();
        maxTransformerMapping.setTransformer(maxTransformer);
        maxTransformerMapping.setLinkedArgumentName("out");

        TransformerMapping replaceTransformerMapping = new TransformerMapping();
        replaceTransformerMapping.setTransformer(replaceTransformer);
        replaceTransformerMapping.setLinkedArgumentName("out");

        // MappingElement 설정
        MappingElement maxMappingElement = new MappingElement();
        maxMappingElement.setTransformerMapping(maxTransformerMapping);

        MappingElement replaceMappingElement = new MappingElement();
        replaceMappingElement.setTransformerMapping(replaceTransformerMapping);

        // MappingContext 설정
        MappingContext mappingContext = new MappingContext();
        mappingContext.setMappingElements(new MappingElement[] { maxMappingElement, replaceMappingElement });

        return mappingContext;
    }

    @Test
    public void testLetTransform() throws Exception {
        // MappingContext 인스턴스 로드 (이전 테스트에서 사용한 방법을 그대로 사용)
        String resourcePath = "/org/uengine/test/mappingTest.json";
        InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
        MappingContext mappingContext = objectMapper.readValue(json, MappingContext.class);

        AbstractProcessInstance.USE_CLASS = DefaultProcessInstance.class;

        ProcessInstance processInstance = processDefinition.createInstance();
        int passCount = 0;
        processInstance.set("", "test11", "11.5");
        processInstance.set("", "test22", "22.5");
        processInstance.set("", "test33", "33.5");
        processInstance.set("", "testname333", "11.5");
        processInstance.set("", "testname3333", "ko_KR");

        for (ParameterContext parameterContext : mappingContext.getMappingElements()) {
            MappingElement mappingElement = (MappingElement) parameterContext;
            Transformer transformer = mappingElement.getTransformerMapping().getTransformer();
            try {
                testTranformNumber(processInstance, transformer);
                passCount++;
            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed Transformer :  " + transformer.getClass().getName() + " - " + e.getMessage());
            }
        }

        assertTrue(passCount == mappingContext.getMappingElements().length);
    }

    void testTranformNumber(ProcessInstance processInstance, Transformer transformer) throws Exception {
        String className = transformer.getClass().getSimpleName();
        Object result = transformer.letTransform(processInstance, "outputArgumentName");

        // test11 11.5
        // test22 22.5
        // test33 33.5
        // testname333 11.5
        // testname3333 D

        switch (className) {
            case "AbsTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 11.5);
                break;
            case "CeilTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 12.0);
                break;
            case "MaxTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 22.5);
                break;
            case "MinTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 11.5);
                break;
            case "RoundTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 12.0);
                break;
            case "SumTransformer":
                assertTrue(Double.parseDouble(result.toString()) == 67.5);
                break;
            case "ConcatTransformer":
                assertTrue(result.toString().equals("11.522.533.5"));
                break;
            case "ReplaceTransformer":
                if (((ReplaceTransformer) transformer).isRegularExp()) {
                    assertTrue(result.toString().equals("**.*"));
                } else {
                    assertTrue(result.toString().equals("11.5"));
                }
                break;
            case "NumberFormatTransformer":
                assertTrue(result.toString().equals("11"));
                break;
        }
        assertNotNull(result);
    }
}
