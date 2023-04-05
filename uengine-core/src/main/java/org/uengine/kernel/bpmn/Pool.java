package org.uengine.kernel.bpmn;

import org.uengine.contexts.TextContext;
import org.uengine.kernel.GlobalContext;
import org.uengine.modeling.ElementView;
import org.uengine.modeling.IElement;
import org.uengine.util.UEngineUtil;

public class Pool{

	String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
		
	TextContext description = org.uengine.contexts.TextContext.createInstance();
		public String getDescription() {
			return description.getText();
		}
		public void setDescription(TextContext string) {
			description = string;
		}
		public void setDescription(String string) {
			description.setText(string);
		}
		


	transient String currentEditorId;
		public String getCurrentEditorId() {
			return currentEditorId;
		}
		public void setCurrentEditorId(String currentEditorId) {
			this.currentEditorId = currentEditorId;
		}	
	public Pool(){
		this.setDescription("");
	}

	String viewId;
		public String getViewId() {
			return viewId;
		}
		public void setViewId(String viewId) {
			this.viewId = viewId;
		}


	public void createDocument() {
		// TODO Auto-generated method stub
	}
}
