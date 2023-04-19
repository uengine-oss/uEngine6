package org.uengine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.contexts.TextContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.rpa.RPAActivity;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.EventHandler;
import org.uengine.kernel.KeyedParameter;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ResultPayload;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;


@SpringBootTest(classes = ProcessServiceApplication.class)
public class KernelTest {

    @Autowired
    ApplicationContext applicationContext;


    @Test
    void contextLoads() {
        System.out.println("sdastsa");
    }

	@Test
	public void runBasicProcess() {

        ProcessDefinition processDefinition = new ProcessDefinition();

        ProcessVariable variable1 = new ProcessVariable();
        variable1.setName("var1");


        StartEvent act1 = new StartEvent();
        act1.setName("start");

        processDefinition.addChildActivity(act1);

        RPAActivity act2 = new RPAActivity();
        act2.setName("act2");
        act2.setArgument(ProcessVariable.forName("var1"));
        
        ParameterContext parameterContext = new ParameterContext();
        parameterContext.setArgument(new TextContext());
        parameterContext.getArgument().setText("parameter1");
        parameterContext.setVariable(ProcessVariable.forName("var1"));

        act2.setParameters(new ParameterContext[]{
            parameterContext
        });

        processDefinition.addChildActivity(act2);

        SequenceFlow sequence = new SequenceFlow();
        sequence.setSourceActivity(act1);
        sequence.setTargetActivity(act2);

        processDefinition.addSequenceFlow(sequence);

        processDefinition.setProcessVariables(new ProcessVariable[]{variable1});
        

        processDefinition.afterDeserialization();
        


        org.uengine.kernel.ProcessInstance instance = applicationContext.getBean(
            org.uengine.kernel.ProcessInstance.class,
            //new Object[]{
            processDefinition,
            null,
            null
            //}
        );

        try {
            String testValue = "Test Value";
            instance.set("", "var1", testValue);
            instance.execute();
            Object value2 = instance.get("var1");
            assertEquals(value2, testValue + "_");


            //// 단계 완료시키기 (python 에서 이벤트가 왔을때 bpm 단계 완료 처리하고 다음단계 액티비티로 넘기는)
            ResultPayload rp = new ResultPayload();
            String changedValue = "value is changed by receiving result";
            rp.setProcessVariableChanges(new KeyedParameter[]{
                new KeyedParameter("var1", changedValue)
            });

            act2.fireReceived(instance, rp);

            Object value3 = instance.get("var1");

            String statusOfAct2 = act2.getStatus(instance);

            assertEquals(statusOfAct2, Activity.ACTIVITY_DONE);

            assertEquals(value3, changedValue);

            // EventHandler[] handlers = instance.getEventHandlersInAction();

            // for(EventHandler handler : handlers){
            //     System.out.println(handler);
            // }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

	}

}