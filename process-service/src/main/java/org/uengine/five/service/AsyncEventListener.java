package org.uengine.five.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Payload;
import org.uengine.contexts.EventSynchronization;
import org.uengine.five.config.kafka.KafkaProcessor;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.five.dto.RoleMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

// import javax.transaction.Transactional;

// @Service
@Configuration
public class AsyncEventListener {
 
    static ObjectMapper objectMapper = BpmnXMLParser.createTypedJsonObjectMapper();
    
    @Autowired
    ProcessInstanceRepository processInstanceRepository;

    @Autowired
    InstanceServiceImpl instanceService;
    
    @Autowired
    EventMappingRepository eventMappingRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {
        System.out.println("\n\n##### listener whatever : " + eventString + "\n\n");
    }

    @StreamListener(value = KafkaProcessor.INPUT, condition = "headers['type'] != null")
    public void wheneverEvent(@Payload String eventString) {
        System.out.println("\n\n##### listener wheneverEvent : " + eventString + "\n\n");
        try {
           EventSynchronization eventSynchronization = objectMapper.readValue(eventString, EventSynchronization.class);

           String definitionId = eventMappingRepository.findDefinitionIdByEventType(eventSynchronization.getEventType());
           if(definitionId == null){
                // NEXT
                // String corrKey = "1";

                // List<ProcessInstanceEntity> processInstanceEntityList = processInstanceRepository.findByCorrKeyAndStatus(corrKey, "Running");
                // for(ProcessInstanceEntity processInstanceEntity : processInstanceEntityList) {
                //     System.out.println("processInstanceEntity.getProcessInstanceId() = " + processInstanceEntity);
                // }
           } else {
                // START
                ProcessExecutionCommand processExecutionCommand = new ProcessExecutionCommand();
             
                RoleMapping roleMapping = new RoleMapping();
                roleMapping.setName("initiator");
                roleMapping.setEndpoints(new String[] { "initiator@uengine.org" });
                roleMapping.setResourceNames(new String[] { "Initiator" });
                processExecutionCommand.setRoleMappings(new RoleMapping[] { roleMapping });
                
                processExecutionCommand.setProcessDefinitionId(definitionId);
                instanceService.start(processExecutionCommand);
           }
        } catch (Exception e) {
            throw new RuntimeException("Error wheneverEvent :" + e.getMessage(), e);
        }
       
    }
}
