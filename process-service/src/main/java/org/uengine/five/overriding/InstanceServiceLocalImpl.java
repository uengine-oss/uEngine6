package org.uengine.five.overriding;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.service.InstanceServiceImpl;
import org.uengine.kernel.ProcessInstance;
import org.uengine.processmanager.InstanceServiceLocal;

public class InstanceServiceLocalImpl implements InstanceServiceLocal {

    @Autowired
    InstanceServiceImpl instanceService;

    @Override
    public ProcessInstance getInstance(String instanceId) throws Exception {
        return instanceService.getProcessInstanceLocal(instanceId);
    }
}
