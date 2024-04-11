package org.uengine.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.contexts.UserContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.Message;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.RoleMapping;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.overriding.SpringComponentFactory;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.Activity;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.bpmn.CallActivity;

@SpringBootTest(classes = ProcessServiceApplication.class)
public class InstanceServiceImplTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DefinitionServiceUtil definitionService;

    @InjectMocks
    @Autowired
    private InstanceServiceImpl instanceService;

    @Autowired
    private WorklistRepository worklistRepository;

    @BeforeEach
    public void setup() {
        ProcessServiceApplication.applicationContext = applicationContext;
        GlobalContext.setComponentFactory(new SpringComponentFactory());

        UserContext.getThreadLocalInstance().setUserId("initiator@uengine.org");
        UserContext.getThreadLocalInstance().setScopes(new ArrayList<String>());
        UserContext.getThreadLocalInstance().getScopes().add("manager");
    }

    @Test
    public void testRunDefinition() throws Exception {
        ProcessExecutionCommand command = new ProcessExecutionCommand();
        // command.setProcessDefinitionId("sales/simpleProcess.xml");
        command.setProcessDefinitionId("sales/testProcess.xml");
        command.setSimulation(false);

        // RoleMapping roleMapping = new RoleMapping();
        // roleMapping.setName("initiator");
        // roleMapping.setEndpoints(new String[] { "initiator@uengine.org" });
        // roleMapping.setResourceNames(new String[] { "Initiator" });
        // command.setRoleMappings(new RoleMapping[] { roleMapping });

        InstanceResource instanceResource = instanceService.start(command);
        String instanceId = instanceResource.getInstanceId();

        Map<String, Object> variables = instanceService.getProcessVariables(instanceId);

        System.out.println(variables);// Add assertions to verify the behavior

        ProcessInstance instance = instanceService.getProcessInstanceLocal(instanceId);

        assertEquals("Task_b should be in RUNNING status", Activity.STATUS_RUNNING, instance.getStatus("Task_b"));

        HumanActivity taskB = (HumanActivity) instance.getProcessDefinition().getActivity("Task_b");
        String[] taskIds = taskB.getTaskIds(instance);
        boolean worklistExists = false;

        // for (String taskId : taskIds) {
        // Optional<WorklistEntity> worklistEntity =
        // worklistRepository.findById(Long.parseLong(taskId));
        // if (worklistEntity.isPresent()) {
        // worklistExists = true;
        // break;
        // }

        List<WorklistEntity> worklistEntity = worklistRepository.findToDo();
        if (worklistEntity.size() > 0) {
            worklistExists = true;
            // break;
        }
        // }
        assertTrue("Worklist for Task_b should exist", worklistExists);

        UserContext.getThreadLocalInstance().setUserId("otheruser@uengine.org");
        worklistEntity = worklistRepository.findToDo();
        assertTrue("Worklist for otheruser@uengine.org should be empty", worklistEntity.size() == 0);

        String symptom = "An example error symptom description";
        WorkItemResource workItemResource = new WorkItemResource();
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("symptom", symptom);
        parameterValues.put("troubleType", "sw");

        workItemResource.setParameterValues(parameterValues);
        instanceService.putWorkItem(taskIds[0], workItemResource);

        assertEquals(symptom, instance.get("", "symptom"));

        assertEquals("Task_c should be in RUNNING status", Activity.STATUS_RUNNING, instance.getStatus("Task_c"));

        instanceService.postMessage(instanceId, new Message("Receive", null));

        assertEquals("Task_c should be in COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Task_c"));
        assertNotEquals("Task_d should not be in COMPLETED status", Activity.STATUS_COMPLETED,
                instance.getStatus("Task_d"));

        instanceService.backToHere(instanceId, "Task_b");

        assertEquals("Task_c should be in Ready status",
                Activity.STATUS_READY,
                instance.getStatus("Task_c"));

        parameterValues.put("troubleType", "hw");

        taskIds = taskB.getTaskIds(instance);

        workItemResource.setParameterValues(parameterValues);
        instanceService.putWorkItem(taskIds[0], workItemResource);

        assertEquals("Task_d should be in Running status", Activity.STATUS_RUNNING,
                instance.getStatus("Task_d"));

        CallActivity callActivity = (CallActivity) instance.getProcessDefinition().getActivity("Task_d");
        Vector ids = callActivity.getSubprocessIds(instance);

        ProcessInstance subProcessInstance = instanceService.getProcessInstanceLocal(ids.get(0).toString());
        // taskIds =
        // ((HumanActivity)subProcessInstance.getProcessDefinition().getActivity("Task_b")).getTaskIds(subProcessInstance);

        assertEquals(subProcessInstance.getRoleMapping("initiator").getEndpoint(),
                instance.getRoleMapping("initiator").getEndpoint());

        System.out.println(ids);

        // assertEquals("Process instance should be completed",
        // Activity.STATUS_COMPLETED, instance.getStatus());

    }

    @Test
    public void testBpmnDefinition() throws Exception {
        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId("sales/testProcess.xml");
        command.setSimulation(false);

        RoleMapping roleMapping = new RoleMapping();
        roleMapping.setName("initiator");
        roleMapping.setEndpoints(new String[] { "initiator@uengine.org" });
        roleMapping.setResourceNames(new String[] { "Initiator" });
        command.setRoleMappings(new RoleMapping[] { roleMapping });

        InstanceResource instanceResource = instanceService.start(command);
        String instanceId = instanceResource.getInstanceId();

        Map<String, Object> variables = instanceService.getProcessVariables(instanceId);

        System.out.println(variables);// Add assertions to verify the behavior

        ProcessInstance instance = instanceService.getProcessInstanceLocal(instanceId);

        assertEquals("Task_b should be in RUNNING status", Activity.STATUS_RUNNING, instance.getStatus("Task_b"));

        HumanActivity taskB = (HumanActivity) instance.getProcessDefinition().getActivity("Task_b");
        String[] taskIds = taskB.getTaskIds(instance);
        boolean worklistExists = false;
        for (String taskId : taskIds) {
            Optional<WorklistEntity> worklistEntity = worklistRepository.findById(Long.parseLong(taskId));
            if (worklistEntity.isPresent()) {
                worklistExists = true;
                break;
            }
        }
        assertTrue("Worklist for Task_b should exist", worklistExists);

        String symptom = "An example error symptom description";
        WorkItemResource workItemResource = new WorkItemResource();
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("symptom", symptom);
        parameterValues.put("troubleType", "sw");

        workItemResource.setParameterValues(parameterValues);
        instanceService.putWorkItem(taskIds[0], workItemResource);

        assertEquals(symptom, instance.get("", "symptom"));

        assertEquals("Task_c should be in RUNNING status", Activity.STATUS_RUNNING, instance.getStatus("Task_c"));

        instanceService.postMessage(instanceId, new Message("Receive", null));

        assertEquals("Task_c should be in COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Task_c"));
        assertNotEquals("Task_d should not be in COMPLETED status", Activity.STATUS_COMPLETED,
                instance.getStatus("Task_d"));

        instanceService.backToHere(instanceId, "Task_b");

        assertEquals("Task_c should be in Ready status",
                Activity.STATUS_READY,
                instance.getStatus("Task_c"));

        parameterValues.put("troubleType", "hw");

        taskIds = taskB.getTaskIds(instance);

        workItemResource.setParameterValues(parameterValues);
        instanceService.putWorkItem(taskIds[0], workItemResource);

        assertEquals("Task_d should be in Running status", Activity.STATUS_RUNNING,
                instance.getStatus("Task_d"));

        CallActivity callActivity = (CallActivity) instance.getProcessDefinition().getActivity("Task_d");
        Vector ids = callActivity.getSubprocessIds(instance);

        ProcessInstance subProcessInstance = instanceService.getProcessInstanceLocal(ids.get(0).toString());
        // taskIds =
        // ((HumanActivity)subProcessInstance.getProcessDefinition().getActivity("Task_b")).getTaskIds(subProcessInstance);

        assertEquals(subProcessInstance.getRoleMapping("initiator").getEndpoint(),
                instance.getRoleMapping("initiator").getEndpoint());

        System.out.println(ids);

        // assertEquals("Process instance should be completed",
        // Activity.STATUS_COMPLETED, instance.getStatus());

    }
}