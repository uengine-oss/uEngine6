package org.uengine.processmanager;

import org.uengine.kernel.ProcessInstance;

public interface InstanceServiceLocal {
	ProcessInstance getInstance(String instanceId) throws Exception;
}