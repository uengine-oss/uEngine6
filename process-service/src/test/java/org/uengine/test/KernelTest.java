package org.uengine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.rpa.RPAActivity;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessVariable;
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
        act1.setName("act1");

        processDefinition.addChildActivity(act1);

        RPAActivity act2 = new RPAActivity();
        act2.setName("act2");
        act2.setArgument(ProcessVariable.forName("variable1"));

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
            instance.set("", "variable1", testValue);
            instance.execute();
            Object value2 = instance.get("variable1");
            assertEquals(value2, testValue + "_");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

	}

}