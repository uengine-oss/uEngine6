package org.uengine.five.overriding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.FieldDescriptor;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.processmanager.ProcessTransactionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by uengine on 2018. 1. 5..
 */
public class EventMappingDeployFilter implements DeployFilter {

    private static final Logger log = LoggerFactory.getLogger(EventMappingDeployFilter.class);

    @Autowired
    EventMappingRepository eventMappingRepository;

    @Override
    public void beforeDeploy(ProcessDefinition definition, ProcessTransactionContext tc, String path, boolean isNew)
            throws Exception {

        /*
         * Condition (Find Start)
         * 1. EventSynchronization 존재 하는 첫번째 StartEvent
         * 2. ReceiveActivity 상속 Activity
         */

        Set<Activity> startActivitiesWithEventSync = new HashSet<>();
        List<Activity> startActivities = definition.getStartActivities();
        if (startActivities != null) {
            for (Activity activity : startActivities) {
                Activity startActivity = findStartActivityWithEventSynchronization(activity, definition, new HashSet<>());
                if (startActivity == null) {
                    // Not Found Start Event
                } else {
                    startActivitiesWithEventSync.add(startActivity);
                    saveEventMappingEntity(startActivity, definition, true);
                }
            }
        }

        List<Activity> startEvents = definition.getEvents();
        if (startEvents != null) {
            for (Activity startEvent : startEvents) {
                saveEventMappingEntity(true, startEvent, definition, true);
            }
        }

        // ReceiveActivity && Except Start Activity
        List<Activity> activities = definition.getChildActivities();
        if (activities != null) {
            for (Activity activity : activities) {
                if (activity instanceof ReceiveActivity && !startActivitiesWithEventSync.contains(activity)
                        && activity.getEventSynchronization() != null) {
                    saveEventMappingEntity(activity, definition, false);
                }
            }
        }

    }

    private Activity findStartActivityWithEventSynchronization(Activity activity, ProcessDefinition definition, Set<Activity> visited)
            throws Exception {
        try {
            if (activity == null) return null;
            if (visited == null) visited = new HashSet<>();
            if (visited.contains(activity)) return null;
            visited.add(activity);

            if ((activity instanceof StartEvent || activity instanceof ReceiveActivity)
                    && activity.getEventSynchronization() != null) {
                return activity;
            }

            List<SequenceFlow> outgoing = activity.getOutgoingSequenceFlows();
            if (outgoing == null) outgoing = Collections.emptyList();

            for (SequenceFlow sequenceFlow : outgoing) {
                if (sequenceFlow.getTargetActivity() != null) {
                    Activity result = findStartActivityWithEventSynchronization(sequenceFlow.getTargetActivity(),
                            definition, visited);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            String nameSafe = "(unknown)";
            try {
                if (activity != null && activity.getName() != null) nameSafe = activity.getName();
            } catch (Exception ignore) {}
            throw new UEngineException(
                    "Error when to find StartActivityWith EventSynchronization: " + nameSafe, e);
        }

    }

    private void saveEventMappingEntity(boolean isEvent, Activity activity, ProcessDefinition definition,
            boolean isStartEvent)
            throws Exception {
        if (isEvent) {
            if (activity instanceof Event) {
                Event event = (Event) activity;
                String eventKey = event.getEventKey();
                if (Event.THROW_EVENT.equals(event.getEventType()))
                    return;

                if (event.getEventType() == null)
                    return;

                // EventMappingEntity의 @Id(eventType)는 반드시 수동으로 채워야 함
                if (isNullOrBlank(eventKey)) {
                    log.warn("Skip EventMappingEntity save: eventKey is null/blank. defId={}, tracingTag={}",
                            safe(definition != null ? definition.getId() : null),
                            safe(activity.getTracingTag()));
                    return;
                }
                eventKey = eventKey.trim();

                EventMappingEntity eventMappingEntity = new EventMappingEntity();
                eventMappingEntity.setEventType(eventKey);
                eventMappingEntity.setCorrelationKey(event.getEventType());
                eventMappingEntity.setDefinitionId(
                        definition.getId());
                eventMappingEntity.setTracingTag(activity.getTracingTag());

                if (Event.START_EVENT.equals(event.getEventType())) {
                    eventMappingEntity.setIsStartEvent(true);
                } else {
                    eventMappingEntity.setIsStartEvent(false);
                }

                eventMappingRepository.save(eventMappingEntity);
            }
        } else {
            saveEventMappingEntity(activity, definition, isStartEvent);
        }

    }

    private void saveEventMappingEntity(Activity activity, ProcessDefinition definition, boolean isStartEvent)
            throws Exception {
        try {
            if (activity == null) return;
            if (activity.getEventSynchronization() == null) return;
            if (eventMappingRepository == null) {
                throw new IllegalStateException("eventMappingRepository is null. EventMappingDeployFilter might not be Spring-managed.");
            }

            String corrKey = null;

            // eventType / attributes가 null인 정의가 존재할 수 있으므로 방어적으로 처리한다.
            if (activity.getEventSynchronization() == null)
                return;

            FieldDescriptor[] attributes = activity.getEventSynchronization().getAttributes();
            if (attributes == null) attributes = new FieldDescriptor[0];
            FieldDescriptor[] corrKeyFields = Arrays.stream(attributes).filter(FieldDescriptor::getIsCorrKey)
                    .toArray(FieldDescriptor[]::new);
            if (corrKeyFields.length > 0) {
                corrKey = corrKeyFields[0].getName();
            }

            String eventType = activity.getEventSynchronization().getEventType();
            if (isNullOrBlank(eventType)) {
                return;
            }
            eventType = eventType.trim();
            
            EventMappingEntity eventMappingEntity = new EventMappingEntity();
            eventMappingEntity.setEventType(eventType);
            eventMappingEntity.setDefinitionId(definition.getId());
            eventMappingEntity.setCorrelationKey(corrKey);
            eventMappingEntity.setTracingTag(activity.getTracingTag());
            eventMappingEntity.setIsStartEvent(isStartEvent);

            eventMappingRepository.save(eventMappingEntity);
        } catch (Exception e) {
            throw new UEngineException("Error when to save EventMappingEntity: " + activity.getName(), e);
        }
    }

    private static boolean isNullOrBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private static String safe(String v) {
        return v == null ? "(null)" : v;
    }
}
