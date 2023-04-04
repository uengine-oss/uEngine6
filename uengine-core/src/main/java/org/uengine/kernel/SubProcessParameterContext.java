package org.uengine.kernel;

import java.io.Serializable;

import org.uengine.contexts.TextContext;
import org.uengine.kernel.GlobalContext;

/**
 * @author Jinyoung Jang
 */
public class SubProcessParameterContext extends ParameterContext{
	
	
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	boolean split;
		public boolean isSplit() {
			return split;
		}
		public void setSplit(boolean split) {
			this.split = split;
		}


	@Override
	public boolean isMultipleInput() {
		return super.isMultipleInput();
	}
}
