package org.uengine.kernel;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JLabel;

import org.uengine.contexts.TextContext;

public class EventHandler implements Serializable {

	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	public final static int TRIGGERING_BY_EVENTBUTTON = 1;
	public final static int TRIGGERING_BY_API = 2;
	public final static int TRIGGERING_BY_WEBSERVICE = 3;
	public final static int TRIGGERING_BY_DELEGATION = 10;
	public final static int TRIGGERING_BY_COMPENSATION = 11;
	public final static int TRIGGERING_BY_FAULT = 12;
	public final static int TRIGGERING_BY_CONDITIONAL = 13;
	public final static int TRIGGERING_BY_ESCALATION = 14;
	// activity event
	public final static int TRIGGERING_BY_AFTER_CHILD_COMPLETED = 21;
	public final static int TRIGGERING_BY_AFTER_CHILD_SAVED = 22;
	public final static int TRIGGERING_BY_AFTER_CHILD_SAVED_OR_COMPLETED = 23;

	// approval
	public final static int TRIGGERING_BY_DRAFTED = 101;
	public final static int TRIGGERING_BY_APPROVED = 102;
	public final static int TRIGGERING_BY_REJECTED = 103;
	public final static int TRIGGERING_BY_ARBITRARYFINISHED = 104;

	String name;
	TextContext displayName = org.uengine.contexts.TextContext.createInstance();
	Activity handlerActivity;
	Role openRoles;
	int triggeringMethod = TRIGGERING_BY_EVENTBUTTON;

	public Activity getHandlerActivity() {
		return handlerActivity;
	}

	public void setHandlerActivity(Activity handlerActivity) {
		this.handlerActivity = handlerActivity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TextContext getDisplayName() {
		if (displayName.getText() == null) {
			TextContext result = TextContext.createInstance();
			result.setText(getName());
			return result;
		}

		return displayName;
	}

	public void setDisplayName(TextContext displayName) {
		this.displayName = displayName;
	}

	public Role getOpenRoles() {
		return openRoles;
	}

	public void setOpenRoles(Role openRoles) {
		this.openRoles = openRoles;
	}

	public int getTriggeringMethod() {
		return triggeringMethod;
	}

	public void setTriggeringMethod(int triggeringMethod) {
		this.triggeringMethod = triggeringMethod;
	}

}
