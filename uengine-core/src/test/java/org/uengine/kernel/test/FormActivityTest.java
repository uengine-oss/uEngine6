package org.uengine.kernel.test;

import java.util.HashMap;

import org.uengine.contexts.HtmlFormContext;
import org.uengine.contexts.MappingContext;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.FormActivity;
import org.uengine.kernel.MappingElement;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.Role;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.processdesigner.mapper.TransformerMapping;
import org.uengine.processdesigner.mapper.transformers.ConcatTransformer;
import org.uengine.processdesigner.mapper.transformers.DirectValueTransformer;

public class FormActivityTest extends UEngineTest {

    ProcessDefinition processDefinition;

    public void setUp() throws Exception {
        processDefinition = new ProcessDefinition();
        processDefinition.setId("trouble-ticket-process");

        ProcessVariable formVariable = new ProcessVariable();
        formVariable.setName("form");
        formVariable.setType(HtmlFormContext.class);

        ProcessVariable stringVariable = new ProcessVariable();
        stringVariable.setName("troubleType");
        stringVariable.setType(String.class);

        processDefinition.setProcessVariables(new ProcessVariable[] { formVariable, stringVariable });

        HtmlFormContext htmlFormContext = new HtmlFormContext();
        htmlFormContext.setFormDefId("form");
        formVariable.setDefaultValue(htmlFormContext);

        processDefinition.setRoles(new Role[] { new Role("reporter") });

        FormActivity formActivity = new FormActivity();
        formActivity.setTracingTag("formActivity");
        formActivity.setVariableForHtmlFormContext(formVariable);
        formActivity.setRole(processDefinition.getRole("reporter"));

        MappingElement parameterContext = new MappingElement();
        parameterContext.setArgument(TextContext.createInstance());
        parameterContext.getArgument().setText("troubleType");
        parameterContext.setTransformerMapping(new TransformerMapping());

        DirectValueTransformer directValueTransformer = new DirectValueTransformer();
        directValueTransformer.setValue("[PREFIX]");
        directValueTransformer.setType(String.class);

        TransformerMapping transformerMapping = new TransformerMapping();
        transformerMapping.setLinkedArgumentName("out");
        transformerMapping.setTransformer(directValueTransformer);

        ConcatTransformer concatTransformer = new ConcatTransformer();
        concatTransformer.setArgumentSourceMap(new HashMap<>());
        concatTransformer.getArgumentSourceMap().put("str1", transformerMapping);
        concatTransformer.getArgumentSourceMap().put("str2", "form.troubleType");
        parameterContext.getTransformerMapping().setTransformer(concatTransformer);

        // formActivity.setMappingContexts(new ParameterContext[] { parameterContext });
        MappingContext mappingContext = new MappingContext();
        mappingContext.setMappingElements(new MappingElement[] { parameterContext });
        formActivity.setMappingContext(mappingContext);

        Event startEvent = new StartEvent();
        startEvent.setTracingTag("startEvent");

        processDefinition.addChildActivity(startEvent);
        processDefinition.addChildActivity(formActivity);

        // 시퀀스 플로우 구성
        processDefinition.addSequenceFlow(new SequenceFlow("startEvent", "formActivity"));

        processDefinition.afterDeserialization();
    }

    public void testFormDataMapping() throws Exception {
        ProcessInstance instance = processDefinition.createInstance();

        instance.putRoleMapping("reporter", "reporter@uengine.org");
        instance.execute();

        // Step 2: Create a ProcessVariableValue instance and populate it

        HtmlFormContext htmlFormContext = new HtmlFormContext();
        htmlFormContext.setValueMap(new HashMap<>());
        htmlFormContext.getValueMap().put("troubletype", "sw");
        htmlFormContext.setFormDefId("troubleTicketForm");
        // Step 3: Set the ProcessVariableValue to the process instance
        instance.set("", "form", htmlFormContext);

        // 서브프로세스 내에서 취소 이벤트 발생
        String message = ((FormActivity) instance.getCurrentRunningActivity().getActivity()).getMessage();
        instance.getProcessDefinition().fireMessage(message, instance, "test");

        assertEquals("[PREFIX]sw", instance.get("", "troubleType"));

    }
}