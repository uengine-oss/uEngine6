package org.uengine.kernel;

import java.io.Serializable;
import java.util.Map;

import org.uengine.contexts.TextContext;

/**
 * @author Jinyoung Jang
 */

public abstract class Condition implements Validatable, Serializable {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	public abstract boolean isMet(ProcessInstance instance, String scope) throws Exception;

	TextContext description = org.uengine.contexts.TextContext.createInstance();

	public TextContext getDescription() {
		return description;
	}

	public void setDescription(TextContext string) {
		description = string;
	}

	public String toString() {
		if (getDescription().getText() != null)
			return getDescription().getText();
		else
			return super.toString();
	}

	public String getName() {

		return toString();
	}

	public void setName(String name) {
	}

	public ValidationContext validate(Map options) {
		ValidationContext vc = new ValidationContext();

		return vc;
	}
}