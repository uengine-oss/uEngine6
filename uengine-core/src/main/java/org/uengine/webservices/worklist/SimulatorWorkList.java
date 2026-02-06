/*
 * Created on 2004. 12. 13.
 */
package org.uengine.webservices.worklist;

import java.rmi.RemoteException;
import java.util.*;

import org.uengine.kernel.KeyedParameter;
import org.uengine.kernel.RoleMapping;
import org.uengine.processmanager.DefaultTransactionContext;
import org.uengine.processmanager.TransactionContext;

/**
 * @author Jinyoung Jang
 */
public class SimulatorWorkList extends DefaultWorkList{
	
	ArrayList worklist = new ArrayList();
	static int currTaskId = 0;

	@Override
	public String reserveWorkItem(RoleMapping roleMapping, KeyedParameter[] parameters, TransactionContext tc) throws RemoteException {
		return addWorkItem(roleMapping, parameters, tc);
	}

	@Override
	public String addWorkItem(RoleMapping roleMapping, KeyedParameter[] parameters, TransactionContext tc) throws RemoteException {
		String userId = roleMapping != null ? roleMapping.getEndpoint() : null;
		return addWorkItem(userId, parameters);
	}

	@Override
	public String addWorkItem(String taskId, RoleMapping roleMapping, KeyedParameter[] parameters, TransactionContext tc) throws RemoteException {
		String userId = roleMapping != null ? roleMapping.getEndpoint() : null;
		return addWorkItem(taskId, userId, parameters);
	}

	@Override
	public void updateWorkItem(String taskId, RoleMapping roleMapping, KeyedParameter[] parameters, TransactionContext tc) throws RemoteException {
		// 시뮬레이터는 기본적으로 update를 별도 처리하지 않음 (DefaultWorkList도 no-op)
		super.updateWorkItem(taskId, roleMapping, parameters, tc);
	}
	
	public String addWorkItem(String userId, KeyedParameter[] parameters)
		throws RemoteException {

		return addWorkItem(null, userId, parameters);
	}

	public String addWorkItem(
		String taskId,
		String userId,
		KeyedParameter[] parameters)
		throws RemoteException {
			
		taskId = new String(""+(currTaskId++));	
		
		Hashtable workitem = new Hashtable();
		workitem.put("TASKID", taskId);	
		workitem.put("PARAMETERS", parameters);
		workitem.put("USERID", userId);
		
		worklist.add(workitem);

		return taskId;
	}

	public void cancelWorkItem(String taskID, KeyedParameter[] options, DefaultTransactionContext tc)
		throws RemoteException {
		// TODO Auto-generated method stub
		super.cancelWorkItem(taskID, options, tc);
	}

	/* (non-Javadoc)
	 * @see org.uengine.webservices.worklist.WorkList#completeWorkItem(java.lang.String, org.uengine.webservices.worklist.KeyedParameter[])
	 */
	public void completeWorkItem(String taskID, KeyedParameter[] options, DefaultTransactionContext tc)
		throws RemoteException {
		// TODO Auto-generated method stub
		super.completeWorkItem(taskID, options, tc);
	}

	public String reserveWorkItem(String userId, KeyedParameter[] parameters)
		throws RemoteException {
		return addWorkItem(userId, parameters);
	}

	public void updateWorkItem(
		String taskId,
		String userId,
		KeyedParameter[] parameters, DefaultTransactionContext tc)
		throws RemoteException {
		RoleMapping rm = RoleMapping.create();
		if (rm != null) rm.setEndpoint(userId);
		updateWorkItem(taskId, rm, parameters, tc);
	}

	public ArrayList getWorklist() {
		return worklist;
	}

	public void setWorklist(ArrayList list) {
		worklist = list;
	}
	
	public Collection findWorkitem(Hashtable filter, boolean isAnd){
		Collection result = new ArrayList();
		String userId = (String)filter.get("USERID");
		String status = (String)filter.get("STATUS");
		
		for(Iterator iter = getWorklist().iterator(); iter.hasNext(); ){
			Map workitem = (Map)iter.next();
			
			String compareUserId = (workitem.containsKey("USERID") ? (String)workitem.get("USERID"): "[NOVALUE]");
			String compareStatus = (workitem.containsKey("STATUS") ? (String)workitem.get("STATUS"): "[NOVALUE]");
			
			boolean isResult = false;
			
			if(compareUserId.equals(userId)){
				isResult = true;
			}
			
			if(compareStatus.equals(status)){
				if(!isAnd)
					isResult = true;
			}else{
				if(isAnd)
					isResult = false;
			}

			if(isResult)
				result.add(workitem);
		}
		
		return result;
	}

}
