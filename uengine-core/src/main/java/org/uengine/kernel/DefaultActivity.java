package org.uengine.kernel;

import javax.naming.InitialContext;

import org.uengine.util.UEngineUtil;

/**
 * @author Jinyoung Jang
 */
//@Face(displayName="Unit 프로세스")
public class DefaultActivity extends Activity{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	public static final String PVKEY_TASKID = "_task id";

	public DefaultActivity(String name){
		setName(name);
		
		//createDocument();
	}
	public DefaultActivity(){
		this("");
	}

	protected void executeActivity(ProcessInstance instance) throws Exception{
		System.out.println("default activity::execute: " + getName());

		fireComplete(instance);
	}


	String document;

	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}


	public void setTaskIds(ProcessInstance instance, String[] taskIds) throws Exception {
		StringBuffer taskId = new StringBuffer();
		if (taskIds != null) {
			for (int i = 0; i < taskIds.length; i++) {
				if (taskId.length() > 0)
					taskId.append(",");
				taskId.append(taskIds[i]);
			}
		}

		instance.setProperty(getTracingTag(), PVKEY_TASKID, (taskId.length() > 0 ? taskId.toString() : null));
	}

	public String[] getTaskIds(ProcessInstance instance) throws Exception {
		if (instance == null)
			return null;
		String taskId = (String) instance.getProperty(getTracingTag(), PVKEY_TASKID);
		String[] taskIds = null;
		if (taskId != null && taskId.trim().length() > 0)
			taskIds = taskId.split(",");
		else
			taskIds = null;

		return taskIds;
	}

}

