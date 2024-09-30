/*
 * Created on 2004. 12. 19.
 */
package org.uengine.five.overriding;

import org.uengine.kernel.*;

import java.io.Serializable;


/**
 * @author Jinyoung Jang
 */
public class InstanceDataAppendingActivityFilter implements ActivityFilter, Serializable{

	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public void afterExecute(Activity activity, final ProcessInstance instance)
		throws Exception {
		

		if(activity instanceof HumanActivity){
			try{
				RoleMapping rm = ((HumanActivity)activity).getRole().getMapping(instance);
				rm.fill(instance);
				if(rm == null) return;


				JPAProcessInstance jpaProcessInstance = (JPAProcessInstance) instance.getLocalInstance();

				if(
						instance.isNew()
						&& instance.getProcessDefinition().getInitiatorHumanActivityReference(instance.getProcessTransactionContext()).getActivity().equals(activity)
				){
					jpaProcessInstance.getProcessInstanceEntity().setInitEp(rm.getEndpoint());
					jpaProcessInstance.getProcessInstanceEntity().setInitRsNm(rm.getResourceName());
					jpaProcessInstance.getProcessInstanceEntity().setInitComCd(rm.getCompanyId());

					jpaProcessInstance.getProcessInstanceEntity().setPrevCurrEp(rm.getEndpoint());
					jpaProcessInstance.getProcessInstanceEntity().setPrevCurrRsNm(rm.getResourceName());

					jpaProcessInstance.getProcessInstanceEntity().setCurrEp("");
					jpaProcessInstance.getProcessInstanceEntity().setCurrRsNm("");


				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

	public void afterComplete(Activity activity, ProcessInstance instance) throws Exception {

	}


	public void beforeExecute(Activity activity, ProcessInstance instance)
		throws Exception {
	}

	public void onDeploy(ProcessDefinition definition) throws Exception {
	}

	public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue) throws Exception {

		if(activity instanceof HumanActivity && "saveEndpoint".equals(propertyName)){
			JPAProcessInstance processInstance = (JPAProcessInstance) instance.getLocalInstance();
			try{
				RoleMapping rm = ((HumanActivity)activity).getRole().getMapping(instance);
				rm.fill(instance);
				if(rm == null) return;
				if(
						instance.isNew()
						&& instance.getProcessDefinition().getInitiatorHumanActivityReference(instance.getProcessTransactionContext()).getActivity().equals(activity)
				){
					processInstance.getProcessInstanceEntity().setInitEp(rm.getEndpoint());
					processInstance.getProcessInstanceEntity().setInitRsNm(rm.getResourceName());
					processInstance.getProcessInstanceEntity().setInitComCd(rm.getCompanyId());

					processInstance.getProcessInstanceEntity().setCurrEp(rm.getEndpoint());
					processInstance.getProcessInstanceEntity().setCurrRsNm(rm.getResourceName());
				} else {
					StringBuffer endpoint = new StringBuffer();
					StringBuffer resourceName = new StringBuffer();
					do {
						if (endpoint.length() > 0) endpoint.append(";");
						endpoint.append(rm.getEndpoint());

						if (resourceName.length() > 0) resourceName.append(";");
						resourceName.append(rm.getResourceName());
					} while (rm.next());

					processInstance.getProcessInstanceEntity().setCurrEp(endpoint.toString());
					processInstance.getProcessInstanceEntity().setCurrRsNm(resourceName.toString());
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			if ( instance.isNew() && instance.isSubProcess() && !instance.getInstanceId().equals(instance.getRootProcessInstanceId())) {
				JPAProcessInstance rootProcessInstance = (JPAProcessInstance) instance.getRootProcessInstance().getLocalInstance();
				String initEp = (String) rootProcessInstance.getProcessInstanceEntity().getInitEp();
				String initRSNM = (String) rootProcessInstance.getProcessInstanceEntity().getInitRsNm();
				String initComcode = (String) rootProcessInstance.getProcessInstanceEntity().getInitComCd();
				processInstance.getProcessInstanceEntity().setInitEp(initEp);
				processInstance.getProcessInstanceEntity().setInitRsNm(initRSNM);
				processInstance.getProcessInstanceEntity().setInitComCd(initComcode);

				processInstance.getProcessInstanceEntity().setCurrEp(initEp);
				processInstance.getProcessInstanceEntity().setCurrRsNm(initRSNM);
			}
		}
	}
}
