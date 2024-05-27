package org.uengine.test;

import java.util.ArrayList;
import static org.junit.Assert.*;

import org.junit.Test; // junit4
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.overriding.SpringComponentFactory;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.repository.WorklistRepository;
// import org.uengine.five.service.AsyncEventListener;
import org.uengine.five.service.InstanceService;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.Activity;
// import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.uengine.five.config.kafka.KafkaProcessor;
// import org.uengine.five.dto.InstanceResource;
import org.uengine.five.dto.ProcessExecutionCommand;
// import org.springframework.cloud.stream.test.binder.MessageCollector;

import org.uengine.contexts.UserContext;

// @DirtiesContext
// @EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092"})
// @SpringBootTest(classes = {ProcessServiceApplication.class})
// @RunWith(SpringRunner.class)
// @DataJpaTest
// @SpringBootTest
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProcessServiceApplication.class)
public class AsyncEventListenerTest {

    @Autowired
    private ApplicationContext applicationContext;

    // @Autowired
    // private AsyncEventListener asyncEventListener;

    @Autowired
    private EventMappingRepository eventMappingRepository;

    @Autowired
    private KafkaProcessor processor;

    // @Autowired
    // private MessageCollector messageCollector;

    // @InjectMocks
    @Autowired
    private InstanceService instanceService;

    // @InjectMocks
    @Autowired
    private InstanceServiceImpl instanceServiceImpl;


    @BeforeEach
    public void setup() {
        ProcessServiceApplication.applicationContext = applicationContext;
        GlobalContext.setComponentFactory(new SpringComponentFactory());
        
        UserContext.getThreadLocalInstance().setUserId("initiator@uengine.org");
        UserContext.getThreadLocalInstance().setScopes(new ArrayList<String>());
        UserContext.getThreadLocalInstance().getScopes().add("manager");
    }

