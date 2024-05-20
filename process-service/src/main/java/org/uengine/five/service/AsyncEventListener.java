package org.uengine.five.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.contexts.EventSynchronization;
import org.uengine.five.config.kafka.KafkaProcessor;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.FieldDescriptor;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.five.dto.RoleMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

// import javax.transaction.Transactional;

@Service
public class AsyncEventListener {
 
    static ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
    
    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    InstanceService instanceService;

    @Autowired
    InstanceServiceImpl instanceServiceImpl;

    @Autowired
    DefinitionServiceUtil definitionService;
    
    @Autowired
    EventMappingRepository eventMappingRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
        System.out.println("\n\n##### listener whatever : " + eventString + "\n\n");
    }

    @Transactional(rollbackFor = { Exception.class })
    @ProcessTransactional
    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type'] != null")
    public void wheneverEvent(@Payload String eventBody, @Header("type") String typeHeader) {
        System.out.println("\n\n##### listener wheneverEvent : " + eventBody + "\n\n");
        try {
            HashMap<String,Object> eventContent = objectMapper.readValue(eventBody, HashMap.class);
            EventMappingEntity eventMappingEntity = eventMappingRepository.findEventMappingByEventType(typeHeader);
            String startDefId = eventMappingEntity.getDefinitionId();
            String corrKey = eventMappingEntity.getCorrelationKey();

            if(eventMappingEntity.isStartEvent()){
                // START
                ProcessDefinition definition = (ProcessDefinition)definitionService.getDefinition(startDefId);
                String coorKeyValue = null;

                for (Activity act : definition.getStartActivities()){
                    if(act.getEventSynchronization() != null){
                        FieldDescriptor[] attributes = act.getEventSynchronization().getAttributes();
                        FieldDescriptor[] corrKeyFields = Arrays.stream(attributes).filter(FieldDescriptor::getIsCorrKey).toArray(FieldDescriptor[]::new);
                        if (corrKeyFields.length > 0) {
                            String corrKeyName = corrKeyFields[0].getName();
                            coorKeyValue = (String) eventContent.get(corrKeyName);
                        }
                    }
                }
               
                ProcessExecutionCommand processExecutionCommand = new ProcessExecutionCommand();
                processExecutionCommand.setProcessDefinitionId(startDefId);
                processExecutionCommand.setCorrelationKey(coorKeyValue);
            
                // RoleMapping roleMapping = new RoleMapping();
                // roleMapping.setName("initiator");
                // roleMapping.setEndpoints(new String[] { "initiator@uengine.org" });
                // roleMapping.setResourceNames(new String[] { "Initiator" });
                // processExecutionCommand.setRoleMappings(new RoleMapping[] { roleMapping });
                
                instanceService.start(processExecutionCommand);
            } else {
                // NEXT
                List<ProcessInstanceEntity> processInstanceList = processInstanceRepository.findByCorrKeyAndStatus(corrKey, "Running");

                if(processInstanceList == null || processInstanceList.size() == 0) {
                    System.out.println("No running process instance found for correlation key: " + corrKey);
                    return;
                }

                for(ProcessInstanceEntity processInstanceEntity : processInstanceList){
                    ProcessInstance instance = instanceServiceImpl.getProcessInstanceLocal(processInstanceEntity.getInstId().toString());
                    for (Activity activity: instance.getCurrentRunningActivities()){
                        if(activity.getEventSynchronization() != null && activity.getEventSynchronization().getEventType().equals(typeHeader)){
                            ReceiveActivity receiveActivity = (ReceiveActivity) activity;
                            receiveActivity.fireReceived(instance, eventContent);
                            break;
                        }
                    } 
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error wheneverEvent :" + e.getMessage(), e); 
            // e.printStackTrace();
        }
       
    }
}
