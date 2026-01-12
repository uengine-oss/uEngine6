package org.uengine.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.uengine.contexts.HtmlFormContext;
import org.uengine.contexts.UserContext;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.dto.RoleMapping;
import org.uengine.five.dto.WorkItemResource;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.overriding.SpringComponentFactory;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ExecutionScopeContext;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessInstance;

@SpringBootTest(classes = ProcessServiceApplication.class)
public class SubProcessInSubTest {

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
    public void testTroubleReportFullTestInSub2() throws Exception {
        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId("TroubleReportFullTestInSub.bpmn");
        // command.setSimulation(false);

        RoleMapping roleMapping = new RoleMapping();
        roleMapping.setName("manager");
        roleMapping.setEndpoints(new String[] { "manager" });
        roleMapping.setResourceNames(new String[] { "manager" });
        RoleMapping roleMapping1 = new RoleMapping();
        roleMapping1.setName("manager");
        roleMapping1.setEndpoints(new String[] { "manager" });
        roleMapping1.setResourceNames(new String[] { "manager" });
        RoleMapping roleMapping2 = new RoleMapping();
        roleMapping2.setName("manager");
        roleMapping2.setEndpoints(new String[] { "manager" });
        roleMapping2.setResourceNames(new String[] { "manager" });
        command.setRoleMappings(new RoleMapping[] { roleMapping, roleMapping1, roleMapping2 });

        InstanceResource instanceResource = instanceService.start(command);
        String instanceId = instanceResource.getInstanceId();
        String json = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test1\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리1\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test3\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test2\""
                + "},"
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test4\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리2\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test6\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test5\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";

        ProcessInstance instance = applicationContext.getBean(
                ProcessInstance.class,
                new Object[] {
                        null,
                        instanceId,
                        null
                });

        List<WorklistEntity> workList = worklistRepository.findToDo();
        for (WorklistEntity worklistEntity : workList) {
            WorkItemResource workItemResource = new WorkItemResource();
            workItemResource.setExecScope(worklistEntity.getExecScope());
            workItemResource.setParameterValues(new HashMap<String, Object>());
            instanceService.setVariableWithTaskId(instanceId, worklistEntity.getTaskId().toString(),
                    "TroubleReportForm", json);
            instanceService.putWorkItemComplete(worklistEntity.getTaskId().toString(), workItemResource, "false");
        }
        json = "{\"formDefId\":\"SelectWorker\",\"filePath\":\"SelectWorker.form\",\"valueMap\":{\"Worker1\":\"manager\",\"Worker2\":\"\",\"Worker3\":\"\",\"_type\":\"java.util.HashMap\"},\"_type\":\"org.uengine.contexts.HtmlFormContext\"}";
        workList = worklistRepository.findToDo();

        for (WorklistEntity worklistEntity : workList) {
            WorkItemResource workItemResource = new WorkItemResource();
            workItemResource.setExecScope(worklistEntity.getExecScope());
            workItemResource.setParameterValues(new HashMap<String, Object>());
            instanceService.setVariableWithTaskId(instanceId, worklistEntity.getTaskId().toString(),
                    "SelectWorkerForm", json);
            instanceService.putWorkItemComplete(worklistEntity.getTaskId().toString(), workItemResource, "false");
        }

        json = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test1\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리11\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test3\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test2\""
                + "},"
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test4\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리12\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test6\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test5\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";

        String json2 = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test1\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리21\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test3\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test2\""
                + "},"
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test4\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리22\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test6\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test5\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";

        workList = worklistRepository.findToDo();

        for (int i = 0; i < workList.size(); i++) {
            WorklistEntity worklistEntity = workList.get(i);
            WorkItemResource workItemResource = new WorkItemResource();
            workItemResource.setExecScope(worklistEntity.getExecScope());
            workItemResource.setParameterValues(new HashMap<String, Object>());
            instanceService.setVariableWithTaskId(instanceId, worklistEntity.getTaskId().toString(),
                    "TroubleReportForm", i == 0 ? json : json2);
            instanceService.putWorkItemComplete(worklistEntity.getTaskId().toString(), workItemResource, "false");
        }

        json = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test1\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리111\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test3\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test2\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";

        json2 = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test4\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리121\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test6\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test5\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";

        String json3 = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test1\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리211\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test3\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test2\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";
        String json4 = "{"
                + "\"formDefId\":\"troubleReport\","
                + "\"filePath\":\"troubleReport.form\","
                + "\"valueMap\":{"
                + "\"TroubleReport\":["
                + "{"
                + "\"file\":\"\","
                + "\"TroubleName\":\"test4\","
                + "\"TroubleWorker\":\"manager\","
                + "\"dueDate\":\"2024-06-30\","
                + "\"TroubleClear\":\"처리221\","
                + "\"_type\":\"java.util.HashMap\","
                + "\"TroubleReporter\":\"test6\","
                + "\"TroubleType\":\"trouble\","
                + "\"TroubleContent\":\"test5\""
                + "}"
                + "],"
                + "\"_type\":\"java.util.HashMap\""
                + "},"
                + "\"_type\":\"org.uengine.contexts.HtmlFormContext\""
                + "}";
        workList = worklistRepository.findToDo();

        for (int i = 0; i < workList.size(); i++) {
            WorklistEntity worklistEntity = workList.get(i);
            WorkItemResource workItemResource = new WorkItemResource();
            workItemResource.setExecScope(worklistEntity.getExecScope());
            workItemResource.setParameterValues(new HashMap<String, Object>());
            String variable = json;
            switch (i) {
                case 0:
                    variable = json;
                    break;
                case 1:
                    variable = json2;
                    break;
                case 2:
                    variable = json3;
                    break;
                case 3:
                    variable = json4;
                    break;

            }

            instanceService.setVariableWithTaskId(instanceId, worklistEntity.getTaskId().toString(),
                    "TroubleReportForm", variable);
            instanceService.putWorkItemComplete(worklistEntity.getTaskId().toString(), workItemResource, "false");
        }

        workList = worklistRepository.findToDo();
        List<ExecutionScopeContext> executionScopeContexts = instance.getExecutionScopeContexts();

        for (ExecutionScopeContext executionScopeContext : executionScopeContexts) {
            Object variable = ((DefaultProcessInstance) instance).getVariables()
                    .get(":" + executionScopeContext.getExecutionScope() + ":TroubleReportForm");
            HtmlFormContext htmlFormContext = (HtmlFormContext) variable;
            ArrayList valueMap = null;
            if (htmlFormContext.getValueMap().get("TroubleReport") instanceof ArrayList) {
                valueMap = (ArrayList) htmlFormContext.getValueMap().get("TroubleReport");
            }
            Object testString = "";
            System.out.println(testString);
            switch (executionScopeContext.getExecutionScope()) {
                case "0":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리11", testString);
                    testString = ((HashMap) valueMap.get(1)).get("TroubleClear");
                    assertEquals("처리12", testString);
                    break;
                case "1":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리21", testString);
                    testString = ((HashMap) valueMap.get(1)).get("TroubleClear");
                    assertEquals("처리22", testString);
                    break;
                case "2":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리111", testString);
                    break;
                case "3":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리121", testString);
                    break;
                case "4":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리211", testString);
                    break;
                case "5":
                    testString = ((HashMap) valueMap.get(0)).get("TroubleClear");
                    assertEquals("처리221", testString);
                    break;
            }
        }

        System.out.println(workList);
    }
}
