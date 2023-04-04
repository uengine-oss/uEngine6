package org.uengine.kernel;

import java.util.ArrayList;


public class ParameterContextPanel  implements ContextAware{
	
	
	String editorId;
		public String getEditorId() {
			return editorId;
		}
		public void setEditorId(String editorId) {
			this.editorId = editorId;
		}
	String parentEditorId;
		public String getParentEditorId() {
			return parentEditorId;
		}
		public void setParentEditorId(String parentEditorId) {
			this.parentEditorId = parentEditorId;
		}	
	ParameterContext[] parameterContext;
		public ParameterContext[] getParameterContext() {
			return parameterContext;
		}
		public void setParameterContext(ParameterContext[] parameterContext) {
			this.parameterContext = parameterContext;
		}
	ParameterContext selectedContext;
		public ParameterContext getSelectedContext() {
			return selectedContext;
		}
		public void setSelectedContext(ParameterContext selectedContext) {
			this.selectedContext = selectedContext;
		}
		
	public ParameterContextPanel(){
		this.setMetaworksContext(new MetaworksContext());
		this.getMetaworksContext().setWhen("edit");
	}
	
	public void load() throws Exception{
		if( parameterContext == null ){
			parameterContext = new ParameterContext[0];
		}
	}
	
	
}
