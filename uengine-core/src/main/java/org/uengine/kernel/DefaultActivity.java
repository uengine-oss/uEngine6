package org.uengine.kernel;

import javax.naming.InitialContext;

import org.uengine.util.UEngineUtil;

/**
 * @author Jinyoung Jang
 */
//@Face(displayName="Unit 프로세스")
public class DefaultActivity extends Activity{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

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



}

