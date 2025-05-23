package org.uengine.five.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.five.Streams;
import org.uengine.five.dto.ProcessExecutionCommand;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.entity.ProcessInstanceEntity;
import org.uengine.five.framework.ProcessTransactional;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.repository.ProcessInstanceRepository;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.bpmn.Event;

import com.fasterxml.jackson.databind.ObjectMapper;

// import javax.transaction.Transactional;

@EnableBinding(Streams.class)
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
    @StreamListener(value = Streams.INPUT, condition = "headers['type'] != null")
    @ProcessTransactional
    public void wheneverEvent(@Payload String eventBody, @Header("type") String typeHeader) {
        System.out.println("\n\n##### listener wheneverEvent : " + eventBody + "\n\n");
        try {

            HashMap<String, Object> eventContent = objectMapper.readValue(eventBody, HashMap.class);
            EventMappingEntity eventMappingEntity = eventMappingRepository.findEventMappingByEventType(typeHeader);

            if (eventMappingEntity == null)
                throw new Exception("EventMappingEntity is null");

            String corrKey = eventMappingEntity.getCorrelationKey();

            if (eventContent.get(corrKey) != null) {
                String coorKeyValue = eventContent.get(corrKey).toString();

                if (eventMappingEntity.isStartEvent()) {
                    // START
                    String startDefId = eventMappingEntity.getDefinitionId();
                    ProcessExecutionCommand processExecutionCommand = new ProcessExecutionCommand();
                    processExecutionCommand.setProcessDefinitionId(startDefId);
                    processExecutionCommand.setCorrelationKeyValue(coorKeyValue);

                    instanceService.start(processExecutionCommand);
                }

                // NEXT
                // String tracingTag = eventMappingEntity.getTracingTag();
                List<ProcessInstanceEntity> processInstanceList = processInstanceRepository
                        .findByCorrKeyAndStatus(coorKeyValue, "Running");
                for (ProcessInstanceEntity processInstanceEntity : processInstanceList) {
                    ProcessInstance instance = instanceServiceImpl
                            .getProcessInstanceLocal(processInstanceEntity.getInstId().toString());

                    for (Activity activity : instance.getCurrentRunningActivities()) {
                        if (activity.getEventSynchronization() != null
                                && activity.getEventSynchronization().getEventType().equals(typeHeader)) {
                            // set eventData.
                            // ((DefaultProcessInstance)instance).setProperty(activity.getTracingTag(),
                            // "eventJson", (Serializable) eventContent);
                            ((DefaultProcessInstance) instance).set(activity.getTracingTag(), "eventData",
                                    (Serializable) eventContent);

                            ReceiveActivity receiveActivity = (ReceiveActivity) activity;
                            receiveActivity.fireReceived(instance, eventContent);
                            break;
                        }
                    }
                }
            } else {
                String coorKeyValue = corrKey;
                if (eventMappingEntity.isStartEvent()) {
                    // START
                    String startDefId = eventMappingEntity.getDefinitionId();
                    ProcessExecutionCommand processExecutionCommand = new ProcessExecutionCommand();
                    processExecutionCommand.setProcessDefinitionId(startDefId);
                    processExecutionCommand.setCorrelationKeyValue(coorKeyValue);

                    instanceService.start(processExecutionCommand);
                } else {
                    // 시작 이벤트가 아닐 경우 모든 인스턴스에서 이벤트를 발생시킨다.
                    List<ProcessInstanceEntity> processInstanceList = processInstanceRepository
                            .findByStatus("Running");
                    for (ProcessInstanceEntity processInstanceEntity : processInstanceList) {
                        ProcessInstance instance = instanceServiceImpl
                                .getProcessInstanceLocal(processInstanceEntity.getInstId().toString());

                        for (Activity activity : instance.getCurrentRunningActivities()) {
                            if (activity instanceof Event) {
                                Event event = (Event) activity;
                                if (event.getEventKey() != null &&
                                        event.getEventKey().equals(typeHeader) &&
                                        !Event.THROW_EVENT.equals(event.getEventType())) {
                                    event.onMessage(instance, event.getTracingTag());
                                }
                                break;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            // Acknowledgment acknowledgment =
            // eventBody.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
            // Acknowledgment.class);
            // acknowledgment.acknowledge();
            throw new RuntimeException("Error wheneverEvent :" + e.getMessage(), e);
        }

    }
}
