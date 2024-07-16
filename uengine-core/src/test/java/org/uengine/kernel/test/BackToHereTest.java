package org.uengine.kernel.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityFilter;
import org.uengine.kernel.DefaultActivity;
import org.uengine.kernel.ExecutionScopeContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ParameterContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.ProcessVariableValue;
import org.uengine.kernel.Role;
import org.uengine.kernel.SensitiveActivityFilter;
import org.uengine.kernel.bpmn.EndEvent;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.kernel.bpmn.SubProcess;

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
        SubProcess subProcess = new SubProcess();
        subProcess.setTracingTag("subProcess");

        // Step 1: Declare a ProcessVariable
        ProcessVariable myVariable = new ProcessVariable();
        myVariable.setName("myVar");

        // Assuming you have a process instance 'instance'
        // Step 3: Set the ProcessVariableValue to the process instance
        // This step will be assumed to be done when the instance is created and
        // executed

        // Step 4: Set the ProcessVariable as the forEachVariable of the SubProcess
        subProcess.setForEachVariable(myVariable);

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

        HumanActivity activityBeforeSubProcess = new HumanActivity();
        activityBeforeSubProcess.setRole(processDefinition.getRole("reporter"));
        activityBeforeSubProcess.setTracingTag("activityBeforeSubProcess");
        activityBeforeSubProcess.setParameters(parameters);
        activityBeforeSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        
        processDefinition.addChildActivity(activityBeforeSubProcess);

        // subProcess를 processDefinition의 두 번째 요소로 추가
        
        processDefinition.addChildActivity(subProcess);

        // Event cancelEvent = new Event();
        // cancelEvent.setTracingTag("cancelEvent");
        // cancelEvent.setAttachedToRef("subProcess");
        // // cancelEvent를 processDefinition의 요소로 추가
        // processDefinition.addChildActivity(cancelEvent);

        Event subStartEvent = new StartEvent();
        subStartEvent.setTracingTag("subStartEvent");
        subProcess.addChildActivity(subStartEvent);
        // DefaultActivity를 ReceiveActivity로 변경
        
        HumanActivity activityWithinSubProcess = new HumanActivity();
        activityWithinSubProcess.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess.setTracingTag("activityWithinSubProcess");
        activityWithinSubProcess.setParameters(parameters);
        activityWithinSubProcess.setMessage("receive"); // 이벤트를 받기 위한 설정
        subProcess.addSequenceFlow(new SequenceFlow("subStartEvent", "activityWithinSubProcess"));
        subProcess.addChildActivity(activityWithinSubProcess);

        HumanActivity activityWithinSubProcess2 = new HumanActivity();
        activityWithinSubProcess2.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess2.setTracingTag("activityWithinSubProcess2");
        activityWithinSubProcess2.setParameters(parameters);
        activityWithinSubProcess2.setMessage("receive"); // 이벤트를 받기 위한 설정
        subProcess.addChildActivity(activityWithinSubProcess2);
        subProcess.addSequenceFlow(new SequenceFlow("activityWithinSubProcess", "activityWithinSubProcess2"));

        HumanActivity activityWithinSubProcess3 = new HumanActivity();
        activityWithinSubProcess3.setRole(processDefinition.getRole("reporter"));
        activityWithinSubProcess3.setTracingTag("activityWithinSubProcess3");
        activityWithinSubProcess3.setParameters(parameters);
        activityWithinSubProcess3.setMessage("receive"); // 이벤트를 받기 위한 설정
        subProcess.addChildActivity(activityWithinSubProcess3);
        subProcess.addSequenceFlow(new SequenceFlow("activityWithinSubProcess2", "activityWithinSubProcess3"));

        Event subEndEvent = new EndEvent();
        subEndEvent.setTracingTag("subEndEvent");
        subProcess.addSequenceFlow(new SequenceFlow("activityWithinSubProcess3", "subEndEvent"));
        subProcess.addChildActivity(subEndEvent);

        DefaultActivity activityAfterSubProcess = new DefaultActivity();
        activityAfterSubProcess.setTracingTag("activityAfterSubProcess");
        processDefinition.addChildActivity(activityAfterSubProcess);

        // 시퀀스 플로우 구성
        processDefinition.addSequenceFlow(new SequenceFlow("startEvent", "activityBeforeSubProcess"));
        processDefinition.addSequenceFlow(new SequenceFlow("activityBeforeSubProcess", "subProcess"));
        processDefinition.addSequenceFlow(new SequenceFlow("subProcess", "activityAfterSubProcess"));

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
        pvv.moveToAdd();
        pvv.setValue("value2");
        pvv.moveToAdd();
        pvv.setValue("value3");

        // Step 3: Set the ProcessVariableValue to the process instance
        instance.set("", "myVar", pvv);
        instance.putRoleMapping("reporter", "reporter@uengine.org");
        instance.execute();
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
        instance.setExecutionScope("0");
        assertEquals(instance.getStatus("activityWithinSubProcess"), "Running");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityWithinSubProcess2"), "Running");
        instance.getProcessDefinition().fireMessage("receive", instance, "receive");
        assertEquals(instance.getStatus("activityWithinSubProcess3"), "Running");
        instance.getProcessDefinition().getActivity("activityWithinSubProcess").backToHere(instance);
        
        assertEquals(instance.getStatus("activityWithinSubProcess"), "Running");
        // }

    }
}