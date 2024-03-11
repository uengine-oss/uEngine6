package org.uengine.test;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.command.ProcessExecutionCommand;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceResource;
import org.uengine.five.service.InstanceServiceImpl;
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

    @Test
    public void testRunDefinition() throws Exception {
        // Mock the definition service to return a simple process definition
        // ProcessDefinition processDefinition = new ProcessDefinition();
        // processDefinition.setId("simpleProcess");
        // processDefinition.setName("Simple Process");
        // // Add tasks and sequence flows as needed
        // Task taskA = new Task();
        // taskA.setTracingTag("Task_a");
        // processDefinition.addChildActivity(taskA);
        // // Add more tasks and sequence flows as needed

        // when(definitionService.getDefinition("simpleProcess.bpmn",
        // false)).thenReturn(processDefinition);

        // Call the method under test

        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId("simpleProcess.bpmn");
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

        // 이제 내가 해야할 것들: monitoring, suspend, resume, stop 등, dynamic change, mi, ...
    }
}