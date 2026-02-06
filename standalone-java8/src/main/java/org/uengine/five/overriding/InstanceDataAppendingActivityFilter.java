/*
 * Created on 2004. 12. 19.
 */
package org.uengine.five.overriding;

import org.uengine.kernel.*;
import org.uengine.kernel.bpmn.ServiceTask;
import org.uengine.webservices.worklist.WorkList;
import org.uengine.five.framework.ProcessTransactionContext;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * @author Jinyoung Jang
 */
public class InstanceDataAppendingActivityFilter implements ActivityFilter, Serializable{

	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public void afterExecute(Activity activity, final ProcessInstance instance)
			throws Exception {

		if (activity instanceof HumanActivity || activity instanceof ServiceTask || activity instanceof ScriptActivity) {
			try {
				RoleMapping rm = null;
				if (activity instanceof HumanActivity) {
					rm = ((HumanActivity) activity).getRole().getMapping(instance);
				} else if (activity instanceof ServiceTask) {
					Role role = ((ServiceTask) activity).getRole();
					if (role != null)
						rm = role.getMapping(instance);
				} else if (activity instanceof ScriptActivity) {
					String roleName = null;
					if (activity.getExtendedAttributes() != null) {
						Object rn = activity.getExtendedAttributes().get("role");
						if (rn != null) roleName = String.valueOf(rn);
					}
					if (roleName != null) {
						Role role = instance.getProcessDefinition().getRole(roleName);
						if (role != null) rm = role.getMapping(instance);
					}
				}

				// rm.fill(instance);
				if (rm == null && (activity instanceof ServiceTask || activity instanceof ScriptActivity)) {
					try {
						rm = RoleMapping.create();
						rm.setEndpoint("system");
						rm.setResourceName("system");
						rm.setName("System");
					} catch (Exception e) {
						// ignore
					}
				}

				if (rm == null)
					return;

				// ScriptActivity/ServiceTask는 executeActivity 내부에서 fireComplete 전에 afterExecute를 호출하므로
				// 여기서 한 번 worklist에 추가된다. 엔진이 executeActivity 반환 후 다시 afterExecute를 호출하면
				// 중복 추가를 막기 위해 이미 추가된 (rootInstId, trcTag)는 건너뛴다.
				if (activity instanceof ServiceTask || activity instanceof ScriptActivity) {
					String rootInstId = instance.getRootProcessInstanceId();
					String trcTag = activity.getTracingTag();
					String dedupeKey = "worklistAppended:" + rootInstId + ":" + trcTag;
					ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();
					if (tc != null && Boolean.TRUE.equals(tc.getSharedContext(dedupeKey)))
						return;
					if (tc != null)
						tc.setSharedContext(dedupeKey, true);
				}

				JPAProcessInstance jpaProcessInstance = (JPAProcessInstance) instance.getLocalInstance();

				if (jpaProcessInstance.isNewInstance()
						&& instance.getProcessDefinition()
								.getInitiatorHumanActivityReference(instance.getProcessTransactionContext())
								.getActivity().equals(activity)) {
					jpaProcessInstance.getProcessInstanceEntity().setInitEp(rm.getEndpoint());
					jpaProcessInstance.getProcessInstanceEntity().setInitRsNm(rm.getResourceName());
					jpaProcessInstance.getProcessInstanceEntity().setInitComCd(rm.getCompanyId());

					jpaProcessInstance.getProcessInstanceEntity()
							.setPrevCurrEp("");
					jpaProcessInstance.getProcessInstanceEntity()
							.setPrevCurrRsNm("");

					jpaProcessInstance.getProcessInstanceEntity().setCurrEp(rm.getEndpoint());
					jpaProcessInstance.getProcessInstanceEntity().setCurrRsNm(rm.getResourceName());

					jpaProcessInstance.setNewInstance(false);
				} else {
					jpaProcessInstance.getProcessInstanceEntity()
							.setPrevCurrEp(jpaProcessInstance.getProcessInstanceEntity().getCurrEp());
					jpaProcessInstance.getProcessInstanceEntity()
							.setPrevCurrRsNm(jpaProcessInstance.getProcessInstanceEntity().getCurrRsNm());

					jpaProcessInstance.getProcessInstanceEntity().setCurrEp(rm.getEndpoint());
					jpaProcessInstance.getProcessInstanceEntity().setCurrRsNm(rm.getResourceName());

				}

				if (activity instanceof ServiceTask || activity instanceof ScriptActivity) {
					WorkList worklist = instance.getWorkList();
					String title = activity.getName(GlobalContext.DEFAULT_LOCALE);
					ArrayList<KeyedParameter> params = new ArrayList<KeyedParameter>();
					params.add(new KeyedParameter(KeyedParameter.TITLE, title));
					params.add(new KeyedParameter("actType", activity.getClass().getSimpleName()));
					params.add(new KeyedParameter(KeyedParameter.TRACINGTAG, activity.getTracingTag()));
					params.add(new KeyedParameter(KeyedParameter.INSTANCEID, instance.getInstanceId()));
					params.add(new KeyedParameter(KeyedParameter.ROOTINSTANCEID, instance.getRootProcessInstanceId()));
					params.add(new KeyedParameter(KeyedParameter.PROCESSDEFINITION, instance.getProcessDefinition().getId()));
					params.add(new KeyedParameter(KeyedParameter.DEFAULT_STATUS, Activity.STATUS_COMPLETED));
					params.add(new KeyedParameter("status", Activity.STATUS_COMPLETED));
					params.add(new KeyedParameter("endpoint", rm.getEndpoint()));
					params.add(new KeyedParameter("endDate", new java.util.Date()));
					params.add(new KeyedParameter("assignType", 0));
					params.add(new KeyedParameter(KeyedParameter.TOOL, "defaultHandler"));

					KeyedParameter[] paramArray = params.toArray(new KeyedParameter[0]);

					String taskId = worklist.addWorkItem(rm, paramArray,
							instance.getProcessTransactionContext());
					worklist.completeWorkItem(taskId, paramArray, instance.getProcessTransactionContext());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 액티비티 실행 중 예외가 나서 에러 Boundary 등으로 처리된 경우 호출됨.
	 * ServiceTask는 afterExecute가 호출되지 않으므로, 여기서 실패 건을 worklist에 추가한다.
	 */
	@Override
	public void afterFault(Activity activity, ProcessInstance instance, FaultContext faultContext) throws Exception {
		if (activity instanceof ServiceTask || activity instanceof ScriptActivity) {
			try {
				RoleMapping rm = null;
				if (activity instanceof ServiceTask) {
					Role role = ((ServiceTask) activity).getRole();
					if (role != null)
						rm = role.getMapping(instance);
				} else if (activity instanceof ScriptActivity) {
					String roleName = null;
					if (activity.getExtendedAttributes() != null) {
						Object rn = activity.getExtendedAttributes().get("role");
						if (rn != null) roleName = String.valueOf(rn);
					}
					if (roleName != null) {
						Role role = instance.getProcessDefinition().getRole(roleName);
						if (role != null) rm = role.getMapping(instance);
					}
				}
				if (rm == null) {
					rm = RoleMapping.create();
					rm.setEndpoint("system");
					rm.setResourceName("system");
					rm.setName("System");
				}
				WorkList worklist = instance.getWorkList();
				String title = activity.getName(GlobalContext.DEFAULT_LOCALE);
				ArrayList<KeyedParameter> params = new ArrayList<KeyedParameter>();
				params.add(new KeyedParameter(KeyedParameter.TITLE, title));
				params.add(new KeyedParameter("actType", activity.getClass().getSimpleName()));
				params.add(new KeyedParameter(KeyedParameter.TRACINGTAG, activity.getTracingTag()));
				params.add(new KeyedParameter(KeyedParameter.INSTANCEID, instance.getInstanceId()));
				params.add(new KeyedParameter(KeyedParameter.ROOTINSTANCEID, instance.getRootProcessInstanceId()));
				params.add(new KeyedParameter(KeyedParameter.PROCESSDEFINITION, instance.getProcessDefinition().getId()));
				params.add(new KeyedParameter(KeyedParameter.DEFAULT_STATUS, Activity.STATUS_FAULT));
				params.add(new KeyedParameter("status", Activity.STATUS_FAULT));
				params.add(new KeyedParameter("endpoint", rm.getEndpoint()));
				params.add(new KeyedParameter("endDate", new java.util.Date()));
				params.add(new KeyedParameter("assignType", 0));
				params.add(new KeyedParameter(KeyedParameter.TOOL, "defaultHandler"));

				KeyedParameter[] paramArray = params.toArray(new KeyedParameter[0]);
				worklist.addWorkItem(rm, paramArray, instance.getProcessTransactionContext());
			} catch (Exception e) {
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
				rm.fill();
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
