package org.uengine.kernel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.uengine.contexts.EventSynchronization;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.uengine.contexts.TextContext;
import org.uengine.kernel.bpmn.BlockFinder;
import org.uengine.kernel.bpmn.CompensateEvent;
import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.SequenceFlow;
import org.uengine.modeling.ElementView;
import org.uengine.modeling.IElement;
// import org.uengine.modeling.ElementView;
// import org.uengine.modeling.IElement;
//import org.uengine.modeling.IIntegrityElement;
import org.uengine.util.UEngineUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class plays the most important role in the uEngine
 * ,which is composed of key attributes and methods of general behaviors,
 * properties and callback messages of Activity Types.
 * To understand uEngine's process model, you may need to understand 'Composite
 * Pattern' (which is one of most well-known design pattern also used in
 * java.io.File)
 * first. That is, this Activity class and ComplexActivity has a self-contained
 * relationship for easy-activity-type extension and
 * easy-to-hold the block-based (structured) process model.
 * 
 * @author Jinyoung Jang
 * @see org.uengine.kernel.ComplexActivity
 */
public abstract class Activity implements IElement, Validatable, java.io.Serializable, Cloneable {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	final public static String ACTIVITY_DONE = "activity done";
	final public static String ACTIVITY_SKIPPED = "activity skip";
	final public static String ACTIVITY_COMPENSATED = "activity compensate";
	final public static String ACTIVITY_RESUMED = "activity resume";
	final public static String ACTIVITY_RESET = "activity reset";
	final public static String ACTIVITY_CHANGED = "activity changed";
	final public static String ACTIVITY_STOPPED = "activity stop";
	final public static String ACTIVITY_SUSPENDED = "activity suspended";
	final public static String ACTIVITY_RETRYING = "activity retrying";

	final public static String CHILD_DONE = "child done";
	final public static String CHILD_COMPENSATED = "child compensate";
	final public static String PREPIX_MESSAGE = "message from external";
	final public static String ACTIVITY_FAULT = "activity fault";
	final public static String CHILD_FAULT = "child fault";
	final public static String CHILD_SKIPPED = "child skip";
	final public static String CHILD_RESUMED = "child resume";
	final public static String CHILD_STOPPED = "child stop";

	final public static String STATUS_READY = "Ready";
	final public static String STATUS_COMPLETED = "Completed";
	final public static String STATUS_FAULT = "Failed";
	final public static String STATUS_RETRYING = "Retrying";
	final public static String STATUS_QUEUED = "Queued";
	final public static String STATUS_RUNNING = "Running";
	final public static String STATUS_SUSPENDED = "Suspended";
	final public static String STATUS_SKIPPED = "Skipped";
	final public static String STATUS_STOPPED = "Stopped";
	final public static String STATUS_TIMEOUT = "Timeout";
	final public static String STATUS_CANCELLED = "Cancelled";
	final public static String STATUS_ALLCOMMIT = "AllCommit";
	final public static String STATUS_MULTIPLE = "Multiple";

	final public static String VAR_START_TIME = "_start_time";
	final public static String VAR_END_TIME = "_end_time";
	final public static String VAR_BIZ_STATUS = "_var_biz_status";

	final public static String QUEUINGMECH_JMS = "JMS";
	final public static String QUEUINGMECH_SYNC = "synchronous";

	final public static String PVKEY_STATUS = "_status";
	final public static String PVKEY_RETRY_CNT = "_retryCnt";
	final public static String PVKEY_LOOPBACK_CNT = "_loopBackCnt";

	public static final String STATUS_RESERVED = "Reserved";
	static ObjectMapper objectMapper = new ObjectMapper();

	public int getLoopBackCount(ProcessInstance instance) throws Exception {
		if (instance == null) {
			return 0;
		}
		return instance.getProperty(getTracingTag(), PVKEY_LOOPBACK_CNT) == null ? 0
				: new Integer((Integer) instance.getProperty(getTracingTag(), PVKEY_LOOPBACK_CNT));
	}

	public void addLoopBackCount(ProcessInstance instance) throws Exception {
		int backCount = instance.getProperty(getTracingTag(), PVKEY_LOOPBACK_CNT) == null ? 0
				: new Integer((Integer) instance.getProperty(getTracingTag(), PVKEY_LOOPBACK_CNT));
		instance.setProperty(getTracingTag(), PVKEY_LOOPBACK_CNT, backCount + 1);
	}

	/**
	 * points parent activity (should be kind of ComplexActivity of this activity)
	 */
	@JsonIgnore
	Activity parentActivity = null;

	@JsonIgnore
	public Activity getParentActivity() {
		return parentActivity;
	}

	protected void setParentActivity(Activity parentAct) {
		this.parentActivity = parentAct;
	}

	public void clearParentActivity() {
		this.parentActivity = null;
	}

	String viewId;

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	boolean breakpoint;

	public boolean isBreakpoint() {
		return breakpoint;
	}

	public void setBreakpoint(boolean breakpoint) {
		this.breakpoint = breakpoint;
	}

	EventHandler[] eventHandlers;

	public EventHandler[] getEventHandlers() {
		return eventHandlers;
	}

	public void setEventHandlers(EventHandler[] eventHandlers) {

		this.eventHandlers = eventHandlers;
		if (eventHandlers != null) {
			for (int i = 0; i < eventHandlers.length; i++) {
				Activity eventHandlingActivity = eventHandlers[i].getHandlerActivity();

				eventHandlingActivity.setParentActivity(this);
				autoTag(eventHandlingActivity);

				if (getProcessDefinition() != null)
					getProcessDefinition().registerActivity(eventHandlingActivity);
			}
		}
	}

	@JsonIgnore
	private Vector<Activity> previousActivities;

	protected void autoTag(Activity child) {
		// child.setTracingTag(getTracingTag() + "_" + getChildActivities().size());
		if (getProcessDefinition() == null)
			return;

		if (child.getTracingTag() == null
		/*
		 * ||
		 * (
		 * getProcessDefinition().wholeChildActivities!=null &&
		 * getProcessDefinition().wholeChildActivities.containsKey(child.getTracingTag()
		 * )
		 * )
		 */ ) {
			child.setTracingTag("" + getProcessDefinition().getNextActivitySequence());
		}

		if (child instanceof ComplexActivity) {
			ComplexActivity complexActivity = (ComplexActivity) child;

			for (int i = 0; i < complexActivity.getChildActivities().size(); i++) {
				Activity childAct = (Activity) complexActivity.getChildActivities().get(i);
				autoTag(childAct);
			}
		}
	}

	TextContext name;

	public String getName() {
		if (name == null) {
			return null;
		}
		return name.getText();
	}

	public String getName(String locale) {
		if (name == null) {
			return null;
		}
		return name.getText(locale);
	}

	public void setName(TextContext name) {
		if (name == null) {
			name = new TextContext();
		}

		TextContext oldName = this.name;
		this.name = name;

		// TODO All the properties should be coded like this
		firePropertyChangeEvent(new PropertyChangeEvent(this, "name", oldName, name));
	}

	public void setName(String name) {
		if (getName() == null) {
			TextContext textCtx = null;
			// if(getProcessDefinition() == null) {
			textCtx = TextContext.createInstance();

			// } else {
			// textCtx = TextContext.createInstance(getProcessDefinition());
			// }
			setName(textCtx);
		}
		this.name.setText(name);
	}

	TextContext description;

	public String getDescription() {
		if (description == null) {
			description = TextContext.createInstance();
		}
		return description.getText();
	}

	public void setDescription(TextContext string) {
		description = string;
	}

	public void setDescription(String name) {
		if (getDescription() == null) {
			TextContext textCtx = null;
			// if(getProcessDefinition() == null) {
			textCtx = TextContext.createInstance();

			// } else {
			// textCtx = TextContext.createInstance(getProcessDefinition());
			// }
			setDescription(textCtx);
		}
		this.description.setText(name);
	}

	/**
	 * tracingTag is a identifier for a certain activity within a process
	 * definition.
	 */
	String tracingTag;

	public String getTracingTag() {
		return tracingTag;
	}

	public void setTracingTag(String tag) {
		tracingTag = tag;
	}

	/*
	 * String iconURL;
	 * public String getIconURL() {
	 * return iconURL;
	 * }
	 * public void setIconURL(String iconURL) {
	 * this.iconURL = iconURL;
	 * }
	 */
	/**
	 * for ABC (Activity-Based Costing)
	 */
	int cost;

	public int getCost() {
		return cost;
	}

	public void setCost(int i) {
		cost = i;
	}

	Hashtable extendedAttributes;

	public Hashtable getExtendedAttributes() {
		return extendedAttributes;
	}

	public void setExtendedAttributes(Hashtable value) {
		extendedAttributes = value;
	}

	public void setExtendedAttribute(String k, Object value) {
		if (getExtendedAttributes() == null) {
			setExtendedAttributes(new Hashtable());
		}

		if (value == null)
			getExtendedAttributes().remove(k);
		else
			getExtendedAttributes().put(k, value);

		firePropertyChangeEvent(new PropertyChangeEvent(this, "extendedAttribute", value, value));
	}

	/**
	 * retry limits for activity execution
	 */
	int retryLimit;

	public int getRetryLimit() {
		return retryLimit;
	}

	public void setRetryLimit(int i) {
		retryLimit = i;
	}