    @Test
    // @SuppressWarnings("unchecked")
    public void testStartEvent() {
        // ProcessServiceApplication.applicationContext = applicationContext;

        EventMappingEntity eventMappingEntity = new EventMappingEntity();
        eventMappingEntity.setEventType("TroubleTicketIssued");
        eventMappingEntity.setDefinitionId("/sales/troubleTicket.bpmn");
        eventMappingEntity.setCorrelationKey(null);
        eventMappingEntity.setIsStartEvent(true);
        eventMappingRepository.save(eventMappingEntity);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // START
            TroubleIssued troubleTicketIssued = new TroubleIssued();
            troubleTicketIssued.setId("1");
            troubleTicketIssued.setType("sw");
            troubleTicketIssued.setDescription("sw isn't working.");

            String msg = objectMapper.writeValueAsString(troubleTicketIssued);
            processor
                .inboundTopic()
                .send ( 
                    MessageBuilder
                        .withPayload(msg)
                        .setHeader(
                            MessageHeaders.CONTENT_TYPE,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                        .setHeader("type", troubleTicketIssued.getClass().getSimpleName())
                        .build()
                );
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testNextEvent() throws Exception {
        ProcessServiceApplication.applicationContext = applicationContext;
        GlobalContext.setComponentFactory(new SpringComponentFactory());
        String instanceId = "1";

        ProcessExecutionCommand command = new ProcessExecutionCommand();
        command.setProcessDefinitionId("sales/TroubleCenter.bpmn");
        command.setSimulation(false);
        command.setCorrelationKey(instanceId);
        instanceService.start(command);

      
        try {
            
            ProcessInstance instance = instanceServiceImpl.getProcessInstanceLocal(instanceId);
            ObjectMapper objectMapper = new ObjectMapper();

            EventMappingEntity eventMappingEntity = new EventMappingEntity();
            eventMappingEntity.setEventType("TroubleIssued");
            eventMappingEntity.setDefinitionId("sales/TroubleCenter.bpmn");
            eventMappingEntity.setCorrelationKey(instanceId);
            eventMappingEntity.setIsStartEvent(false);
            eventMappingRepository.save(eventMappingEntity);
    
            EventMappingEntity eventMappingEntity2 = new EventMappingEntity();
            eventMappingEntity2.setEventType("TroubleReceived");
            eventMappingEntity2.setDefinitionId("sales/TroubleCenter.bpmn");
            eventMappingEntity2.setCorrelationKey(instanceId);
            eventMappingEntity2.setIsStartEvent(false);
            eventMappingRepository.save(eventMappingEntity2);

            EventMappingEntity eventMappingEntity3 = new EventMappingEntity();
            eventMappingEntity3.setEventType("TroubleFixed");
            eventMappingEntity3.setDefinitionId("sales/TroubleCenter.bpmn");
            eventMappingEntity3.setCorrelationKey(instanceId);
            eventMappingEntity3.setIsStartEvent(false);
            eventMappingRepository.save(eventMappingEntity3);

            EventMappingEntity eventMappingEntity4 = new EventMappingEntity();
            eventMappingEntity4.setEventType("TroubleCompleted");
            eventMappingEntity4.setDefinitionId("sales/TroubleCenter.bpmn");
            eventMappingEntity4.setCorrelationKey(instanceId);
            eventMappingEntity4.setIsStartEvent(false);
            eventMappingRepository.save(eventMappingEntity4);
    
            
            // Start > TroubleIssued
            TroubleIssued troubleIssued = new TroubleIssued();
            troubleIssued.setId("1");
            troubleIssued.setType("sw");
            troubleIssued.setDescription("sw isn't working.");
            String msg = objectMapper.writeValueAsString(troubleIssued);
            processor
                .inboundTopic()
                .send(
                    MessageBuilder
                        .withPayload(msg)
                        .setHeader(
                            MessageHeaders.CONTENT_TYPE,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                        .setHeader("type", troubleIssued.getClass().getSimpleName())
                        .build()
                );
            assertEquals("TroubleIssued should be in STATUS_COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Activity_1js38su"));

            
            //TroubleIssued > TroubleReceived
            TroubleReceived troubleReceived = new TroubleReceived();
            troubleReceived.setId("1");
            troubleReceived.setType("sw");
            troubleReceived.setDescription("RECEIVE: 'sw isn't working.'");
            String msg2 = objectMapper.writeValueAsString(troubleReceived);
            processor
                .inboundTopic()
                .send(
                    MessageBuilder
                        .withPayload(msg2)
                        .setHeader(
                            MessageHeaders.CONTENT_TYPE,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                        .setHeader("type", troubleReceived.getClass().getSimpleName())
                        .build()
                );
            assertEquals("TroubleReceived should be in STATUS_COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Activity_171teqp"));


            // TroubleReceived > TroubleFixed
            TroubleFixed troubleFixed = new TroubleFixed();
            troubleFixed.setId("1");
            troubleFixed.setType("sw");
            troubleFixed.setDescription("sw is fixed.");
            String msg3 = objectMapper.writeValueAsString(troubleFixed);
            processor
                .inboundTopic()
                .send(
                    MessageBuilder
                        .withPayload(msg3)
                        .setHeader(
                            MessageHeaders.CONTENT_TYPE,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                        .setHeader("type", troubleFixed.getClass().getSimpleName())
                        .build()
                );
            assertEquals("TroubleFixed should be in STATUS_COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Activity_1hauzrn"));

            // TroubleFixed > TroubleCompleted
            TroubleCompleted troubleCompleted = new TroubleCompleted();
            troubleCompleted.setId("1");
            troubleCompleted.setType("sw");
            troubleCompleted.setDescription("sw is working.");
            String msg4 = objectMapper.writeValueAsString(troubleCompleted);
            processor
                .inboundTopic()
                .send(
                    MessageBuilder
                        .withPayload(msg4)
                        .setHeader(
                            MessageHeaders.CONTENT_TYPE,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                        .setHeader("type", troubleCompleted.getClass().getSimpleName())
                        .build()
                );
            assertEquals("TroubleCompleted should be in STATUS_COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus("Activity_17l2z7n"));


            // instance 종료 
            assertEquals("Instance should be in COMPLETED status", Activity.STATUS_COMPLETED, instance.getStatus());
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}



