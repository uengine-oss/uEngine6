package org.uengine.five.overriding;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.uengine.five.DefinitionServiceApplication;
import org.uengine.five.Streams;
import org.uengine.five.events.DefinitionDeployed;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.bpmn.CatchingRestMessageEvent;
import org.uengine.processmanager.ProcessTransactionContext;

import java.util.List;

/**
 * Created by uengine on 2018. 1. 5..
 */
public class EventSendingDeployFilter implements DeployFilter {



    @Override
    public void beforeDeploy(ProcessDefinition definition, ProcessTransactionContext tc, String path, boolean isNew) throws Exception {

        DefinitionDeployed definitionDeployed = new DefinitionDeployed();
        definitionDeployed.setDefintionId(path);

        Streams streams = DefinitionServiceApplication.getApplicationContext().getBean(Streams.class);

        MessageChannel messageChannel = streams.outboundChannel();
        messageChannel.send(MessageBuilder
                .withPayload(definitionDeployed)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());


        List<Activity> startActivities = definition.getStartActivities();

        if(startActivities != null){
            for(Activity activity : startActivities){

                if(activity instanceof CatchingRestMessageEvent){
    
                    CatchingRestMessageEvent catchingMessageEvent = (CatchingRestMessageEvent) activity;
    
    //                ServiceEndpointRepository serviceEndpointRepository = MetaworksRemoteService.getComponent(ServiceEndpointRepository.class);
    //
    //                ServiceEndpointEntity serviceEndpointEntity = new ServiceEndpointEntity();
    //
    //                serviceEndpointEntity.setPath(catchingMessageEvent.getServicePath());
    //                serviceEndpointEntity.setCorrelationKey(catchingMessageEvent.getCorrelationKey());
    //                serviceEndpointEntity.setDefId(folder);
    //
    //                serviceEndpointRepository.save(serviceEndpointEntity);
    
                }
    
            }
        }
    

    }
}