	int retryDelay;

	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int l) {
		retryDelay = l;
	}

	boolean isHidden = false;

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean b) {
		isHidden = b;
	}

	int integrity; // range: 1: error, 2: warn, 0: complete

	public int getIntegrity() {
		return integrity;
	}

	public void setIntegrity(int integrity) {
		this.integrity = integrity;
	}

	transient ArrayList propertyChangeListeners = new ArrayList();

	public void addProperyChangeListener(PropertyChangeListener pcl) {
		if (propertyChangeListeners == null)
			propertyChangeListeners = new ArrayList();

		if (!propertyChangeListeners.contains(pcl))
			propertyChangeListeners.add(pcl);
	}

	public void removeProperyChangeListener(PropertyChangeListener pcl) {
		propertyChangeListeners.remove(pcl);
	}

	public void clearProperyChangeListeners() {
		if (propertyChangeListeners == null)
			propertyChangeListeners = new ArrayList();

		propertyChangeListeners.clear();
	}

	public void firePropertyChangeEvent(PropertyChangeEvent pce) {
		if (propertyChangeListeners == null)
			propertyChangeListeners = new ArrayList();

		for (Iterator iter = propertyChangeListeners.iterator(); iter.hasNext();) {
			PropertyChangeListener pcl = (PropertyChangeListener) iter.next();
			pcl.propertyChange(pce);
		}
	}

	boolean isDynamicChangeAllowed = true;

	public boolean isDynamicChangeAllowed() {
		return isDynamicChangeAllowed;
	}

	public void setDynamicChangeAllowed(boolean b) {
		isDynamicChangeAllowed = b;
	}

	boolean isQueuingEnabled = false;

	public boolean isQueuingEnabled() {
		return isQueuingEnabled;
	}

	public void setQueuingEnabled(boolean b) {
		isQueuingEnabled = b;
	}

	String activityIcon;

	public String getActivityIcon() {
		return activityIcon;
	}

	public void setActivityIcon(String activityIconURL) {
		activityIcon = activityIconURL;
	}

	String statusCode;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	protected boolean isFaultTolerant = false;

	public boolean isFaultTolerant() {
		return isFaultTolerant;
	}

	public void setFaultTolerant(boolean isErrorTolerant) {
		this.isFaultTolerant = isErrorTolerant;
	}

	public boolean isFaultTolerant(ProcessInstance instance) {
		return isFaultTolerant() || instance.getProcessTransactionContext().getSharedContext("faultTolerant") != null;
	}

	public Calendar getStartedTime(ProcessInstance instance) {
		try {
			Calendar theTime = (Calendar) instance.getProperty(getTracingTag(), VAR_START_TIME);

			return theTime;
		} catch (Exception e) {
			return null;
		}
	}

	public void setStartedTime(ProcessInstance instance, Calendar theTime) throws Exception {
		instance.setProperty(getTracingTag(), VAR_START_TIME, theTime);
	}

	public void setStartedTime(ProcessInstance instance, Date theTime) throws Exception {
		Calendar theTimeCalendar = Calendar.getInstance();
		theTimeCalendar.setTime(theTime);
		setStartedTime(instance, theTimeCalendar);
	}

	public void setStartedTime(ProcessInstance instance, String theTime) throws Exception {
		String[] yearMonthDate = theTime.split("-");

		Calendar theCalendar = Calendar.getInstance();

		int year = Integer.parseInt(yearMonthDate[0]);
		int month = Integer.parseInt(yearMonthDate[1]) - 1;
		int date = Integer.parseInt(yearMonthDate[2]);

		theCalendar.set(year, month, date);

		setStartedTime(instance, theCalendar);

	}

	public String getBusinessStatus(ProcessInstance instance) throws Exception {
		try {
			// if(true) return "test";
			String theValue = (String) instance.getProperty(getTracingTag(), VAR_BIZ_STATUS);

			return theValue;
		} catch (Exception e) {
			return null;
		}
	}

	public void setBusinessStatus(ProcessInstance instance, String value) throws Exception {
		instance.setProperty(getTracingTag(), VAR_BIZ_STATUS, value);
	}

	public Calendar getEndTime(ProcessInstance instance) {
		try {
			Calendar theTime = (Calendar) instance.getProperty(getTracingTag(), VAR_END_TIME);

			return theTime;
		} catch (Exception e) {
			return null;
		}
	}

	public void setEndTime(ProcessInstance instance, Calendar theTime) throws Exception {
		instance.setProperty(getTracingTag(), VAR_END_TIME, theTime);
	}

	/**
	 * there should be an empty constructor for reflection
	 */
	public Activity() {
		setRetryLimit(0);
		setRetryDelay(60);
	}

	public Activity(String activityName) { // for manual-coding
		this();

		setName(activityName);
	}

	/**
	 * These methods would be called by the the moment of a process instance is
	 * intialized.
	 * You may override this method for initialize some instance data when the
	 * process instance is initialized.
	 * The usual invocation sequence is createInstance --> beforeExecute -->?
	 * executeActivity -->? afterExecute --> fireComplete() --> onEvent (if done or
	 * failed) -->? afterComplete()������
	 * 
	 * @param instanceInCreating ProcessInstance passed from uEngine to initialize
	 *                           the instance.
	 * @return
	 * @throws Exception
	 */
	public ProcessInstance createInstance(ProcessInstance instanceInCreating) throws Exception {
		return instanceInCreating;
	}

	public ProcessInstance createInstance(String instanceId, Map options) throws Exception {
		return null;
	}

	public ProcessInstance createInstance() throws Exception {
		return createInstance((String) null, null);
	}

	/**
	 * returns the root activity for this (child) activity. Basically the return
	 * value should be an instance of 'ProcessDefinition' class.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Activity getRootActivity() {
		Activity temp = this;
		while (temp.getParentActivity() != null)
			temp = temp.getParentActivity();

		return temp;
	}

	/**
	 * returns the process definition of this (child) activity.
	 * 
	 * @return
	 */
	@JsonIgnore
	public ProcessDefinition getProcessDefinition() {
		Activity root = getRootActivity();
		if (root instanceof ProcessDefinition)
			return (ProcessDefinition) root;
		else
			return null;
	}

	/**
	 * This callback method would be called just before the executeActivity()
	 * method.
	 * The usual invocation sequence is createInstance --> beforeExecute -->
	 * executeActivity --> afterExecute --> fireComplete() --> onEvent (if done or
	 * failed) -->? afterComplete()
	 * 
	 * @param instance
	 * @throws Exception
	 */
	protected void beforeExecute(ProcessInstance instance) throws Exception {
		if (instance.isRunning(getTracingTag()))
			instance.addDebugInfo(new UEngineException("Activity.java:: The process is trying to execute the activity ["
					+ getName() + "] more than once."));

		if (getStartedTime(instance) != null) { // means the activity is future activity that needed to be notified to
												// be started

		} else {
			setStartedTime(instance, GlobalContext.getNow(instance.getProcessTransactionContext()));
		}

		// run attached events
		for (Activity childActivity : getProcessDefinition().getChildActivities()) {
			runBoundaryEvents(childActivity, instance);
		}

		ActivityFilter[] activityFilters = getProcessDefinition().getActivityFilters();
		if (activityFilters != null)
			for (int i = 0; i < activityFilters.length; i++)
				if (activityFilters[i] != null)
					activityFilters[i].beforeExecute(this, instance);

		fireActivityEventListeners(instance, "beforeExecute", null);

	}

	private void runBoundaryEvents(Activity activity, ProcessInstance instance) throws Exception {
		if (activity instanceof Event) {
			Event event = (Event) activity;
			if (this.getTracingTag().equals(event.getAttachedToRef())) {
				instance.execute(event.getTracingTag());
			}
		} else if (activity instanceof ScopeActivity) {
			ScopeActivity scopeActivity = (ScopeActivity) activity;
			for (Activity childActivity : scopeActivity.getChildActivities()) {
				runBoundaryEvents(childActivity, instance);
			}
		}
	}

	/**
	 * This callback method would be called just after the fireCompleted() method.
	 * The usual invocation sequence is createInstance ?--> beforeExecute -->?
	 * executeActivity -->? afterExecute --> fireComplete() --> onEvent (if done or
	 * failed) -->? afterComplete()
	 * 
	 * @param instance
	 * @throws Exception
	 */
	protected void afterComplete(ProcessInstance instance) throws Exception {
		setEndTime(instance, GlobalContext.getNow(instance.getProcessTransactionContext()));

		ActivityFilter[] activityFilters = getProcessDefinition().getActivityFilters();
		if (activityFilters != null)
			for (int i = 0; i < activityFilters.length; i++)
				if (activityFilters[i] != null)
					activityFilters[i].afterComplete(this, instance);

		fireActivityEventListeners(instance, "afterComplete", null);

		// add completed activity to transaction history.
		instance.getProcessTransactionContext()
				.addExecutedActivityInstanceContext(new ActivityInstanceContext(this, instance));
		//

	}

	/**
	 * This callback method would be called just after the executeActivity() method.
	 * The usual invocation sequence is createInstance ?--> beforeExecute -->?
	 * executeActivity -->? afterExecute --> fireComplete() --> onEvent (if done or
	 * failed) -->? afterComplete()������
	 * 
	 * @param instance
	 * @throws Exception
	 */
	protected void afterExecute(ProcessInstance instance) throws Exception {
		// add executed(queued) activity to transaction history.
		instance.getProcessTransactionContext()
				.addExecutedActivityInstanceContext(new ActivityInstanceContext(this, instance));
		//

		ActivityFilter[] activityFilters = getProcessDefinition().getActivityFilters();
		if (activityFilters != null)
			for (int i = 0; i < activityFilters.length; i++)
				if (activityFilters[i] != null)
					activityFilters[i].afterExecute(this, instance);

		fireActivityEventListeners(instance, "afterExecute", null);

		Vector messageListeners = instance.getMessageListeners("event");
		for (int i = 0; i < messageListeners.size(); i++) {
			MessageListener messageListener = (MessageListener) getProcessDefinition()
					.getActivity((String) messageListeners.get(i));
			if (messageListener instanceof ScopeActivity) {
				ScopeActivity scopeActivity = (ScopeActivity) messageListener;

				if (scopeActivity.isAncestorOf(this)) {
					EventMessagePayload emp = new EventMessagePayload();
					emp.setTriggerTracingTag(getTracingTag());

					scopeActivity.fireEventHandlers(instance, EventHandler.TRIGGERING_BY_AFTER_CHILD_COMPLETED, emp);

				}
			}
		}

		/*
		 * try{
		 * 
		 * // TODO Auto-generated method stub
		 * if(GlobalContext.logLevelIsDebug){
		 * PrintStream ps;
		 * ps = new PrintStream(
		 * new FileOutputStream(FormActivity.FILE_SYSTEM_DIR + "/" +
		 * UEngineUtil.getCalendarDir() + "/" + instance.getInstanceId() + "." +
		 * getTracingTag() + ".log.xml")
		 * );
		 * ps.print(instance.getProcessTransactionContext().getDebugInfo());
		 * 
		 * }
		 * }catch(Exception e){
		 * (new UEngineException("failed to create execution log:"+ e.getMessage(),
		 * e)).printStackTrace();
		 * }
		 */

	}

	/**
	 * Core logic for the activity should be implemented here.
	 * The usual invocation sequence is createInstance --> beforeExecute -->
	 * executeActivity --> afterExecute --> fireComplete() --> onEvent (if done or
	 * failed) --> afterComplete()
	 * 
	 * @param instance
	 * @throws Exception
	 */
	abstract protected void executeActivity(ProcessInstance instance) throws Exception;

	/**
	 * we recommend rather use method 'fireComplete()' or 'fireFault()' instead of
	 * this method.
	 * 
	 */
	protected void onEvent(String command, ProcessInstance instance, Object payload) throws Exception {

		if (instance.fireActivityEventInterceptor(this, command, instance, payload))
			return;

		// review: performance: need to use 'Hashtable' to locate the command or
		// directly invocation from fire... methods.
		if (command.equals(ACTIVITY_DONE)) {
			Activity theActivityHasBeenDone = ((Activity) payload);

			// In BPMN executions, multiple activity completions (multiple-tokens) are
			// permitted.

			// if(Activity.STATUS_COMPLETED.equals(theActivityHasBeenDone.getStatus(instance))){
			// if(!(theActivityHasBeenDone instanceof SubProcessActivity)){
			// //SubProcessActivity spa = (SubProcessActivity)theActivityHasBeenDone;
			//
			// //TODO: it should block the completion only when the sub process activity is
			// in the event handler
			//// throw new UEngineException("Activity [" + theActivityHasBeenDone + "] tries
			// to notify completion event twice. Check whether the activity calls
			// 'fireComplete(instance)' more than once.");
			// }
			// }

			setStatus(instance, STATUS_COMPLETED);
			// review: Ensure subclasses are not overrided this method.
			afterComplete(instance);

			if (!isFaultTolerant() && getParentActivity() != null)
				getParentActivity().onEvent(CHILD_DONE, instance, this);
		} else if (command.equals(ACTIVITY_FAULT)) {
			FaultContext fc = (FaultContext) payload;
			instance.setFault(getTracingTag(), fc);

			if (getParentActivity() != null)
				getParentActivity().onEvent(CHILD_FAULT, instance, fc);
		} else if (command.equals(ACTIVITY_COMPENSATED)) {
			if (getParentActivity() != null)
				getParentActivity().onEvent(CHILD_COMPENSATED, instance, payload);
			for (Activity childActivity : getProcessDefinition().getChildActivities()) {
				if (childActivity instanceof CompensateEvent) {
					CompensateEvent compensateEvent = (CompensateEvent) childActivity;
					if (compensateEvent.getAttachedToRef() != null
							&& compensateEvent.getAttachedToRef().equals(getTracingTag())) {
						compensateEvent.onMessage(instance, compensateEvent.getTracingTag());
					}
				}
			}
		} else if (command.equals(ACTIVITY_SKIPPED)) {
			instance.setStatus(getTracingTag(), STATUS_SKIPPED);

			if (getParentActivity() != null)
				getParentActivity().onEvent(CHILD_SKIPPED, instance, payload);
		} else if (command.equals(ACTIVITY_RESUMED)) {
			if (getParentActivity() != null) {
				// Test -> workitem에 정상적으로 추가 안됨.
				// String status = getParentActivity().getStatus(instance);
				// if (!status.equals(Activity.STATUS_RUNNING)) {
				// getParentActivity().onEvent(CHILD_RESUMED, instance, payload);
				// }
				getParentActivity().onEvent(CHILD_RESUMED, instance, payload);
			}
		} else if (command.equals(ACTIVITY_STOPPED)) {
			instance.setStatus(getTracingTag(), Activity.STATUS_STOPPED);

			if (getParentActivity() != null)
				getParentActivity().onEvent(CHILD_STOPPED, instance, payload);
		} else if (command.equals(ACTIVITY_CHANGED)) {
			onChanged(instance);
		}

		fireEventToActivityFilters(instance, command, payload);
		fireActivityEventListeners(instance, command, payload);

	}

	public void backToHere(ProcessInstance instance) throws Exception {
		// ProcessInstance instance = getProcessInstanceLocal(instanceId);

		String execScope = null;
		if (tracingTag.contains(":")) {
			execScope = tracingTag.split(":")[1];
			tracingTag = tracingTag.split(":")[0];
		}
		if (execScope != null) {
			instance.setExecutionScope(execScope);
		}

		List<ActivityInstanceContext> runningActivities = instance.getCurrentRunningActivitiesDeeply();
		int depth = getProcessDefinition().getDepthFromStartEvent(this);
		for (ActivityInstanceContext activityInstanceContext : runningActivities) {
			Activity activity = activityInstanceContext.getActivity();
			int runningDepth = getProcessDefinition().getDepthFromStartEvent(activity);

			if (runningDepth != -1) {
				if (runningDepth < depth) {
					throw new UEngineException(
							"Activity [" + getTracingTag() + "] is next for [" + activity.getTracingTag() + "]");
				}
			}

			if (activity.getTracingTag().equals(getTracingTag())) {
				throw new UEngineException("Activity [" + getTracingTag() + "] is already running.");
			}
		}

		if (instance.getStatus(this.getTracingTag()).equals(STATUS_RUNNING)
				&& instance.getStatus(this.getTracingTag()).equals(STATUS_CANCELLED)) {
			throw new UEngineException("Activity [" + getTracingTag() + "] is not available.");
		}

		System.out.println("**********************");
		System.out.println("getInstanceId : " + instance.getInstanceId());
		System.out.println("getExecutionScopeContext : " + instance.getExecutionScopeContext());
		System.out.println("**********************");

		List<ProcessInstance> list = new ArrayList<ProcessInstance>();
		Map<String, List<Activity>> map = new HashMap<String, List<Activity>>();
		recursivePropagatedActivities(instance, this, map, list);
		ProcessInstance proInstance;
		for (int i = list.size() - 1; i >= 0; i--) {
			proInstance = list.get(i);

			List<Activity> activities = map.get(proInstance.getInstanceId());
			Activity proActivity;
			for (int y = activities.size() - 1; y >= 0; y--) {
				proActivity = activities.get(y);
				proActivity.compensate(proInstance);
				proActivity.resetFlow(proInstance);
			}
		}

		compensateToThis(instance);
		resetFlowToThis(instance);
		resume(instance);
		/*
		 * ProcessDefinition extends FlowActivity 상속하고 있기 때문에,
		 * List list = new ArrayList();
		 * definition.gatherPropagatedActivitiesOf(instance,
		 * definition.getWholeChildActivity(tracingTag), list);
		 * 
		 * list 를 역순으로 하여 발견된 각 activity 들에 대해 compensate() 호출
		 */

		// return new InstanceResource(instance);
	}

	public void resetFlow(ProcessInstance instance) throws Exception {
		if (getIncomingSequenceFlows().size() > 0) {
			for (SequenceFlow flow : getIncomingSequenceFlows()) {
				instance.setStatus(flow.getTracingTag(), STATUS_READY);
			}
		}
	}

	public void resetFlowToThis(ProcessInstance instance) throws Exception {
		if (instance.isSubProcess()) {
			String instanceStatus = instance.getStatus();

			if (instanceStatus.equals(Activity.STATUS_COMPLETED) || instanceStatus.equals(Activity.STATUS_SKIPPED)
					|| instanceStatus.equals(Activity.STATUS_FAULT)) {

				String returningTracingTag = (String) instance.getMainActivityTracingTag();
				String returningInstanceId = (String) instance.getMainProcessInstanceId();

				Hashtable options = new Hashtable();
				options.put("ptc", instance.getProcessTransactionContext());

				ProcessInstance returningInstance = AbstractProcessInstance.create().getInstance(returningInstanceId,
						options);
				ProcessDefinition returningDefinition = returningInstance.getProcessDefinition();
				SubProcessActivity returningActivity = (SubProcessActivity) returningDefinition
						.getActivity(returningTracingTag);

				returningActivity.resetFlow(returningInstance);
			}
		}
	}

	public void recursivePropagatedActivities(ProcessInstance instance, Activity activity,
			Map<String, List<Activity>> map, List<ProcessInstance> list) throws Exception {
		ProcessDefinition definition = instance.getProcessDefinition();
		List<Activity> activities = new ArrayList<Activity>();
		definition.gatherPropagatedActivitiesOf(instance, activity, activities);

		map.put(instance.getInstanceId(), activities);
		list.add(instance);
		if (instance.isSubProcess()) {
			String tracingTag = instance.getMainActivityTracingTag();
			ProcessInstance pi = instance.getMainProcessInstance();
			ProcessDefinition def = pi.getProcessDefinition();
			Activity act = def.getActivity(tracingTag);
			recursivePropagatedActivities(pi, act, map, list);
		}
	}

	// definition.getActivity()
	// ㅁ-ㅁ

	/**
	 * only when needed to reserve activity before running, it stores the runner
	 * ticket for trigger the activity later.
	 * 
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	public void reserveActivity(ProcessInstance instance) throws Exception {
		setStatus(instance, Activity.STATUS_RESERVED);
	}

	/**
	 * This callback method whould be called when this activity is dynamically
	 * changed (added)
	 * 
	 * @param instance
	 * @throws Exception
	 */
	protected void onChanged(ProcessInstance instance) throws Exception {
	}

	// TODO this method is generic but may pose error-prone codes
	/*
	 * public void fireEvent(String event, ProcessInstance instance, Object payload)
	 * throws Exception{
	 * onEvent(event, instance, payload);
	 * }
	 */

	/**
	 * fires a completion for this activity. You should invoke this method in the
	 * end of executeActivity() method if the activity is a synchronous job.
	 * In the other hand, if you implement a asynchronous job of activity, you calls
	 * this fireComplete() after done (or after receiving the result).
	 */
	public void fireComplete(ProcessInstance instance) throws Exception {
		// This must be before than the onEvent since connected activity would not
		// recognize this is completed.
		if (!(this instanceof ProcessDefinition))
			instance.getActivityCompletionHistory().add(getTracingTag());

		setTokenCount(instance, 0);
		onEvent(ACTIVITY_DONE, instance, this);
	}

	public void notifyCompletionToParent(ProcessInstance instance) throws Exception {
		if (getParentActivity() != null)
			getParentActivity().onEvent(CHILD_DONE, instance, this);
	}

	public void fireSkipped(ProcessInstance instance) throws Exception {
		onEvent(ACTIVITY_SKIPPED, instance, this);
	}

	public void fireCompensate(ProcessInstance instance) throws Exception {
		onEvent(ACTIVITY_COMPENSATED, instance, this);
	}

	public void fireFault(ProcessInstance instance, FaultContext faultContext) throws Exception {
		onEvent(ACTIVITY_FAULT, instance, faultContext);
	}

	public void fireFault(ProcessInstance instance, Exception e) throws Exception {

		UEngineException fault;
		if (e instanceof UEngineException)
			fault = (UEngineException) e;
		else if (e instanceof Throwable) {
			fault = new UEngineException(((Throwable) e).getMessage(), (Throwable) e);
		} else {
			fault = new UEngineException("" + e);
		}

		FaultContext fc = new FaultContext();
		fc.setCauseActivity(this);
		fc.setFault(fault);

		fireFault(instance, fc);

	}

	public void fireResume(ProcessInstance instance) throws Exception {
		instance.setEventOriginator(ACTIVITY_RESUMED, this);
		onEvent(ACTIVITY_RESUMED, instance, this);
	}

	public void fireChanged(ProcessInstance instance) throws Exception {
		onEvent(ACTIVITY_CHANGED, instance, this);
	}

	public void stop(ProcessInstance instance) throws Exception {
		String currentStatus = getStatus(instance);

		if (!STATUS_FAULT.equals(currentStatus))
			stop(instance, Activity.STATUS_STOPPED);
	}

	public void stop(ProcessInstance instance, String status) throws Exception {
		onEvent(ACTIVITY_STOPPED, instance, this);
		instance.setStatus(getTracingTag(), status);
	}

	public void suspend(ProcessInstance instance) throws Exception {
		reset(instance);
		instance.setStatus(getTracingTag(), STATUS_SUSPENDED);
	}

	public void resume(ProcessInstance instance) throws Exception {
		// instance.setStatus(getTracingTag(), STATUS_RUNNING);
		// need to check the tasks that have not completed yet or is still running..
		// executeActivity(instance); //may occur some flow control error\
		fireResume(instance);
	}

	public void reset(ProcessInstance instance) throws Exception {
		instance.setStatus(getTracingTag(), STATUS_READY);
	}

	public void skip(ProcessInstance instance) throws Exception {
		fireSkipped(instance);
	}

	public void setNewTaskId(ProcessInstance instance) throws Exception {
	}

	public void executeAttachedEvent(ProcessInstance instance) throws Exception {
		EventHandler[] eventHandlers = getEventHandlers();

		if (eventHandlers != null) {
			for (int i = 0; i < eventHandlers.length; i++) {
				// Activity eventHandlingActivity = eventHandlers[i].getHandlerActivity();

				if (eventHandlers[i].getTriggeringMethod() == EventHandler.TRIGGERING_BY_COMPENSATION) {
					EventMessagePayload eventMessage = new EventMessagePayload();
					eventMessage.setEventName(
							eventHandlers[i].getName() + eventHandlers[i].getHandlerActivity().getTracingTag());
					eventMessage.setTriggerTracingTag(eventHandlers[i].getHandlerActivity().getTracingTag());
					// fireEventHandlers(instance, 11, eventMessage);
					CompensateEvent activity = (CompensateEvent) instance.getProcessDefinition()
							.getActivity(eventHandlers[i].getHandlerActivity().getTracingTag());
					activity.onMessage(instance, eventHandlers);
				}
			}
		}

		for (Activity childActivity : getProcessDefinition().getChildActivities()) {
			if (childActivity instanceof Event) {
				Event event = (Event) childActivity;
				if (this.getTracingTag().equals(event.getAttachedToRef())) {
					getProcessDefinition().fireMessage(event.getTracingTag(), instance, event.getTracingTag());

					instance.setStatus(event.getTracingTag(), STATUS_READY);

				}
			}
		}
	}

	public void compensate(ProcessInstance instance) throws Exception {
		reset(instance);

		executeAttachedEvent(instance);

		fireCompensate(instance);
	}

	public void compensateOneStep(ProcessInstance instance) throws Exception {
		// reset(instance);
		suspend(instance);
	}

	/**
	 * TODO: is this signature definition enough for full dynamic change?
	 */
	public void change(ProcessInstance instance) throws Exception {
		fireChanged(instance);
	}

	public Map getActivityDetails(ProcessInstance inst, String locale) throws Exception {
		Map details = new TreeMap();

		// details.put("name", ""+getName());
		// details.put(GlobalContext.getLocalizedMessage("activitytypes.org.uengine.kernel.activity.details.cost",
		// "cost"), ""+getCost());

		if (inst != null) {
			// try{
			// details.put(GlobalContext.getLocalizedMessage("activitytypes.org.uengine.kernel.activity.details.elapsedtime",
			// "elapsed time"), getElapsedTime(inst));
			// }catch(Exception e){
			// System.out.println("Activity.java : failed to get elapsedtime.");
			// }

			if (inst.getFault(getTracingTag()) != null) {
				UEngineException fault = inst.getFault(getTracingTag());
				if (fault != null)
					details.put(GlobalContext.getLocalizedMessage(
							"activitytypes.org.uengine.kernel.activity.details.fault", locale, "fault"), fault);

				/*
				 * ByteArrayOutputStream bos = new ByteArrayOutputStream();
				 * fault.printStackTrace(new PrintStream(bos));
				 * 
				 * details.put("fault.details", bos.toString());
				 */

				if (fault.getDetails() != null)
					details.put(GlobalContext.getLocalizedMessage(
							"activitytypes.org.uengine.kernel.activity.details.faultdetails", locale, "fault.details"),
							fault.getDetails());
			}

			try {
				Calendar calStartedDate = getStartedTime(inst);
				String strStartedDate = "-";
				if (calStartedDate != null) {
					java.text.DateFormat df = new java.text.SimpleDateFormat(
							GlobalContext.getPropertyString("monitoring.activitydetails.dateformatter", "yyyy-MM-dd"));
					strStartedDate = df.format(calStartedDate.getTime());
				}
				details.put(GlobalContext.getLocalizedMessage(
						"activitytypes.org.uengine.kernel.humanactivity.details.starteddate", locale, "started date"),
						strStartedDate);
			} catch (Exception e) {
			}

			try {
				Calendar calEndDate = getEndTime(inst);
				String strEndDate = "-";
				if (calEndDate != null) {
					java.text.DateFormat df = new java.text.SimpleDateFormat(
							GlobalContext.getPropertyString("monitoring.activitydetails.dateformatter", "yyyy-MM-dd"));
					strEndDate = df.format(calEndDate.getTime());
				}
				details.put(
						GlobalContext.getLocalizedMessage(
								"activitytypes.org.uengine.kernel.humanactivity.details.enddate", locale, "end date"),
						strEndDate);
			} catch (Exception e) {
			}
		}

		return details;
	}

	public ValidationContext validate(Map options) {
		ValidationContext vc = new ValidationContext();

		if (!UEngineUtil.isNotEmpty(getTracingTag()))
			vc.add(getActivityLabel() + " : no tracingTag. ");

		/*
		 * 일단 필요가 없어 보이는 로직이라서 주석 처리함 14.2.27 김형국
		 * try{
		 * ComplexActivity.USE_JMS = false;
		 * ComplexActivity.USE_THREAD = false;
		 * 
		 * ProcessInstance instance
		 * = new DefaultProcessInstance(getProcessDefinition(), "test instance", null);
		 * 
		 * Method[] methods = getClass().getMethods();
		 * for(int i=0; i<methods.length; i++){
		 * try{
		 * Method method = methods[i];
		 * if(
		 * method.getName().startsWith("get")
		 * && method.getParameterTypes().length==0
		 * && (method.getReturnType()==String.class ||
		 * TextContext.class.isAssignableFrom(method.getReturnType()))
		 * ){
		 * 
		 * String setterName = "s" + method.getName().substring(1);
		 * 
		 * boolean setterMethodExists = false;
		 * try{
		 * getClass().getMethod(setterName, new Class[]{String.class});
		 * setterMethodExists = true;
		 * }catch(Exception e){
		 * }
		 * 
		 * if(!setterMethodExists)
		 * try{
		 * getClass().getMethod(setterName, new Class[]{TextContext.class});
		 * setterMethodExists = true;
		 * }catch(Exception e){
		 * }
		 * 
		 * if(setterMethodExists)
		 * {
		 * //System.out.println("validating: in " + getName() + ", property = " +
		 * method.getName());
		 * Object value = method.invoke(this, new Object[]{});
		 * if(value!=null){
		 * if(value instanceof String)
		 * evaluateContent(instance, (String)value, vc);
		 * else
		 * evaluateContent(instance, ((TextContext)value).getText(), vc);
		 * }
		 * }
		 * }
		 * }catch(Exception e){
		 * }
		 * }
		 * 
		 * }catch(Exception e){
		 * //e.printStackTrace();
		 * }
		 */

		// 1. Transition 에 condition이 있으면 otherwise 가 1개는 있어야함
		// 2. condition이 1개라도 있으면 나머지 transition에도 condition이 있어야함.

		// boolean otherwiseCondition = false;
		boolean isCondition = false;
		boolean emptyCondition = false; // 컨디션이 전혀 없는 경우는 true
		for (Iterator<SequenceFlow> it = getOutgoingSequenceFlows().iterator(); it.hasNext();) {
			SequenceFlow ts = (SequenceFlow) it.next();
			if (ts.getCondition() != null) {
				isCondition = true;
				// Condition condition = ts.getCondition();
				// if (condition instanceof And) {
				// Condition[] condis = ((Or) condition).getConditions();
				// if (condis[0] instanceof Otherwise) {
				// otherwiseCondition = true;
				// }
				// }
			} else {
				emptyCondition = true;
			}
		}
		// if (isCondition && !otherwiseCondition) { // 컨디션이 존재하는데 otherwise가 없을때
		// vc.add(" : no otherwise condition. ");
		// }
		if (isCondition && emptyCondition) { // 컨디션이 존재하는데 어떤 선은 컨디션이 없을때
			vc.add("출력 시퀀스 플로우의 컨디션이 하나라도 존재하면 동일한 분기의 모든 시퀀스플로우의 컨디션이 존재해야 합니다.");
		}

		if (getIncomingSequenceFlows().size() < 1) {
			vc.add("해당 블록에 들어오는 시퀀스 플로우가 존재하지 않습니다.");
		}

		if (getOutgoingSequenceFlows().size() < 1) {
			Boolean requiredOutgoing = true;
			if (getIncomingSequenceFlows().size() > 0) {
				for (SequenceFlow sequenceFlow : getIncomingSequenceFlows()) {
					if (sequenceFlow.getSourceActivity() instanceof CompensateEvent) {
						requiredOutgoing = false;
					}
				}
			}
			if (requiredOutgoing)
				vc.add("해당 블록에서 나가는 시퀀스 플로우가 존재하지 않습니다.");
		}

		Set<Activity> visitedActivities = new HashSet();
		boolean isCircularReference = false;
		for (SequenceFlow sequenceFlow : getOutgoingSequenceFlows()) {
			Activity targetActivity = sequenceFlow.getTargetActivity();
			if (hasCircularReference(targetActivity, visitedActivities)) {
				isCircularReference = true;
				break;
			}
		}
		if (isCircularReference) {
			vc.addWarning("이 블록은 최종적으로 자기 자신을 참조하도록 되어 있어 무한 루프의 가능성이 있습니다.");
		}

		return vc;
	}

	private boolean hasCircularReference(Activity activity, Set<Activity> visitedActivities) {
		if (visitedActivities.contains(activity)) {
			return true;
		}

		visitedActivities.add(activity);

		for (SequenceFlow sequenceFlow : activity.getOutgoingSequenceFlows()) {
			Activity targetActivity = sequenceFlow.getTargetActivity();
			if (hasCircularReference(targetActivity, visitedActivities)) {
				return true;
			}
		}

		visitedActivities.remove(activity);
		return false;
	}

	public void usabilityCheck(Map checkingValues) {
		try {
			ComplexActivity.USE_JMS = false;
			ComplexActivity.USE_THREAD = false;
			Method[] methods = getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				try {
					Method method = methods[i];
					if (method.getName().startsWith("get")) {
						Object value = method.invoke(this, new Object[] {});
						if (checkingValues.containsKey(value)) {
							checkingValues.put(value, true);
						}
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	protected String getActivityLabel() {
		return "[At " + getName() + " Activity (" + getTracingTag() + ")] ";
	}

	public boolean isRunning(ProcessInstance instance) throws Exception {
		String status = getStatus(instance);

		return Activity.STATUS_RUNNING.equals(status);
	}

	public static boolean isSkippable(String status) {
		return !(status.equals(Activity.STATUS_SKIPPED) || status.equals(Activity.STATUS_READY)
				|| status.equals(Activity.STATUS_CANCELLED) || status.equals(Activity.STATUS_COMPLETED));
	}

	public static boolean isStoppable(String status) {
		return !(status.equals(Activity.STATUS_READY) || status.equals(Activity.STATUS_CANCELLED)
				|| status.equals(Activity.STATUS_COMPLETED) || status.equals(Activity.STATUS_TIMEOUT));
	}

	public static boolean isCompensatable(String status) {
		return !(status.equals(Activity.STATUS_SKIPPED) || status.equals(Activity.STATUS_READY)
				|| status.equals(Activity.STATUS_CANCELLED) || status.equals(Activity.STATUS_COMPLETED));
	}

	public static boolean isResumable(String status) {
		return (status.equals(Activity.STATUS_SUSPENDED) || status.equals(Activity.STATUS_FAULT));
	}

	public static boolean isSuspendable(String status) {
		return (status.equals(Activity.STATUS_RUNNING) || status.equals(Activity.STATUS_TIMEOUT));
	}

	public static boolean isCompletable(String status) {
		return (status.equals(Activity.STATUS_RUNNING) || status.equals(Activity.STATUS_TIMEOUT));
	}
	//

	public String getStatus(final ProcessInstance instance) throws Exception {
		String status = null;

		if (instance != null) {
			try {
				status = (String) instance.getProperty(getTracingTag(), Activity.PVKEY_STATUS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (status == null)
			status = STATUS_READY;

		if (STATUS_READY.equals(status)) {
			ExecutionScopeContext escTree = instance
					.getExecutionScopeContextTree(instance.getExecutionScopeContext() != null
							? instance.getExecutionScopeContext().getExecutionScope()
							: null);

			if (escTree.getChilds() != null && escTree.getChilds().size() > 0) {

				// if(getParentActivity() instanceof SubProcess){

				// TODO: this is temporal. it is not exact logic.
				String parentStatus = getParentActivity().getStatus(instance);
				if (STATUS_RUNNING.equals(parentStatus)) {
					status = STATUS_MULTIPLE;
				} else {
					status = parentStatus;
				}
			}
		}

		// try to find child execution scopes and try to create an aggregated status
		// message
		// if(status==null){
		//
		// final HashMap<String, Integer> countPerStatus = new HashMap<String,
		// Integer>();
		//
		// ExecutionScopeContext escTree =
		// instance.getExecutionScopeContextTree(instance.getExecutionScopeContext().getExecutionScope());
		//
		// if(escTree.getChilds()!=null){
		// new TreeVisitor<ExecutionScopeContext>(){
		//
		// @Override
		// public List<ExecutionScopeContext> getChild(ExecutionScopeContext parent){
		// return parent.getChilds();
		// }
		//
		// @Override
		// public void logic(ExecutionScopeContext elem){
		// try {
		// new
		// InExecutionScope(instance.getExecutionScopeContext().getExecutionScope()){
		//
		// @Override
		// public Object logic(ProcessInstance instance) throws Exception {
		//
		// String status = (String)instance.getProperty(getTracingTag(),
		// Activity.PVKEY_STATUS);
		//
		// if(status!=null) {
		// int count = 0;
		//
		// if(countPerStatus.containsKey(status))
		// count = countPerStatus.get(status);
		//
		// countPerStatus.put(status, count + 1);
		// }
		//
		// return null;
		// }
		// }.run(instance);
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
		// }
		//
		// }.run(escTree);
		//
		// String aggregatedStatus = "";
		// if(countPerStatus.size()>0){
		// String sep = "";
		// for(String key : countPerStatus.keySet()){
		// aggregatedStatus += sep + key + countPerStatus.get(key);
		// sep = ", ";
		// }
		//
		// return aggregatedStatus;
		// }
		// }
		// }

		return status;
	}

	public void setStatus(ProcessInstance instance, String status) throws Exception {
		instance.setStatus(getTracingTag(), status); // ban to illegal invocation with invalid tracing tag

		firePropertyChangeEventToActivityFilters(instance, Activity.PVKEY_STATUS, status);
	}

	public int getRetryCount(ProcessInstance instance) throws Exception {
		Integer cnt = null;

		if (instance != null) {
			try {
				cnt = (Integer) instance.getProperty(getTracingTag(), Activity.PVKEY_RETRY_CNT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (cnt == null)
			return 0;

		return cnt.intValue();
	}

	public void setRetryCount(ProcessInstance instance, int retryCnt) throws Exception {
		instance.setProperty(getTracingTag(), PVKEY_RETRY_CNT, new Integer(retryCnt)); // ban to illegal invocation with
																						// invalid tracing tag
	}

	public String getElapsedTime(ProcessInstance instance) throws Exception {

		long elapsedTime = getElapsedTimeAsLong(instance);

		if (elapsedTime == -1)
			return "-";

		String hour = "" + elapsedTime / 3600000L;
		String min = "" + (elapsedTime % 3600000L) / 60000L;

		return hour + " hr " + min + " min";
	}

	public long getElapsedTimeAsLong(ProcessInstance instance) throws Exception {
		Calendar startedTime = getStartedTime(instance);
		Calendar endTime = getEndTime(instance);

		if (startedTime == null)
			return -1;

		if (endTime == null)
			endTime = GlobalContext.getNow(instance.getProcessTransactionContext());

		return endTime.getTimeInMillis() - startedTime.getTimeInMillis();
	}

	public Activity findParentActivity(Class type) {
		Activity tracing = this;
		do {
			if (type.isAssignableFrom(tracing.getClass())) {
				return tracing;
			}

			tracing = tracing.getParentActivity();
		} while (tracing != null);

		return null;
	}

	public Object clone() {
		// TODO [tuning point]: Object cloning with serialization. it will be called by
		// ProcessManagerBean.getProcessDefintionXX method.

		try {

			if (true) {

				String strInAct = GlobalContext.serialize(this, String.class);
				Activity clonedActivity = (Activity) GlobalContext.deserialize(strInAct);
				return clonedActivity;
			} else {

				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				ObjectOutputStream ow = new ObjectOutputStream(bao);
				ow.writeObject(this);
				ByteArrayInputStream bio = new ByteArrayInputStream(bao.toByteArray());
				ObjectInputStream oi = new ObjectInputStream(bio);

				return oi.readObject();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Vector getPreviousActivities() {
		ComplexActivity parent = ((ComplexActivity) getParentActivity());
		if (parent == null)
			return null;

		return parent.getPreviousActivitiesOf(this);
	}

	public StringBuffer evaluateContent(ProcessInstance instance, String expression,
			ValidationContext validationContext) {

		StringBuffer generating = new StringBuffer("");

		if (expression == null)
			return generating;

		int pos = 0, endpos = 0, oldpos = 0;
		String key;
		String starter = "<%", ending = "%>";

		if (expression.contains(starter)) {
			oldpos = evaluateContent_(instance, expression, validationContext, generating, oldpos, starter, ending);
			generating.append(expression.substring(oldpos));

		}

		else { // use spEL
			generating.append(evaluateBySpEL(expression, instance));
		}

		return generating;

	}

	static final String starter = "{";
	static final String ending = "}";

	public String evaluateBySpEL(String expression, ProcessInstance instance) {
		boolean allIsNull = true;
		try {
			SpelExpressionParser expressionParser = new SpelExpressionParser();
			int pos;
			int oldpos = 0;
			int endpos;
			String key;
			StringBuffer generating = new StringBuffer();
			StandardEvaluationContext context = new StandardEvaluationContext();
			context.setRootObject(instance);

			// context.setVariable("tenant", TenantContext.getThreadLocalInstance());

			Map<String, Object> rootObject = new HashMap<>();
			if (getProcessDefinition().getProcessVariables() != null) {
				for (ProcessVariable processVariable : getProcessDefinition().getProcessVariables()) {
					rootObject.put(processVariable.getName(), instance.get("", processVariable.getName()));
				}
			}

			rootObject.put("instance", instance);
			rootObject.put("activity", this);
			rootObject.put("systemEnvironments", System.getenv());
			rootObject.put("systemProperties", System.getProperties());
			context.setRootObject(rootObject);
			context.addPropertyAccessor(new MapAccessor());

			while ((pos = expression.indexOf(starter, oldpos)) > -1) {
				pos += starter.length();
				endpos = expression.indexOf(ending, pos);
				if (endpos > pos) {
					generating.append(expression.substring(oldpos, pos - starter.length()));
					key = expression.substring(pos, endpos);
					key = key.trim();
					Object val = null;
					try {
						val = expressionParser.parseExpression(key).getValue(context);
						allIsNull = false;
					} catch (Exception e) {
						throw e;
					}
					if (val != null)
						generating.append(val.toString());
				}
				oldpos = endpos + ending.length();
			}
			generating.append(expression.substring(oldpos));
			return generating.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error to parse expression " + expression, e);
		}
	}

	private int evaluateContent_(ProcessInstance instance, String expression, ValidationContext validationContext,
			StringBuffer generating, int oldpos, String starter, String ending) {
		int pos;
		int endpos;
		String key;

		while ((pos = expression.indexOf(starter, oldpos)) > -1) {
			pos += starter.length();
			endpos = expression.indexOf(ending, pos);
			// System.out.println("oldpos="+oldpos +"; pos = "+pos);
			if (endpos > pos) {
				generating.append(expression.substring(oldpos, pos - starter.length()));
				key = expression.substring(pos, endpos);
				if (key.startsWith("="))
					key = key.substring(1, key.length());
				if (key.startsWith("*"))
					key = key.substring(1, key.length());
				if (key.startsWith("+"))
					key = key.substring(1, key.length());

				key = key.trim();

				System.out.println("Activity:: evaluateContent: key=" + key);
				Object val = Activity.getSpecialKeyValues(this, instance, key, validationContext);
				// System.out.println("EMailActivity:: parseContent: val:"+val);

				if (val != null) {
					if (val instanceof Map) {
						try {
							generating.append(objectMapper.writeValueAsString(val));
						} catch (IOException e) {
							throw new RuntimeException("failed to convert to JSON", e);
						}
					} else

						generating.append("" + val);
				}
			}
			oldpos = endpos + ending.length();
		}
		return oldpos;
	}

	public StringBuffer evaluateContent(ProcessInstance instance, String expression) {
		return evaluateContent(instance, expression, null);
	}

	static HashMap getterMethodsHT = new HashMap();

	private static Method getGetterMethod(Class targetCls, String propertyName) {
		String prefix = targetCls.getName().toLowerCase() + ".";
		propertyName = propertyName.toLowerCase();

		if (!getterMethodsHT.containsKey(targetCls.getName())) {
			Method[] methods = targetCls.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().startsWith("get") && methods[i].getParameterTypes().length == 0)
					getterMethodsHT.put(prefix + methods[i].getName().toLowerCase(), methods[i]);
			}
			getterMethodsHT.put(targetCls.getName(), "exists");
		}

		if (getterMethodsHT.containsKey(prefix + "get" + propertyName))
			return (Method) getterMethodsHT.get(prefix + "get" + propertyName);

		return null;

	}

	public static Object getSpecialKeyValues(Activity activity, ProcessInstance instance, String k,
			ValidationContext vc) {
		// k = k.toLowerCase();
		if (k.toLowerCase().startsWith("definition.")) {
			try {
				String suffix = k.substring("definition.".length(), k.length());
				suffix = suffix.trim();

				Method getter = getGetterMethod(activity.getProcessDefinition().getClass(), suffix);
				Object value;
				if (getter != null) {
					value = getter.invoke(activity.getProcessDefinition(), new Object[] {});

					return value;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (k.toLowerCase().startsWith("activity.")) {
			try {
				String suffix = k.substring("activity.".length(), k.length());
				suffix = suffix.trim();

				Method getter = getGetterMethod(activity.getClass(), suffix);
				Object value;
				if (getter != null) {
					value = getter.invoke(activity, new Object[] {});

					return value;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (instance == null)
			return null;

		if (k.toLowerCase().startsWith("instance.")) { // <%=instance.name%>
			try {
				String suffix = k.substring("instance.".length(), k.length());
				suffix = suffix.trim();

				Method getter = getGetterMethod(instance.getClass(), suffix);
				Object value;
				if (getter != null) {
					value = getter.invoke(instance, new Object[] {});

					return value;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (k.toLowerCase().startsWith("roles.")) {
			try {
				String[] elements = k.replace('.', '@').split("@");
				String roleName = null;
				String suffixName = null;
				if (elements.length > 1) {
					roleName = elements[1].trim();
				}
				if (elements.length > 2) {
					suffixName = elements[2];
				}

				Role theRole = (roleName != null ? instance.getProcessDefinition().getRole(roleName) : null);

				if (theRole != null) {
					// if(!GlobalContext.isDesignTime()){
					RoleMapping theMapping = theRole.getMapping(instance);

					if (theMapping != null && suffixName != null) {
						Method getter = getGetterMethod(theMapping.getClass(), suffixName);
						Object value;
						if (getter != null) {
							value = getter.invoke(theMapping, new Object[] {});

							return value;
						}
					} else {
						return theMapping;
					}
					// }
				} else {
					vc.add("Undeclared role name [" + roleName + "].");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (instance == null)
					return null;
				Object value = instance.get(k);
				if (value != null) {
					return value;
				} else {
					if (vc != null)
						vc.addAll(instance.getValidationContext());

					return null;
				}

			} catch (Exception e) {

				Object value = null;
				try {
					value = instance.getProperty("", k);

					if (value != null)
						return value;

				} catch (Exception e1) {
					e1.printStackTrace();
				}

				e.printStackTrace();
			}
		}

		if (vc != null)
			vc.addWarning("the expression '" + k + "' cannot be resolved.");

		return null;
	}

	protected void gatherPropagatedActivities(ProcessInstance instance, List list) throws Exception {
		ComplexActivity parent = ((ComplexActivity) getParentActivity());
		if (parent == null)
			return;

		parent.gatherPropagatedActivitiesOf(instance, this, list);
	}

	public List getPropagatedActivities(ProcessInstance instance) throws Exception {
		List actList = new ArrayList();
		gatherPropagatedActivities(instance, actList);

		return actList;
	}

	public List compensateToThis(ProcessInstance instance) throws Exception {
		return compensateToThis(instance, true);
	}

	public List compensateToThis(ProcessInstance instance, boolean compensateAndResume) throws Exception {
		List superCompensatingList = null;
		if (instance.isSubProcess()) {
			String instanceStatus = instance.getStatus();

			if (instanceStatus.equals(Activity.STATUS_COMPLETED) || instanceStatus.equals(Activity.STATUS_SKIPPED)
					|| instanceStatus.equals(Activity.STATUS_FAULT)) {
				String returningTracingTag = (String) instance.getMainActivityTracingTag();
				String returningInstanceId = (String) instance.getMainProcessInstanceId();

				Hashtable options = new Hashtable();
				options.put("ptc", instance.getProcessTransactionContext());

				ProcessInstance returningInstance = AbstractProcessInstance.create().getInstance(returningInstanceId,
						options);
				ProcessDefinition returningDefinition = returningInstance.getProcessDefinition();
				SubProcessActivity returningActivity = (SubProcessActivity) returningDefinition
						.getActivity(returningTracingTag);

				// if(returningInstance.getStatus().equals(Activity.STATUS_COMPLETED) ||
				// returningInstance.getStatus().equals(Activity.STATUS_SKIPPED))
				// returningDefinition.compensateOneStep(returningInstance);

				superCompensatingList = returningActivity.compensateToThis(returningInstance, false);

				returningActivity.setStatus(returningInstance, Activity.STATUS_RUNNING);

				Vector completedSpIds = returningActivity.getSubprocessIds(returningInstance,
						"completedInstanceIdOfSPs");
				completedSpIds.remove(instance.getInstanceId());
				returningActivity.setSubprocessIds(returningInstance, completedSpIds, "completedInstanceIdOfSPs");

				// TODO: should be moved to SubProcessActivity?
				// returningDefinition.compensateOneStep(returningInstance);
				// getProcessDefinition().compensateOneStep(instance);
			}
		}

		List actList = getPropagatedActivities(instance);

		Activity theLastPropagatedActivity = null;
		if (actList.size() > 0) {
			theLastPropagatedActivity = (Activity) actList.get(actList.size() - 1);
			if (Activity.STATUS_COMPLETED.equals(instance.getStatus()) && instance.isSubProcess()
					&& Activity.STATUS_COMPLETED.equals(theLastPropagatedActivity.getStatus(instance))) {
				theLastPropagatedActivity.setStatus(instance, Activity.STATUS_SUSPENDED);
			}
		} else {
			theLastPropagatedActivity = this;
		}

		ComplexActivity theRootOfLastPropagatedActivity = (ComplexActivity) theLastPropagatedActivity
				.getParentActivity();
		while (theRootOfLastPropagatedActivity.getParentActivity() != null
				&& STATUS_COMPLETED.equals(theRootOfLastPropagatedActivity.getParentActivity().getStatus(instance))) {
			theRootOfLastPropagatedActivity = (ComplexActivity) theRootOfLastPropagatedActivity.getParentActivity();
		}

		if (STATUS_COMPLETED.equals(theRootOfLastPropagatedActivity.getStatus(instance))) {
			if (theRootOfLastPropagatedActivity.getParentActivity() != null)
				((ComplexActivity) theRootOfLastPropagatedActivity.getParentActivity()).compensateChild(instance,
						theRootOfLastPropagatedActivity);
			else
				theRootOfLastPropagatedActivity.compensateOneStep(instance);
		}

		for (int i = actList.size() - 1; i > -1; i--) {
			Activity actToBeCompensated = (Activity) actList.get(i);
			String status = actToBeCompensated.getStatus(instance);
			if (status.equals(Activity.STATUS_SKIPPED)) {
				actToBeCompensated.reset(instance);
			} else if (Activity.isCompensatable(status)) {
				actToBeCompensated.compensate(instance);
			} /*
				 * else{
				 * throw new UEngineException("couldn't compensate!");
				 * }
				 */
		}

		instance.addActivityEventInterceptor(new ActivityEventInterceptor() {

			public boolean interceptEvent(Activity activity, String command,
					ProcessInstance instance, Object payload) throws Exception {

				if (activity == Activity.this && ACTIVITY_COMPENSATED.equals(command)) {

					instance.removeActivityEventInterceptor(this);

					return true;
				}

				return false;
			}

		});

		// is it right that the target activity should be compensated as well? we
		// decided it is right.
		compensate(instance);

		if (compensateAndResume) {
			resume(instance);
		}

		// }else{
		// if(actList.size() == 0){
		// ComplexActivity parent = (ComplexActivity)getParentActivity();
		// while(parent!=null){
		// parent.setStatus(instance, Activity.STATUS_RUNNING);
		// parent = (ComplexActivity)parent.getParentActivity();
		// }
		//
		// setStatus(instance, Activity.STATUS_RUNNING);
		// }
		// }

		if (superCompensatingList != null)
			actList.addAll(0, superCompensatingList);

		fireEventToActivityFilters(instance, "returned", this);

		return actList;
		// Integer.parseInt("xxxx");
	}

	protected void firePropertyChangeEventToActivityFilters(ProcessInstance instance, String propertyName,
			Object changedValue) throws Exception {
		if (getProcessDefinition() == null)
			return;

		ActivityFilter[] activityFilters = getProcessDefinition().getActivityFilters();
		if (activityFilters != null)
			for (int i = 0; i < activityFilters.length; i++)
				if (activityFilters[i] != null)
					activityFilters[i].onPropertyChange(this, instance, propertyName, changedValue);
	}

	protected void fireEventToActivityFilters(ProcessInstance instance, String eventName, Object payload)
			throws Exception {
		if (getProcessDefinition() == null)
			return;

		ActivityFilter[] activityFilters = getProcessDefinition().getActivityFilters();
		if (activityFilters != null)
			for (int i = 0; i < activityFilters.length; i++)
				if (activityFilters[i] instanceof SensitiveActivityFilter)
					((SensitiveActivityFilter) activityFilters[i]).onEvent(this, instance, eventName, payload);

		int triggeringMethodMapped = getTriggeringMethodFromEventName(eventName);

		Vector messageListeners = instance.getMessageListeners("event");
		for (int i = 0; i < messageListeners.size(); i++) {
			MessageListener messageListener = (MessageListener) getProcessDefinition()
					.getActivity((String) messageListeners.get(i));
			if (messageListener instanceof ScopeActivity) {
				ScopeActivity scopeActivity = (ScopeActivity) messageListener;

				if (scopeActivity.isAncestorOf(this)) {
					EventMessagePayload emp = new EventMessagePayload();
					emp.setTriggerTracingTag(getTracingTag());

					scopeActivity.fireEventHandlers(instance, triggeringMethodMapped, emp);

				}
			}
		}

	}

	static HashMap triggerMethodMappingWithEventName;
	static {
		triggerMethodMappingWithEventName = new HashMap();
		triggerMethodMappingWithEventName.put("saveWorkitem",
				Integer.valueOf(EventHandler.TRIGGERING_BY_AFTER_CHILD_SAVED));
		triggerMethodMappingWithEventName.put("saveAnyway",
				Integer.valueOf(EventHandler.TRIGGERING_BY_AFTER_CHILD_SAVED_OR_COMPLETED));
	}

	private int getTriggeringMethodFromEventName(String eventName) {
		try {
			return ((Integer) triggerMethodMappingWithEventName.get(eventName)).intValue();
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean registerToProcessDefinition(boolean autoTagging, boolean checkCollision) {
		return getProcessDefinition().registerActivity(this, autoTagging, checkCollision);
	}

	public String toString() {
		return getName() + "(" + getTracingTag() + ")";
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Activity))
			return false;

		Activity comparatee = (Activity) obj;

		try {
			return getProcessDefinition().getId().equals(comparatee.getProcessDefinition().getId())
					&& getTracingTag().equals(comparatee.getTracingTag());
		} catch (Exception e) {
			return super.equals(obj);
		}
	}

	public boolean isSuccessorOf(Activity complexActivity) {
		Activity temp = this;
		while (temp.getParentActivity() != null) {
			temp = temp.getParentActivity();
			// 임시 로직: 대표님께 물어보기
			if (temp.equals(complexActivity))
				return true;
		}

		return false;
	}

	public boolean isAncestorOf(Activity activity) {
		return activity.isSuccessorOf(this);
	}

	public Transferable createTransferrable() {

		return new Transferable() {
			public DataFlavor[] getTransferDataFlavors() {
				return null;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return false;
			}

			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				return Activity.this;
			}
		};

	}

	public void executeTest(ProcessInstance instance, ProcessInstance testInstance) throws Exception {
		executeActivity(instance);
	}

	/*
	 * api for graph-based model
	 */
	@JsonIgnore
	private transient List<SequenceFlow> incomingSequenceFlows;
	@JsonIgnore
	private transient List<SequenceFlow> outgoingSequenceFlows;

	public List<SequenceFlow> getIncomingSequenceFlows() {
		if (incomingSequenceFlows == null) {
			incomingSequenceFlows = new ArrayList<SequenceFlow>();
		}
		return incomingSequenceFlows;
	}

	public void setIncomingSequenceFlows(List<SequenceFlow> incomingSequenceFlows) {
		this.incomingSequenceFlows = incomingSequenceFlows;
	}

	public List<SequenceFlow> getOutgoingSequenceFlows() {
		if (outgoingSequenceFlows == null) {
			outgoingSequenceFlows = new ArrayList<SequenceFlow>();
		}
		return outgoingSequenceFlows;
	}

	public void setOutgoingSequenceFlows(List<SequenceFlow> outgoingSequenceFlows) {
		this.outgoingSequenceFlows = outgoingSequenceFlows;
	}

	public void addIncomingTransition(SequenceFlow incomingSequenceFlow) {
		getIncomingSequenceFlows().add(incomingSequenceFlow);
	}

	public void addOutgoingTransition(SequenceFlow outgoingSequenceFlow) {
		getOutgoingSequenceFlows().add(outgoingSequenceFlow);
	}

	public List<Activity> getPossibleNextActivities(ProcessInstance instance, String scope) throws Exception {
		List<Activity> activities = new ArrayList<Activity>();

		// System.out.println("outgoingTransitions: " +
		// getOutgoingTransitions().size());
		boolean otherwiseFlag = false;
		Activity otherwiseActivity = null;
		for (Iterator<SequenceFlow> it = getOutgoingSequenceFlows().iterator(); it.hasNext();) {
			SequenceFlow ts = (SequenceFlow) it.next();
			if (ts.getCondition() != null) {
				Condition condition = ts.getCondition();
				if (condition.isMet(instance, scope)) {
					if (condition instanceof Or) {
						Condition[] condis = ((Or) condition).getConditions();
						if (condis.length > 0 && condis[0] instanceof Otherwise) {
							// 순서가 없다보니 Otherwise가 먼저와서 무조건 true 가 발생하는 경우가 생김
							// Otherwise가 먼저 올 경우는 일단 스킵했다가 다시 해준다.
							otherwiseFlag = true;
							otherwiseActivity = ts.getTargetActivity();
							continue;
						}
					}

					if (ts.getTargetActivity() != null)
						activities.add(ts.getTargetActivity());
				}
			} else {
				if (ts.getTargetActivity() != null)
					activities.add(ts.getTargetActivity());
			}
		}
		if (otherwiseFlag && activities.isEmpty()) {
			activities.add(otherwiseActivity);
		}
		return activities;
	}

	/**
	 * Check if this activity is triggered from event or not.
	 * This information is used normally for completing FlowActivity.
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean checkStartsWithBoundaryEventActivity() throws Exception {
		Set<Activity> checked = new HashSet<Activity>();
		return _checkStartsWithBoundaryEventActivity(checked);
	}

	private boolean _checkStartsWithBoundaryEventActivity(Set<Activity> checked) throws Exception {
		boolean check = false;
		for (Iterator<SequenceFlow> it = getIncomingSequenceFlows().iterator(); it.hasNext();) {
			SequenceFlow ts = (SequenceFlow) it.next();
			Activity beforeActivity = ts.getSourceActivity(); // may cause recursive loop
			if (checked.contains(beforeActivity))
				return false;

			checked.add(beforeActivity);

			if (beforeActivity instanceof Event && beforeActivity instanceof MessageListener
					&& ((Event) beforeActivity).getAttachedToRef() != null) {
				// if( "STOP_ACTIVITY".equals(((Event)beforeActivity).getActivityStop()) ){
				// return false;
				// }else{
				return true;
				// }
			} else if (beforeActivity == null) {
				return false;
			} else {
				check = beforeActivity._checkStartsWithBoundaryEventActivity(checked);
			}
		}
		return check;
	}

	public void setTokenCount(ProcessInstance instance, int tokenCount) throws Exception {
		instance.setProperty(getTracingTag(), "tokenCount", Integer.valueOf(tokenCount));
	}

	public int getTokenCount(ProcessInstance instance) throws Exception {
		Object objTokenCount = instance.getProperty(getTracingTag(), "tokenCount");

		if (objTokenCount == null)
			return 0;

		int tokenCount = Integer.parseInt(objTokenCount.toString());
		return tokenCount;
	}

	public ElementView createView() {
		ElementView elementView = (ElementView) UEngineUtil.getComponentByEscalation(getClass(), "view");

		elementView.setElement(this);

		return elementView;
	}

	transient boolean checked;

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	ElementView elementView;

	public ElementView getElementView() {
		return this.elementView;
	}

	public void setElementView(ElementView view) {
		this.elementView = view;
	}

	transient List<ActivityEventListener> activityEventListeners;

	public void addEventListener(final ActivityEventListener activityEventListener) {
		if (activityEventListeners == null) {
			activityEventListeners = new ArrayList<ActivityEventListener>();
		}

		activityEventListeners.add(activityEventListener);

		// ActivityFilter[] activityFilters =
		// getProcessDefinition().getActivityFilters();
		// activityFilters = (ActivityFilter[])
		// UEngineUtil.addArrayElement(activityFilters, new SensitiveActivityFilter() {
		// @Override
		// public void onEvent(Activity activity, ProcessInstance instance, String
		// eventName, Object payload) throws Exception {
		// if(Activity.this == activity){
		// activityEventListener.onEvent(activity, instance, eventName, payload);
		// }
		// }
		//
		// @Override
		// public void beforeExecute(Activity activity, ProcessInstance instance) throws
		// Exception {
		// if(Activity.this == activity){
		// activityEventListener.beforeExecute(activity, instance);
		// }
		//
		// }
		//
		// @Override
		// public void afterExecute(Activity activity, ProcessInstance instance) throws
		// Exception {
		// if(Activity.this == activity){
		// activityEventListener.afterExecute(activity, instance);
		// }
		// }
		//
		// @Override
		// public void afterComplete(Activity activity, ProcessInstance instance) throws
		// Exception {
		// if(Activity.this == activity){
		// activityEventListener.afterComplete(activity, instance);
		// }
		// }
		//
		// @Override
		// public void onPropertyChange(Activity activity, ProcessInstance instance,
		// String propertyName, Object changedValue) throws Exception {
		// if(Activity.this == activity){
		// activityEventListener.onPropertyChange(activity, instance, propertyName,
		// changedValue);
		// }
		//
		// }
		//
		// @Override
		// public void onDeploy(ProcessDefinition definition) throws Exception {
		// //not implemented
		// }
		// }, ActivityFilter.class);
		//
		// getProcessDefinition().setActivityFilters(activityFilters);

	}

	protected void fireActivityEventListeners(ProcessInstance instance, String eventName, Object payload)
			throws Exception {
		if (activityEventListeners != null)
			for (ActivityEventListener activityEventListener : activityEventListeners) {

				if ("beforeExecute".equals(eventName)) {
					activityEventListener.beforeExecute(this, instance);
				} else if ("afterExecute".equals(eventName)) {
					activityEventListener.afterExecute(this, instance);
				} else if ("afterComplete".equals(eventName)) {
					activityEventListener.afterComplete(this, instance);
				} else
					activityEventListener.onEvent(this, instance, eventName, payload);
			}
	}

	EventSynchronization eventSynchronization;

	public EventSynchronization getEventSynchronization() {
		return eventSynchronization;
	}

	public void setEventSynchronization(EventSynchronization eventSynchronization) {
		this.eventSynchronization = eventSynchronization;
	}
}
