package org.uengine.kernel.bpmn;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessInstance;
import org.uengine.processmanager.InstanceServiceLocal;

/**
 * Created by jinyoung jang on 2015. 8. 6..
 */
@Component
public class TimerEventJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        TimerEventJob timerEventJob = GlobalContext.getComponent(TimerEventJob.class);

        try {
            timerEventJob.fireEvent(jobExecutionContext.getJobDetail().getJobDataMap());
        } catch (Exception e) {
            e.printStackTrace();

            throw new JobExecutionException(e);
        }

    }

    @Autowired
    InstanceServiceLocal instanceService;

    @Transactional
    public void fireEvent(JobDataMap jobDataMap) throws Exception {
        String instanceId = jobDataMap.getString("instanceId");
        String tracingTag = jobDataMap.getString("tracingTag");
        String executionScope = jobDataMap.getString("executionScope");

        System.out.println("triggered for [instId: " + instanceId + "] and [execScope: " + executionScope
                + "] and [trcTag: " + tracingTag + "]");

        ProcessInstance instance = instanceService.getInstance(instanceId);
        TimerEvent timerEvent = (TimerEvent) instance.getProcessDefinition().getActivity(tracingTag);

        String originalExecutionScope = null;

        if (instance.getExecutionScopeContext() != null) {
            originalExecutionScope = instance.getExecutionScopeContext().getExecutionScope();
        }

        instance.setExecutionScope(executionScope);

        if (timerEvent != null) {
            timerEvent.onMessage(instance, null);
        }

        instance.setExecutionScope(originalExecutionScope);
    }
}
