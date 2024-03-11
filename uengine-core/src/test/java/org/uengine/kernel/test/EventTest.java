package org.uengine.kernel.test;

import java.util.Vector;

import org.uengine.kernel.AbstractProcessInstance;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;

public class EventTest extends UEngineTest {

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
    public void setUp() throws Exception {

        AbstractProcessInstance.USE_CLASS = DefaultProcessInstance.class;

        processDefinition = new ProcessDefinition();

        // StartEvent
        Event startEvent = new StartEvent();
        startEvent.setTracingTag("a1");
        processDefinition.addChildActivity(startEvent);

        // ReceiveActivity 2
        ReceiveActivity receiveActivity2 = new ReceiveActivity();
        receiveActivity2.setTracingTag("a2");
        receiveActivity2.setMessage("receive");
        processDefinition.addChildActivity(receiveActivity2);

        // DefaultActivity 3
        DefaultActivity defaultActivity3 = new DefaultActivity();
        defaultActivity3.setTracingTag("a3");
        processDefinition.addChildActivity(defaultActivity3);

        // Event 4 attached to a2
        Event attachedEvent4 = new Event();
        attachedEvent4.setName("a4");
        attachedEvent4.setTracingTag("a4");
        attachedEvent4.setAttachedToRef("a2");
        processDefinition.addChildActivity(attachedEvent4);

        // DefaultActivity 5
        DefaultActivity defaultActivity5 = new DefaultActivity();
        defaultActivity5.setTracingTag("a5");
        processDefinition.addChildActivity(defaultActivity5);

        // DefaultActivity 6
        DefaultActivity defaultActivity6 = new DefaultActivity();
        defaultActivity6.setTracingTag("a6");
        processDefinition.addChildActivity(defaultActivity6);

        // SequenceFlows
        processDefinition.addSequenceFlow(new SequenceFlow("a1", "a2"));
        processDefinition.addSequenceFlow(new SequenceFlow("a2", "a3"));

        processDefinition.addSequenceFlow(new SequenceFlow("a4", "a5"));
        processDefinition.addSequenceFlow(new SequenceFlow("a5", "a6"));

        processDefinition.afterDeserialization();

    }

    public void testEscalationEvent() throws Exception {
        ProcessInstance instance = processDefinition.createInstance();
        instance.execute();
        // 첫 실행 결과 검증: StartEvent부터 ReceiveActivity까지
        assertExecutionPathEquals("Initial Execution Path", new String[] {
                "a1"
        }, instance);

        //// 2가 실행되는 동안은 4가 여러번 실행가능

        Vector messageListeners = (Vector) instance.getMessageListeners("event");
        boolean hasA4Tag = messageListeners.contains("a4");
        assertTrue("Message listeners should contain an activity with a4 tracing tag", hasA4Tag);

        // 이벤트 발생시키기: 2에 부착된 4(Event)를 통해 실행
        instance.getProcessDefinition().fireMessage("event", instance, "a4");
        assertExecutionPathEquals("After First Event Triggered", new String[] {
                "a1", "a4", "a5", "a6"
        }, instance);

        // 같은 이벤트를 다시 한 번 더 발생시키기
        instance.getProcessDefinition().fireMessage("event", instance, "a4");
        assertExecutionPathEquals("After Second Event Triggered", new String[] {
                "a1", "a4", "a5", "a6", "a4", "a5", "a6"
        }, instance);

        //// 2가 완료되고 나면 4는 더이상 실행되지 않음
        instance.getProcessDefinition().fireMessage("receive", instance, null);
        assertExecutionPathEquals("After Third Event Triggered", new String[] {
                "a1", "a4", "a5", "a6", "a4", "a5", "a6", "a2", "a3"
        }, instance);

        // 4를 다시 실행시키면 5,6이 실행되지 않음
        instance.getProcessDefinition().fireMessage("event", instance, "a4");
        assertExecutionPathEquals("After Fourth Event Triggered", new String[] {
                "a1", "a4", "a5", "a6", "a4", "a5", "a6", "a2", "a3"
        }, instance);
    }

}
