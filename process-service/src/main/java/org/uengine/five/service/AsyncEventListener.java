package org.uengine.five.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.contexts.EventSynchronization;
import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.FieldDescriptor;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.five.dto.RoleMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.uengine.five.Streams;

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

    @StreamListener(Streams.INPUT)
    public void whatever(@Payload String eventString) {
        System.out.println("\n\n##### listener whatever : " + eventString + "\n\n");
    }

    @Transactional(rollbackFor = { Exception.class })
    @StreamListener(
        value = Streams.INPUT, 
        condition = "headers['type'] != null"
    )
    @ProcessTransactional
    public void wheneverEvent(@Payload String eventBody, @Header("type") String typeHeader) {
        System.out.println("\n\n##### listener wheneverEvent : " + eventBody + "\n\n");
        try {

            HashMap<String,Object> eventContent = objectMapper.readValue(eventBody, HashMap.class);
            EventMappingEntity eventMappingEntity = eventMappingRepository.findEventMappingByEventType(typeHeader);
            
            if(eventMappingEntity == null ) 
                return;

            String corrKey = eventMappingEntity.getCorrelationKey();
            String coorKeyValue = eventContent.get(corrKey).toString();
            
            if(eventMappingEntity.isStartEvent()){
                // START
                String startDefId = eventMappingEntity.getDefinitionId();
                ProcessExecutionCommand processExecutionCommand = new ProcessExecutionCommand();
                processExecutionCommand.setProcessDefinitionId(startDefId);
                processExecutionCommand.setCorrelationKeyValue(coorKeyValue);
        
                instanceService.start(processExecutionCommand);
            } 

            // NEXT
            // String tracingTag = eventMappingEntity.getTracingTag();
            List<ProcessInstanceEntity> processInstanceList = processInstanceRepository.findByCorrKeyAndStatus(coorKeyValue, "Running");
            for(ProcessInstanceEntity processInstanceEntity : processInstanceList){
                ProcessInstance instance = instanceServiceImpl.getProcessInstanceLocal(processInstanceEntity.getInstId().toString());
            
                for (Activity activity: instance.getCurrentRunningActivities()){
                    if(activity.getEventSynchronization() != null && activity.getEventSynchronization().getEventType().equals(typeHeader)){
                        // set eventData.
                        // ((DefaultProcessInstance)instance).setProperty(activity.getTracingTag(), "eventJson", (Serializable) eventContent);
                        ((DefaultProcessInstance)instance).set(activity.getTracingTag(), "eventData", (Serializable) eventContent);

                        ReceiveActivity receiveActivity = (ReceiveActivity) activity;
                        receiveActivity.fireReceived(instance, eventContent);
                        break;
                    }
                } 
            }    
        } catch (Exception e) {
            // Acknowledgment acknowledgment = eventBody.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            // acknowledgment.acknowledge();
            throw new RuntimeException("Error wheneverEvent :" + e.getMessage(), e); 
        }
       
    }
}
