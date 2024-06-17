package org.uengine.five.overriding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uengine.five.entity.CatchEvent;
import org.uengine.five.entity.ServiceEndpointEntity;
import org.uengine.five.repository.ServiceEndpointRepository;
import org.uengine.kernel.Activity;
import org.uengine.kernel.CatchingMessageEvent;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.bpmn.CatchingRestMessageEvent;
import org.uengine.processmanager.ProcessTransactionContext;

/**
 * Created by uengine on 2018. 1. 8..
 */
public class ServiceRegisterDeployFilter implements DeployFilter {

    @Override
    public void beforeDeploy(ProcessDefinition definition, ProcessTransactionContext tc, String path, boolean isNew) throws Exception {

        // if(!definition.isInitiateByFirstWorkitem()) return;

        List<Activity> startActivities = definition.getStartActivities();
        if(startActivities == null) return;

        // for(Activity activity : startActivities){

        //     if(activity instanceof CatchingRestMessageEvent){

        //         CatchingRestMessageEvent catchingMessageEvent = (CatchingRestMessageEvent) activity;

        //         ServiceEndpointRepository serviceEndpointRepository = GlobalContext.getComponent(ServiceEndpointRepository.class);

        //         ServiceEndpointEntity serviceEndpointEntity = new ServiceEndpointEntity();

        //         serviceEndpointEntity.setPath(catchingMessageEvent.getServicePath());
        //         serviceEndpointEntity.setCorrelationKey(catchingMessageEvent.getCorrelationKey());
        //         serviceEndpointEntity.setDefId(path);

        //         serviceEndpointRepository.save(serviceEndpointEntity);

        //     }

        // }
        ServiceEndpointRepository serviceEndpointRepository = GlobalContext.getComponent(ServiceEndpointRepository.class);
        // 기존 defId를 가진 요소를 우선 제거
        serviceEndpointRepository.deleteByDefId(path);
        ServiceEndpointEntity serviceEndpointEntity = new ServiceEndpointEntity();

        List<Activity> catchingMessageEvents = definition.getCatchingMessageEvents();
        List<CatchEvent> catchEvents = new ArrayList<>();
        for (Activity activity : catchingMessageEvents) {
            if (activity instanceof CatchingMessageEvent) {
                
                CatchingRestMessageEvent catchingMessageEvent = (CatchingRestMessageEvent) activity;
                serviceEndpointEntity.setPath(catchingMessageEvent.getServicePath());
                CatchEvent catchEvent = new CatchEvent();
                catchEvent.setCorrelationKey(catchingMessageEvent.getCorrelationKey());
                catchEvent.setMessageClass(catchingMessageEvent.getClass().getName());
                catchEvent.setDefId(path);
                catchEvents.add(catchEvent);
            }
        }

        serviceEndpointEntity.setEvents(catchEvents);
        serviceEndpointRepository.save(serviceEndpointEntity);
    }
}
