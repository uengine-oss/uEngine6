/*
 * Created on 2004. 10. 9.
 */
package org.uengine.five.overriding;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.entity.WorklistEntity;
import org.uengine.five.repository.WorklistRepository;
import org.uengine.kernel.Activity;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.SensitiveActivityFilter;

/**
 * @author Jinyoung Jang
 */
public class DueDateSettingWhenCompensationFilter implements SensitiveActivityFilter{
	
	@Autowired
	WorklistRepository worklistRepository;

	public void onEvent(Activity activity, ProcessInstance instance, String eventName, Object payload) throws Exception{
		// activity compensate
		if(("activity compensate".equals(eventName) || "activity resume".equals(eventName)) && activity instanceof HumanActivity){
			HumanActivity humanActivity = (HumanActivity) activity;
			List<WorklistEntity> worklists = worklistRepository.findWorkListByInstIdAndStatus(Long.parseLong(instance.getInstanceId()), "CANCELLED");
			String[] taskIds = worklists.stream()
				.filter(w -> w.getTrcTag().equals(humanActivity.getTracingTag()))
				.map(w -> String.valueOf(w.getTaskId()))
				.toArray(String[]::new);

			if (taskIds != null) {
				for (String taskId : taskIds) {
					WorklistEntity worklist = worklistRepository.findById(Long.parseLong(taskId)).orElse(null);
					if (worklist != null) {
						worklist.setDueDate(new Timestamp(System.currentTimeMillis()));
						worklistRepository.save(worklist);
					}
				}
			}
		}
	}

	@Override
	public void beforeExecute(Activity activity, ProcessInstance instance) throws Exception {}

	@Override
	public void afterExecute(Activity activity, ProcessInstance instance) throws Exception {}

	@Override
	public void afterComplete(Activity activity, ProcessInstance instance) throws Exception {}

	@Override
	public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue) throws Exception {}

	@Override
	public void onDeploy(ProcessDefinition definition) throws Exception {}
}
