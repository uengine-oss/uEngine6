package org.uengine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.bpmn.SequenceFlow;

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

        DefaultActivity act1 = new DefaultActivity();
        act1.setName("act1");

        processDefinition.addChildActivity(act1);

        DefaultActivity act2 = new DefaultActivity();
        act2.setName("act2");

        processDefinition.addChildActivity(act2);

        SequenceFlow sequence = new SequenceFlow();
        sequence.setSourceActivity(act1);
        sequence.setTargetActivity(act2);

        processDefinition.addSequenceFlow(sequence);

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
            instance.execute();
            assertTrue(true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

	}

}