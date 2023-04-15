package org.uengine.processmanager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessDefinitionFactory;

/**
 * @author  <a href="mailto:ghbpark@hanwha.co.kr">Sungsoo Park</a>
 * @version    $Id: ProcessTransactionContext.java,v 1.1 2012/02/13 05:29:33 sleepphoenix4 Exp $
 */
public class DefaultProcessTransactionContext extends DefaultTransactionContext implements ProcessTransactionContext {
		
	static PrintStream originalSystemOut = System.out;
	static PrintStream originalSystemErr = System.err;

		
	@Override
	public void addDebugInfo(Object message){
		if (GlobalContext.logLevelIsDebug) {
			StringBuilder richDebugInfo = (StringBuilder) getSharedContext("_richDebugInfo");
			
			if(richDebugInfo==null){
				richDebugInfo = new StringBuilder();
				richDebugInfo.append("\n=================================================================================");
				richDebugInfo.append("\n==[ uEngine Application Execution Log ]==========================================");
				richDebugInfo.append("\n=================================================================================\n");
			}
			
			richDebugInfo.append(message);
	
			setSharedContext("_richDebugInfo", richDebugInfo);
		}
	}
	
	@Override
	public StringBuilder getDebugInfo(){
		StringBuilder richDebugInfo = (StringBuilder) getSharedContext("_richDebugInfo");
		
		return (richDebugInfo);
	}


	
	@Override
	public ProcessDefinition getProcessDefinition(String pdvid) throws Exception{
		return (ProcessDefinition) ProcessDefinitionFactory.getInstance(this).getActivity(pdvid, true);
	}

	@Override
	public void registerProcessInstance(DefaultProcessInstance defaultProcessInstance) {

	}

	// @Override
	// public ServletRequest getServletRequest() {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'getServletRequest'");
	// }

	// @Override
	// public ServletResponse getServletResponse() {
	// 	// TODO Auto-generated method stub
	// 	throw new UnsupportedOperationException("Unimplemented method 'getServletResponse'");
	// }

}
