package org.uengine.kernel;

import org.uengine.contexts.MappingContext;
import org.uengine.modeling.ElementView;

public class MappingActivity extends DefaultActivity {

	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	public final static String FILE_SYSTEM_DIR = GlobalContext.getPropertyString("filesystem.path", ProcessDefinitionFactory.DEFINITION_ROOT);

	public MappingActivity(){
		setName("mapping");
	}
	
	transient String parentEditorId;
		public String getParentEditorId() {
			return parentEditorId;
		}
		public void setParentEditorId(String parentEditorId) {
			this.parentEditorId = parentEditorId;
		}
	
	MappingContext mappingContext;
		public MappingContext getMappingContext() {
			return mappingContext;
		}
		public void setMappingContext(MappingContext mappingContext) {
			this.mappingContext = mappingContext;
		}
	
	protected void executeActivity(ProcessInstance instance) throws Exception {
		
	}

}
