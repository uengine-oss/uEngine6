package org.uengine.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.uengine.five.ProcessServiceApplication;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.service.AsyncEventListener;



// @DirtiesContext
// @EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092"})
// @SpringBootTest(classes = {ProcessServiceApplication.class})
// @RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
public class AsyncEventListenerTest {

    // @Autowired
    // AsyncEventListener asyncEventListener;

    @Autowired
    EventMappingRepository eventMappingRepository;

    // @Autowired
    // private KafkaConsumer consumer;

    // @Autowired
    // private KafkaProducer producer;

    // @Value("bpm-topic-test")
    // private String topicName;

    @Test
    public void testWheneverEvent() {
        AsyncEventListener asyncEventListener = new AsyncEventListener();
        // EventMappingRepository는 인스턴스화할 수 없으므로 @Autowired를 사용하여 주입받아야 합니다.
        // EventMappingRepository eventMappingRepository = new EventMappingRepository();

        // Mapping.
        EventMappingEntity eventMappingEntity = new EventMappingEntity();
        eventMappingEntity.setEventType("troubleTicketIssued");
        eventMappingEntity.setDefinitionId("/sales/troubleTicket.bpmn");
        // eventMappingRepository.save(eventMappingEntity);

        // null.save(eventMappingEntity);

        // Kafka Message
        // {
        //     eventType: troubleTicketIssued,
        //     attributes: [
        //         {
        //             name: id,
        //             className: java.lang.Integer,
        //             isKey: true
        //         },
        //         {
        //             name: status,
        //             className: java.lang.String,
        //             isKey: false
        //         }
        //     ],
        //     mappingContext: {}
        // }
        String startEventString = "{\"eventType\":\"troubleTicketIssued\",\"attributes\":[{\"name\":\"id\",\"className\":\"java.lang.Integer\",\"isKey\":true},{\"name\":\"status\",\"className\":\"java.lang.String\",\"isKey\":false}],\"mappingContext\":{}}";
        asyncEventListener.wheneverEvent(startEventString);

        // {
        //     eventType: troubleTicketCompleted,
        //     attributes: [
        //         {
        //             name: id,
        //             className: java.lang.Integer,
        //             isKey: true
        //         },
        //         {
        //             name: status,
        //             className: java.lang.String,
        //             isKey: false
        //         },
        //     ],
        //     mappingContext: {}
        // }

        String nextEventString = "{\"eventType\":\"troubleTicketIssued\",\"attributes\":[{\"name\":\"id\",\"className\":\"java.lang.Integer\",\"isKey\":true},{\"name\":\"status\",\"className\":\"java.lang.String\",\"isKey\":false}],\"mappingContext\":{}}";
        asyncEventListener.wheneverEvent(nextEventString);

        // when(eventMappingRepository.findDefinitionIdByEventType("NEW")).thenReturn("process1");

        // verify(eventMappingRepository).findDefinitionIdByEventType("NEW");


    }

    
}
