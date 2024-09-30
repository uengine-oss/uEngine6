package org.uengine.five.overriding;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.entity.EventMappingEntity;
import org.uengine.five.repository.EventMappingRepository;
import org.uengine.five.service.InstanceService;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.FieldDescriptor;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ReceiveActivity;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.kernel.bpmn.StartEvent;
import org.uengine.processmanager.ProcessTransactionContext;

import java.util.Arrays;
import java.util.List;

/**
 * Created by uengine on 2018. 1. 5..
 */
public class EventMappingDeployFilter implements DeployFilter {

    @Autowired
    EventMappingRepository eventMappingRepository;

    @Override
    public void beforeDeploy(ProcessDefinition definition, ProcessTransactionContext tc, String path, boolean isNew) throws Exception {
       
        /* Condition (Find Start)
         * 1. EventSynchronization 존재 하는 첫번째 StartEvent
         * 2. ReceiveActivity 상속 Activity 
        */

        Activity startActivity = null;
        List<Activity> startActivities = definition.getStartActivities();
        if(startActivities != null){
            for(Activity activity : startActivities){
                startActivity = findStartActivityWithEventSynchronization(activity, definition);
                if(startActivity == null){
                    // Not Found Start Event
                } else {
                    saveEventMappingEntity(startActivity, definition, true);
                }
            }

        }
            
        // ReceiveActivity && Except Start Activity
        List<Activity> activities = definition.getChildActivities();
        if(activities != null){
            for(Activity activity : activities){
                if(activity instanceof ReceiveActivity && activity != startActivity &&activity.getEventSynchronization() != null ) {
                    saveEventMappingEntity(activity, definition, false);
                }   
            }
        }

    }

    private Activity findStartActivityWithEventSynchronization(Activity activity, ProcessDefinition definition) throws Exception {
        try {
            if ((activity instanceof StartEvent || activity instanceof ReceiveActivity) && activity.getEventSynchronization() != null) {
                return activity;
            }
        
            for (SequenceFlow sequenceFlow : activity.getOutgoingSequenceFlows()) {
                if (sequenceFlow.getTargetActivity() != null) {
                    Activity result = findStartActivityWithEventSynchronization(sequenceFlow.getTargetActivity(), definition);
                    if (result != null) {
                        return result;
                    }
                }
            }
            return null;
        } catch (Exception e){
            throw new UEngineException("Error when to find StartActivityWith EventSynchronization: " + activity.getName(), e);
        }
       
    }

    private void saveEventMappingEntity(Activity activity, ProcessDefinition definition, boolean isStartEvent) throws Exception {
        try {
            String corrKey = null;
            FieldDescriptor[] attributes = activity.getEventSynchronization().getAttributes();
            FieldDescriptor[] corrKeyFields = Arrays.stream(attributes).filter(FieldDescriptor::getIsCorrKey).toArray(FieldDescriptor[]::new);
            if (corrKeyFields.length > 0) {
                corrKey = corrKeyFields[0].getName();
            }
            
            EventMappingEntity eventMappingEntity = new EventMappingEntity();
            eventMappingEntity.setEventType(activity.getEventSynchronization().getEventType());
            eventMappingEntity.setDefinitionId(definition.getId());
            eventMappingEntity.setCorrelationKey(corrKey);
            eventMappingEntity.setTracingTag(activity.getTracingTag());
            eventMappingEntity.setIsStartEvent(isStartEvent);
           
            eventMappingRepository.save(eventMappingEntity);
        } catch (Exception e){
            throw new UEngineException("Error when to save EventMappingEntity: " + activity.getName(), e);
        }
    }
}
