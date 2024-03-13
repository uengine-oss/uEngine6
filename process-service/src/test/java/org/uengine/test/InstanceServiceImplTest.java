package org.uengine.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;

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

    @Test
    public void testRunDefinition() throws Exception {

        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId("sales/simpleProcess.xml");
        command.setSimulation(false);

        RoleMapping roleMapping = RoleMapping.create();
        roleMapping.setName("initiator");
        roleMapping.setEndpoint("initiator@uengine.org");
        roleMapping.setResourceName("Initiator");
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
        workItemResource.setParameterValues(parameterValues);
        instanceService.putWorkItem(taskIds[0], workItemResource);

        assertEquals(symptom, instance.get("", "symptom"));

        assertEquals("Process instance should be completed", Activity.STATUS_COMPLETED, instance.getStatus());
    }
}