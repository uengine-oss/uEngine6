package org.uengine.kernel.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.uengine.contexts.EventSynchronization;
import org.uengine.contexts.MappingContext;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityFilter;
import org.uengine.kernel.Evaluate;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.MappingElement;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ProcessVariableValue;
import org.uengine.kernel.Role;
import org.uengine.kernel.SensitiveActivityFilter;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.ExclusiveGateway;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class BackToHereTest extends UEngineTest {

    ProcessDefinition processDefinition;

    /**
     * BPMN Text Diagram:
     * 
     * [StartEvent] --> (startEvent)
     * |
     * v
     * [SubProcess] --> (subProcess)
     * |
     * |--> [ReceiveActivity] --> (activityWithinSubProcess)
     * |
     * v
     * [Event] --> (cancelEvent)
     * |
     * v
     * [DefaultActivity] --> (activityAfterSubProcess)
     * 
     * Sequence Flows:
     * startEvent -> subProcess -> activityAfterSubProcess
     * 
     * Methods:
     * setUp(): Initializes the process definition with the specified BPMN elements
     * and sequence flows.
     */
    @Before
    public void setUp() throws Exception {
        processDefinition = new ProcessDefinition();
        processDefinition.setRoles(new Role[] { new Role("reporter") });
        processDefinition.setId("testId");
        // 서브프로세스 및 이벤트 구성
        // SubProcess subProcess = new SubProcess();
        // processDefinition.setTracingTag("subProcess");

        // Step 1: Declare a ProcessVariable
        ProcessVariable myVariable = new ProcessVariable();
        myVariable.setName("myVar");

        // Assuming you have a process instance 'instance'
        // Step 3: Set the ProcessVariableValue to the process instance
        // This step will be assumed to be done when the instance is created and
        // executed

        // Step 4: Set the ProcessVariable as the forEachVariable of the SubProcess
        // processDefinition.setForEachVariable(myVariable);

        Event startEvent = new StartEvent();
        startEvent.setTracingTag("startEvent");
        // startEvent를 processDefinition의 첫 번째 요소로 추가
        processDefinition.addChildActivity(startEvent);
        
        ParameterContext parameterContext = new ParameterContext();
        parameterContext.setDirection("OUT");
        TextContext textContext = new TextContext();
        textContext.setText("error");
        parameterContext.setArgument(textContext);
        parameterContext.setType("java.lang.String");
        parameterContext.setVariable(myVariable);
        ParameterContext[] parameters = new ParameterContext[] { parameterContext };
        ProcessVariable pv = new ProcessVariable();
        pv.setName("myVar");
        ProcessVariable[] pvs = new ProcessVariable[] {pv};
        processDefinition.setProcessVariables(pvs);
        MappingElement mappingElement = new MappingElement();
        mappingElement.setDirection("OUT");
        textContext.setText("error");
        mappingElement.setArgument(textContext);
        mappingElement.setType("java.lang.String");
        mappingElement.setVariable(myVariable);
        MappingContext mc = new MappingContext();
        mc.setMappingElements(new MappingElement[] {mappingElement});
        EventSynchronization es = new EventSynchronization();
        es.setMappingContext(mc);
        

        HumanActivity activityBeforeSubProcess = new HumanActivity();
        activityBeforeSubProcess.setRole(processDefinition.getRole("reporter"));
        activityBeforeSubProcess.setTracingTag("activityBeforeSubProcess");
        activityBeforeSubProcess.setEventSynchronization(es);
        activityBeforeSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        
        processDefinition.addChildActivity(activityBeforeSubProcess);

        SequenceFlow activityBeforeSubProcessSequenceFlow = new SequenceFlow("startEvent", "activityBeforeSubProcess");
        activityBeforeSubProcessSequenceFlow.setTracingTag("activityBeforeSubProcessSequenceFlow");
        processDefinition.addSequenceFlow(activityBeforeSubProcessSequenceFlow);
        // subProcess를 processDefinition의 두 번째 요소로 추가
        
        // processDefinition.addChildActivity(subProcess);

        // Event cancelEvent = new Event();
        // cancelEvent.setTracingTag("cancelEvent");
        // cancelEvent.setAttachedToRef("subProcess");
        // // cancelEvent를 processDefinition의 요소로 추가
        // processDefinition.addChildActivity(cancelEvent);

        // Event subStartEvent = new StartEvent();
        // subStartEvent.setTracingTag("subStartEvent");
        // processDefinition.addChildActivity(subStartEvent);
        // DefaultActivity를 ReceiveActivity로 변경
        
        HumanActivity activityWithinSubProcess = new HumanActivity();
        activityWithinSubProcess.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess.setTracingTag("activityWithinSubProcess");
        activityWithinSubProcess.setEventSynchronization(es);
        activityWithinSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        // SequenceFlow activityWithinSubProcessSequenceFlow = new SequenceFlow("subStartEvent", "activityWithinSubProcess");
        // activityWithinSubProcessSequenceFlow.setTracingTag("activityWithinSubProcessSequenceFlow");
        // processDefinition.addSequenceFlow(activityWithinSubProcessSequenceFlow);
        processDefinition.addChildActivity(activityWithinSubProcess);

        HumanActivity activityWithinSubProcess2 = new HumanActivity();
        activityWithinSubProcess2.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess2.setTracingTag("activityWithinSubProcess2");
        activityWithinSubProcess2.setEventSynchronization(es);
        activityWithinSubProcess2.setMessage("receive"); // 이벤트를 받기 위한 설정
        processDefinition.addChildActivity(activityWithinSubProcess2);
        SequenceFlow activityWithinSubProcess2SequenceFlow = new SequenceFlow("activityWithinSubProcess", "activityWithinSubProcess2");
        activityWithinSubProcess2SequenceFlow.setTracingTag("activityWithinSubProcess2SequenceFlow");
        processDefinition.addSequenceFlow(activityWithinSubProcess2SequenceFlow);

        HumanActivity activityWithinSubProcess3 = new HumanActivity();
        activityWithinSubProcess3.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess3.setTracingTag("activityWithinSubProcess3");
        activityWithinSubProcess3.setEventSynchronization(es);
        activityWithinSubProcess3.setMessage("receive"); // 이벤트를 받기 위한 설정
        processDefinition.addChildActivity(activityWithinSubProcess3);
        SequenceFlow activityWithinSubProcess3SequenceFlow = new SequenceFlow("activityWithinSubProcess2", "activityWithinSubProcess3");
        activityWithinSubProcess3SequenceFlow.setTracingTag("activityWithinSubProcess3SequenceFlow");
        processDefinition.addSequenceFlow(activityWithinSubProcess3SequenceFlow);

        // EndEvent subEndEvent = new EndEvent();
        // subEndEvent.setTracingTag("subEndEvent");
        SequenceFlow subEndEventSequenceFlow = new SequenceFlow("activityWithinSubProcess3", "activityAfterSubProcess");
        subEndEventSequenceFlow.setTracingTag("subEndEventSequenceFlow");
        processDefinition.addSequenceFlow(subEndEventSequenceFlow);
        // processDefinition.addChildActivity(subEndEvent);

        // DefaultActivity activityAfterSubProcess = new DefaultActivity();
        // activityAfterprocessDefinition.setTracingTag("activityAfterSubProcess");
        // processDefinition.addChildActivity(activityAfterSubProcess);

        HumanActivity activityAfterSubProcess = new HumanActivity();
        activityAfterSubProcess.setRole(processDefinition.getRole("reporter"));
        activityAfterSubProcess.setTracingTag("activityAfterSubProcess");
        activityAfterSubProcess.setEventSynchronization(es);
        activityAfterSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        processDefinition.addChildActivity(activityAfterSubProcess);

        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setTracingTag("exclusiveGateway");
        processDefinition.addChildActivity(exclusiveGateway);

        SequenceFlow sq = new SequenceFlow("activityAfterSubProcess", "exclusiveGateway");
        processDefinition.addSequenceFlow(sq);
        sq.setTracingTag("sq");
        

        SequenceFlow sq2 = new SequenceFlow("exclusiveGateway", "activityBeforeSubProcess");
        sq2.setTracingTag("sq2");
        sq2.setOtherwise(true);
        processDefinition.addSequenceFlow(sq2);
        HumanActivity backActivity2 = new HumanActivity();
        backActivity2.setRole(processDefinition.getRole("reporter"));
        backActivity2.setTracingTag("backActivity2");
        backActivity2.setEventSynchronization(es);
        backActivity2.setMessage("receive"); // 이벤트를 받기 위한 설정
        processDefinition.addChildActivity(backActivity2);

        SequenceFlow sq3 = new SequenceFlow("exclusiveGateway", "backActivity2");
        sq3.setTracingTag("sq3");
        Evaluate ev = new Evaluate();
        ev.setCondition("==");
        ev.setKey("myVar");
        ev.setValue("value1");
        sq3.setCondition(ev);
        exclusiveGateway.setDefaultFlow("sq2");
        processDefinition.addSequenceFlow(sq3);
        // 시퀀스 플로우 구성
        

        SequenceFlow subProcessSequenceFlow = new SequenceFlow("activityBeforeSubProcess", "activityWithinSubProcess");
        subProcessSequenceFlow.setTracingTag("subProcessSequenceFlow");
        processDefinition.addSequenceFlow(subProcessSequenceFlow);

        // SequenceFlow activityAfterSubProcessSequenceFlow = new SequenceFlow("subProcess", "activityAfterSubProcess");
        // activityAfterSubProcessSequenceFlow.setTracingTag("activityAfterSubProcessSequenceFlow");
        // processDefinition.addSequenceFlow(activityAfterSubProcessSequenceFlow);

        processDefinition.afterDeserialization();
        processDefinition.setActivityFilters(new ActivityFilter[] {
                new SensitiveActivityFilter() {
                    @Override
                    public void onEvent(Activity activity, ProcessInstance instance, String eventName, Object payload)
                            throws Exception {
                        if (Activity.ACTIVITY_FAULT.equals(eventName)) {
                            /// do something when a fault occurs in activity execution
                        }
                    }

                    @Override
                    public void beforeExecute(Activity activity, ProcessInstance instance) throws Exception {
                    }

                    @Override
                    public void afterExecute(Activity activity, ProcessInstance instance) throws Exception {
                    }

                    @Override
                    public void afterComplete(Activity activity, ProcessInstance instance) throws Exception {
                        if (activity instanceof ProcessDefinition) {
                            assertExecutionPathEquals(new String[] {
                                    "a10", "a9", "a1", "a2", "a3", "a4", "a7", "a11", "a12"
                            }, instance);

                        }
                    }

                    @Override
                    public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName,
                            Object changedValue) throws Exception {

                    }

                    @Override
                    public void onDeploy(ProcessDefinition definition) throws Exception {

                    }
                }
        });
    }

    @Test
    public void testCancelEventWithinSubProcess() throws Exception {

        ProcessInstance instance = processDefinition.createInstance();
        // Step 2: Create a ProcessVariableValue instance and populate it
        ProcessVariableValue pvv = new ProcessVariableValue();
        pvv.setName("myVar");
        pvv.setValue("value1");
        // pvv.moveToAdd();
        // pvv.setValue("value2");
        // pvv.moveToAdd();
        // pvv.setValue("value3");

        // Step 3: Set the ProcessVariableValue to the process instance
        instance.set("", "myVar", "value1");
        instance.putRoleMapping("reporter", "reporter@uengine.org");
        instance.execute();
        instance.set("", "myVar", "value1");
        // 서브프로세스 내에서 취소 이벤트 발생
        // instance.getProcessDefinition().fireMessage("event", instance,
        // "cancelEvent");

        // assertExecutionPathEquals("After Cancel Event Triggered Within SubProcess",
        // new String[] {
        // "startEvent", "cancelEvent"
        // }, instance);

        // 서브프로세스 내의 ReceiveActivity를 트리거하여 진행
        
        // ExecutionScopeContext rootExecutionScopeContext = instance.getExecutionScopeContext();
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityBeforeSubProcess"), "Completed");
        // instance.setExecutionScope("0");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityWithinSubProcess"), "Completed");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityWithinSubProcess2"), "Completed");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityWithinSubProcess3"), "Completed");
        assertEquals(instance.getStatus("activityAfterSubProcess"), "Running");
        instance.set("","myVar", "value1");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityAfterSubProcess"), "Completed");
        assertEquals(instance.getStatus("exclusiveGateway"), "Completed");
        assertEquals(instance.getStatus("activityBeforeSubProcess"), "Completed");
        instance.getProcessDefinition().getActivity("activityWithinSubProcess").backToHere(instance);
        assertEquals(instance.getStatus("activityWithinSubProcess"), "Running");
        // }

    }
}