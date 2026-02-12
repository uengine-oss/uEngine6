/*
 * Created on 2004. 10. 9.
 */
package org.uengine.kernel;

/**
 * @author Jinyoung Jang
 */
public interface ActivityFilter extends java.io.Serializable{
	//run-time
	void beforeExecute(Activity activity, ProcessInstance instance) throws Exception;
	void afterExecute(Activity activity, ProcessInstance instance) throws Exception;
	void afterComplete(Activity activity, ProcessInstance instance) throws Exception;
	/**
	 * 액티비티 실행 중 예외 발생 후(에러 Boundary 등으로 처리된 경우) 호출.
	 * (예: 실패한 ServiceTask를 worklist에 남기는 처리)
	 */
	void afterFault(Activity activity, ProcessInstance instance, FaultContext faultContext) throws Exception;
	void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue) throws Exception;
	//deploy time
	void onDeploy(ProcessDefinition definition) throws Exception;
}
