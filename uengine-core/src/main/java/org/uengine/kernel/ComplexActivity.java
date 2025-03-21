package org.uengine.kernel;

import java.beans.PropertyChangeEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.uengine.kernel.bpmn.Event;
import org.uengine.kernel.bpmn.Pool;
import org.uengine.kernel.bpmn.ServiceTask;
import org.uengine.processmanager.ProcessManagerRemote;
import org.uengine.processmanager.ProcessTransactionContext;
import org.uengine.util.ActivityForLoop;

/**
 * @author Jinyoung Jang
 */

public class ComplexActivity extends DefaultActivity implements NeedArrangementToSerialize {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

	public static transient boolean USE_JMS = false; // To debug, you may set USE_JMS off
	public static transient boolean USE_THREAD = false;
	public transient static String ERRORCODE_COMPLEX_ACTIVITY_IS_NOT_NECESSARY = "CAINN";

	private static long ERROR_LEVEL_TIMEINMS = 20000;

	private List<Activity> childActivities;

	private List<Pool> pools;

	public List<Pool> getPools() {
		return pools;
	}

	public void setPools(List<Pool> pools) {
		this.pools = pools;
	}

	private Role[] roles;

	public Role[] getRoles() {
		return roles;
	}

	public void setRoles(Role[] roles) {
		this.roles = roles;
		firePropertyChangeEvent(new PropertyChangeEvent(this, "roles", roles, roles));
	}

	public Role getRole(String roleName) {

		if (roles != null)
			// TODO: use hashtable
			for (int i = 0; i < roles.length; i++) {
				if (roles[i] != null && roles[i].getName() != null && roles[i].getName().equalsIgnoreCase(roleName))
					return roles[i];
			}
		return null;
	}

	public boolean isOpenTo(String roleName) {
		return true;
	}

	public void addRole(Role role) {
		if (roles == null)
			setRoles(new Role[] {});

		Role[] newRoles = new Role[roles.length + 1];

		if (roles != null) {
			// TODO: use hashtable
			for (int i = 0; i < roles.length; i++) {
				if (roles[i].getName().equals(role.getName())) {
					roles[i] = role;
					return;
				}
			}
		}

		System.arraycopy(roles, 0, newRoles, 0, roles.length);

		newRoles[roles.length] = role;
		setRoles(newRoles);
	}

	// ProcessVariable[] processVariableDescriptors;
	//
	// Hashtable processVariableDescriptorsHT;
	// public ProcessVariable[] getProcessVariables(){
	// return processVariableDescriptors;
	// }
	// public ProcessVariable getProcessVariable(String pvName){
	// if(processVariableDescriptorsHT!=null &&
	// processVariableDescriptorsHT.containsKey(pvName))
	// return (ProcessVariable)processVariableDescriptorsHT.get(pvName);
	// else
	// return null;
	// }
	// public void setProcessVariables(ProcessVariable[] pvds){
	// this.processVariableDescriptors = pvds;
	// processVariableDescriptorsHT = new Hashtable();
	//
	// for(int i=0; i<processVariableDescriptors.length; i++)
	// processVariableDescriptorsHT.put(
	// processVariableDescriptors[i].getName(),
	// processVariableDescriptors[i]
	// );
	//
	// firePropertyChangeEvent(new PropertyChangeEvent(this, "processVariables",
	// pvds, pvds));
	// }

	public ComplexActivity() {
		super("seq");

		// childActivities = new ActivityRepository(this);
		// 2014.02.06 hyungkook childActivities = new ActivityRepository(this); 이렇게 설정시에
		// dwr collectionConverter 변환 문제
		childActivities = new ArrayList<Activity>();
		pools = new ArrayList<Pool>();
	}

	public List<Activity> getChildActivities() {
		return childActivities;
	}

	public void setChildActivities(List<Activity> value) {
		childActivities = value;
	}

	public synchronized void setChildActivities(Activity[] childActivities) {
		setChildActivities(childActivities, true);
	}

	public synchronized void addChildActivity(Activity child) {
		addChildActivity(child, true);
	}

	public synchronized void removeChildActivity(Activity child) {
		this.childActivities.remove(child);
	}

	public synchronized void addPool(Pool pool) {
		this.pools.add(pool);
	}

	public synchronized void setChildActivities(Activity[] childActivities, boolean autoTagging) {
		getChildActivities().clear();

		for (int i = 0; i < childActivities.length; i++) {
			addChildActivity(childActivities[i], autoTagging);
		}
	}

	public synchronized void addChildActivity(Activity child, boolean autoTagging) {
		if (autoTagging)
			autoTag(child);

		this.childActivities.add(child);

		// TODO not sure
		child.setParentActivity(this);

		if (getProcessDefinition() != null)
			getProcessDefinition().registerActivity(child);
	}

	public ProcessInstance createInstance(ProcessInstance instanceInCreating) throws Exception {

		super.createInstance(instanceInCreating);

		// Sets current running state
		setCurrentStep(instanceInCreating, 0);

		// Lets each child activity initialize process instance
		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			child.createInstance(instanceInCreating);
		}

		return instanceInCreating;
	}

	public int getCurrentStep(ProcessInstance instance) throws Exception {
		Object objCurrentStep = instance.getProperty(getTracingTag(), "currentStep");

		if (objCurrentStep == null)
			return 0;

		int currStep = Integer.parseInt(objCurrentStep.toString());
		return currStep;
	}

	public void setCurrentStep(ProcessInstance instance, int currStep) throws Exception {
		instance.setProperty(getTracingTag(), "currentStep", Integer.valueOf(currStep));
	}

	protected void onEvent(String command, ProcessInstance instance, Object payload) throws Exception {
		// System.out.println("ComplexActivity::onEvent.command = "+command);

		// dispatching leaf childs
		if (command.equals(CHILD_DONE)) {
			String myStatus = getStatus(instance);

			// if(!myStatus.equals(Activity.STATUS_RUNNING)){
			// throw new UEngineException("Though the complex activity is not running
			// status, an child activity tried to complete");
			// }

			int currStep = getCurrentStep(instance);
			if (currStep == -1) {
				try {
					List executedActs = instance.getProcessTransactionContext()
							.getExecutedActivityInstanceContextsInTransaction();
					if (((ActivityInstanceContext) executedActs.get(executedActs.size() - 1))
							.getActivity() instanceof BackActivity) {
						return; // skips the chaning of sequence flow since it seems to be compensated by
								// BackActivity.
					}
				} catch (Exception e) {
				}

				throw new UEngineException("This complex activity is never initialized. At " + instance.getInstanceId()
						+ "[" + getTracingTag() + "]");
			}

			// ignore illegal request
			Activity childActivity = (Activity) payload;
			if (currStep != getChildActivities().indexOf(childActivity)) {
				/* throw new UEngineException */System.out.println(
						"[Inconsistence Status] Completed activity is not the correct step with parent activity's status.");
				currStep = getChildActivities().indexOf(childActivity);
			}

			currStep++;
			setCurrentStep(instance, currStep);
			// System.out.println("--------------------------"+currStep);

			if (!instance.isSuspended(getTracingTag())) {
				ExecutionScopeContext executionScopeContext = instance.getExecutionScopeContext();
				instance.setExecutionScope(
						instance.getParentExecutionScopeOf(executionScopeContext.getExecutionScope()));

				executeActivity(instance); // execute next in the parent scope context.

				instance.setExecutionScope(executionScopeContext.getExecutionScope());

			} else {
				System.out.println("this step is suspended:" + currStep);
			}
		} else if (command.equals(CHILD_COMPENSATED)) {
			Activity childActivity = (Activity) payload;
			int activityOrder = getChildActivities().indexOf(childActivity);
			if (activityOrder == -1)
				throw new UEngineException(
						"Compensating activity is not a child of the parent activity. Some inconsistence status.");

			int currStep = getCurrentStep(instance);
			if (activityOrder != currStep) {
				System.out.println(
						"[Inconsistence Status] Compensating activity is not in the correct order in current step of the parent activity. uEngine corrected this problem by setting the step value implictly.");
				currStep = activityOrder;
			}

			currStep--;
			if (currStep >= 0) {
				setCurrentStep(instance, currStep);
				Activity activityInTheBackOrder = getChildActivities().get(currStep);

				activityInTheBackOrder.compensateOneStep(instance);
				// childActivity.suspend(instance);
			} else {
				super.compensate(instance);
			}
		} else if (command.equals(CHILD_SKIPPED)) {
			int currStep = getCurrentStep(instance);
			currStep++;
			if (currStep < getChildActivities().size()) {
				setCurrentStep(instance, currStep);
				Activity childActivity = getChildActivities().get(currStep);
				childActivity.reset(instance);
				childActivity.suspend(instance);
			} else {
				super.skip(instance);
			}
		} else if (command.equals(CHILD_RESUMED)) {
			// mark the status as running since its child activity became running.
			ComplexActivity parentActivity = (ComplexActivity) this;
			do {
				parentActivity.setStatus(instance, Activity.STATUS_RUNNING);
				parentActivity = (ComplexActivity) parentActivity.getParentActivity();
			} while (parentActivity != null);
			//

			Activity childActivity = (Activity) payload;
			int activityOrder = getChildActivities().indexOf(childActivity);
			if (activityOrder == -1)
				throw new UEngineException(
						"Resuming activity is not a child of the parent activity. Some inconsistence status.");

			int currStep = getCurrentStep(instance);
			if (activityOrder != currStep) {
				System.out.println(
						"[Inconsistence Status] Resuming activity is not a child of the parent activity. Some inconsistence status. uEngine corrected this problem by setting the step value implictly.");
				setCurrentStep(instance, activityOrder);
			}

			executeActivity(instance); // resume flow control from the suspended step
			super.onEvent(command, instance, payload);// replicate this msg to the super class
		} else if (command.equals(CHILD_STOPPED)) {
			instance.setStatus(getTracingTag(), Activity.STATUS_STOPPED);
			instance.setStatus("", Activity.STATUS_STOPPED);
			// this will let the process stops and don't report stop message any more upper.
		} else if (command.equals(CHILD_FAULT)) {
			fireFault(instance, ((FaultContext) payload));
		} else if (command.equals(ACTIVITY_DONE)) {

			/**
			 * propagate finish event to its childs;
			 * This job should be prior than 'super.onEvent()' since sometimes (in case of
			 * 'Loop') the child's status can be updated by super.onEvent()
			 */
			for (int i = 0; i < getChildActivities().size(); i++) {
				Activity activity = getChildActivities().get(i);
				if (instance.isRunning(activity.getTracingTag()))
					instance.setStatus(activity.getTracingTag(), Activity.STATUS_COMPLETED);
			}

			super.onEvent(command, instance, payload);
		} else
			super.onEvent(command, instance, payload);
	}

	protected void executeActivity(ProcessInstance instance) throws Exception {
		int currStep = getCurrentStep(instance);
		if (currStep >= childActivities.size()) {
			fireComplete(instance);
			return;
		}

		Activity childActivity = getChildActivities().get(currStep);
		// if(!childActivity.isBackwardActivity()){
		queueActivity(childActivity, instance);
		// }else{//if the activity is a backward activity, which is for compensating and
		// // only need to be executed in compensation process, skip running the
		// activity.
		// currStep++;
		// setCurrentStep(instance, currStep);
		// executeActivity(instance);
		// }
	}

	// TODO: hotspot. replicate the serialization event

	public void beforeSerialization() {

		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			if (child instanceof NeedArrangementToSerialize)
				((NeedArrangementToSerialize) child).beforeSerialization();
		}

		// Vector pureVector = new Vector();
		// pureVector.addAll(childActivities);
		// childActivities = pureVector;
	}

	public void afterDeserialization() {
		// ProcessVariable [] processVariables = getProcessVariables();
		//
		// if(processVariables!=null)
		// for(int i=0; i<processVariables.length; i++){
		// processVariables[i].afterDeserialization();
		// }
		// TODO: This conversion job is only needed in design time. Remove these codes
		// someday for performace enhancement.
		// if(!(childActivities instanceof ActivityRepository)){
		// ActivityRepository actRepository = new ActivityRepository(this);
		// actRepository.addAll(childActivities);
		// childActivities = actRepository;
		// }

		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			child.setParentActivity(this);

			if (child instanceof NeedArrangementToSerialize)
				((NeedArrangementToSerialize) child).afterDeserialization();
		}
	}

	// end

	protected void queueActivity(final Activity act, ProcessInstance instance) throws Exception {

		try {
			final String instanceId = instance.getInstanceId();

			long timeInMillis_start = System.currentTimeMillis();

			System.out.println(
					"- [uEngine] Start Executing Activity: " + act.getName() + " (" + act.getTracingTag() + ")");

			if (act instanceof Event) {
				if (((Event) act).getAttachedToRef() == null) {
					instance.execute(act.getTracingTag());
				}
			} else {
				instance.execute(act.getTracingTag());
			}

			long elapsedTime = (System.currentTimeMillis() - timeInMillis_start);

			PrintStream logWriter = (elapsedTime < ERROR_LEVEL_TIMEINMS ? System.out : System.err);

			logWriter.println("- [uEngine] End Executing Activity: " + act.getName() + " (" + act.getTracingTag()
					+ ") - Elapsed Time : " + elapsedTime);

		} catch (Exception e) {

			// if (act instanceof ServiceTask) {
			// act.getIncomingSequenceFlows().get(0).getSourceActivity().backToHere(instance);
			// } else {
			act.fireFault(instance, e);
			if (instance.getProcessTransactionContext().getSharedContext("faultTolerant") == null) {

				throw e;

			}
			// }
		} finally {
			if (act.isFaultTolerant()) {
				onEvent(CHILD_DONE, instance, act);
			}
		}

	}

	public ValidationContext validate(Map options) {
		ValidationContext valCtx = super.validate(options);
		if (getChildActivities() != null && getChildActivities().size() > 0) {
			// if(getChildActivities().size()==1){
			// if(!(this instanceof ProcessDefinition || this instanceof LoopActivity)){
			// valCtx.addWarningWithCode(getActivityLabel() + " only one child activity:
			// This complex activity is not necessary.",
			// ERRORCODE_COMPLEX_ACTIVITY_IS_NOT_NECESSARY);
			// }
			// }

			if (options == null
					|| (options != null && !options.containsKey(ValidationContext.OPTIONKEY_DISABLE_REPLICATION))) {
				List<Activity> childActivities = getChildActivities();
				for (Activity child : childActivities) {
					ValidationContext vc = child.validate(options);
					valCtx.addAll(vc);

					if (vc.size() > 0) {
						child.setIntegrity(1);
					} else {
						child.setIntegrity(0);
					}
				}
			}
		} else {
			valCtx.addWarning(getActivityLabel() + " child activity is empty.");
		}

		return valCtx;
	}

	public void skip(ProcessInstance instance) throws Exception {
		int currStep = getCurrentStep(instance);
		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			child.skip(instance);
		}
		// super.skip(instance);//the last child will invoke this
	}

	/**
	 * Compensate all the child activity, which is running.
	 * It would work by clicking the back button of the complex block.
	 */
	public void compensate(ProcessInstance instance) throws Exception {
		// System.out.println("Rolling back activity: " + getTracingTag());

		// int currStep = getCurrentStep(instance);
		List<Activity> childActivities = getChildActivities();

		// Actually complex activity (basically sequence activity's behavior) only need
		// to compensate from the first child to the currently running child,
		// In the other hand, other complex activities like SwitchActivity or
		// AllActivity should check their child activities entirely and compensate only
		// the child in running (compensatable) state.
		boolean neverAffected = true;
		int compensatedCnt = 0;
		if (childActivities.size() > 0) {
			for (int i = /* currStep */ childActivities.size() - 1; i >= 0; i--) {
				Activity child = childActivities.get(i);
				if (Activity.isCompensatable(child.getStatus(instance))) {
					child.compensate(instance);
					neverAffected = false;
					compensatedCnt++;
				}
			}
		}

		// the child (which is compensated in the lastest order) whould compensate this
		// (parent)activity by event escalation (CHILD_COMPENSATED).
		// So we don't need to call the 'compensate' method for it-self.
		if (neverAffected && Activity.isCompensatable(getStatus(instance)))
			super.compensate(instance);

		// for (Activity childActivity : getProcessDefinition().getChildActivities()) {
		// if (childActivity instanceof Event) {
		// Event event = (Event) childActivity;
		// if (this.getTracingTag().equals(event.getAttachedToRef())) {
		// // instance.execute(event.getTracingTag());
		// instance.setStatus(event.getTracingTag(), STATUS_READY);
		// }
		// }
		// }

		/**
		 * clear the status
		 */
		if (this instanceof ProcessDefinition && instance.getStatus().equals(Activity.STATUS_COMPLETED)) {
			setCurrentStep(instance, childActivities.size() - compensatedCnt);
		} else {
			setCurrentStep(instance, 0);
		}
	}

	/**
	 * Compensate for just one step.
	 * If this is called in a complex activity, the child positioned in the last
	 * would be compensated.
	 */
	public void compensateOneStep(ProcessInstance instance) throws Exception {
		int currStep = getCurrentStep(instance);

		if (currStep > 0) {
			if (currStep >= childActivities.size()) // make it bounds
				currStep = childActivities.size() - 1;
			// currStep --;

			// Sets current running state
			setCurrentStep(instance, currStep);
			Activity childAtCurrStep = (Activity) childActivities.get(currStep);
			childAtCurrStep.compensateOneStep(instance);
			currStep = getCurrentStep(instance);
		}

		if (currStep == -1) {
			// super.compensate(instance);
			// TODO should be encapsulated with super class' method.
			reset(instance);
			fireCompensate(instance);
		} else {
			setStatus(instance, Activity.STATUS_RUNNING);
		}
	}

	/*
	 * public void compensateOneStep(ActivityInstance instance) throws Exception{
	 * int currStep = Integer.parseInt((instance.get(getTracingTag(),
	 * "currentStep")).toString());
	 * Activity compensatingChild =
	 * (Activity)getChildActivities().elementAt(currStep);
	 * 
	 * compensatingChild.compensate(instance);
	 * 
	 * currStep--;
	 * setCurrentStep(instance, currStep);
	 * }
	 * 
	 * public void skipOneStep(ActivityInstance instance) throws Exception{
	 * int currStep = getCurrentStep(instance);
	 * Activity skippingChild = (Activity)getChildActivities().elementAt(currStep);
	 * 
	 * skippingChild.skip(instance);
	 * 
	 * currStep++;
	 * setCurrentStep(instance, currStep);
	 * }
	 */
	public void reset(ProcessInstance instance) throws Exception {
		setCurrentStep(instance, 0);

		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			child.reset(instance);
		}

		super.reset(instance);
	}

	public void suspend(ProcessInstance instance) throws Exception {
		int currStep = getCurrentStep(instance);
		if (currStep >= getChildActivities().size()) {
			currStep = getChildActivities().size() - 1;
		}

		Activity child = getChildActivities().get(currStep);
		child.suspend(instance);
	}

	public void stop(ProcessInstance instance) throws Exception {
		stop(instance, Activity.STATUS_STOPPED);
	}

	public void stop(ProcessInstance instance, String status) throws Exception {
		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			if (isStoppable(instance.getStatus(child.getTracingTag()))) {
				if (child.getParentActivity() != null && !isStoppable(child.getParentActivity().getStatus(instance)))
					System.out.println("Illegal status");

				if (child instanceof ComplexActivity)
					child.stop(instance, status);
				else
					child.stop(instance);
			}
		}

		super.stop(instance, status);
	}

	protected void onChanged(ProcessInstance instance) throws Exception {
		// TODO: [bug should be fixed someday: when I uncomment following line, jboss
		// hangs up when try to read the value]
		// int currStep = getCurrentStep(instance);
		for (int i = 0; i < getChildActivities().size(); i++) {
			Activity child = getChildActivities().get(i);
			child.fireChanged(instance);

			// TODO: [performance] [dynamic Change] [disabled]
			/*
			 * if(instance.isRunning(this.getTracingTag()) && i == currStep &&
			 * child.getStatus(instance).equals(Activity.STATUS_READY)){
			 * queueActivity(child, instance);
			 * }
			 */
		}
	}

	public Activity findChildActivity(final Class type) {
		ActivityForLoop findingLoop = new ActivityForLoop() {
			public void logic(Activity activity) {
				if (type.isAssignableFrom(activity.getClass())) {
					((ActivityForLoop) this).stop(activity);
				}
			}
		};

		findingLoop.run(this);
		Activity findingActivity = (Activity) findingLoop.getReturnValue();

		return findingActivity;
	}

	public Vector getPreviousActivitiesOf(Activity child) {
		int where = childActivities.indexOf(child);
		where--;

		if (where < 0) {
			Vector acts = getPreviousActivities();
			if (acts != null && acts.size() > 0) {
				Vector lastActs = new Vector();

				for (int i = 0; i < acts.size(); i++) {
					Activity act = (Activity) acts.elementAt(i);

					if (act instanceof ComplexActivity) {
						Vector lastActsOfLastAct = ((ComplexActivity) act).getLastActivities();
						if (lastActsOfLastAct != null)
							lastActs.addAll(lastActsOfLastAct);
					} else {
						lastActs.add(act);
					}
				}

				return lastActs;
			} else
				return null;

		} else {
			Activity act = getChildActivities().get(where);

			if (act instanceof ComplexActivity) {
				return ((ComplexActivity) act).getLastActivities();
			} else {
				Vector vt = new Vector();
				vt.add(act);

				return vt;
			}

		}
	}

	public Vector getLastActivities() {
		if (childActivities.size() == 0)
			return null;

		Activity act = (Activity) getChildActivities().get(childActivities.size() - 1);
		if (act instanceof ComplexActivity)
			return ((ComplexActivity) act).getLastActivities();
		else {
			Vector vt = new Vector();
			vt.add(act);
			return vt;
		}
	}

	protected void gatherPropagatedActivitiesOf(ProcessInstance instance, Activity child, List list) throws Exception {
		int where = childActivities.indexOf(child);

		for (int i = where + 1; i < childActivities.size(); i++) {
			Activity theChild = (Activity) childActivities.get(i);

			if (theChild.getStatus(instance).equals(Activity.STATUS_READY)) {
				break; // return; //The old version simply return to get out of this method since we've
						// thought the following activities, where after occurrent of any of 'ready'
						// activity, don't need to be compensated.
			}

			list.add(theChild);
		}

		gatherPropagatedActivities(instance, list);

	}

	public boolean registerToProcessDefinition(boolean autoTagging, boolean checkCollision) {
		boolean ok = super.registerToProcessDefinition(autoTagging, checkCollision);

		if (checkCollision && !ok)
			return false;

		int i = 0;
		List<Activity> childActivities = getChildActivities();
		for (Activity child : childActivities) {
			child.setParentActivity(this);
			ok = child.registerToProcessDefinition(autoTagging, checkCollision);
			if (checkCollision && !ok)
				return false;
		}

		return true;
	}

	protected boolean compensateChild(ProcessInstance instance, Activity child) throws Exception {
		// this would be invoked only by parallel activity types such as Switch or All.
		List<Activity> childActivities = getChildActivities();
		if (!childActivities.contains(child))
			throw new UEngineException("Illegal compensation request. The activity [" + child
					+ "] is not a child of complex activity [" + this + "].");

		String status = child.getStatus(instance);

		if (Activity.isCompensatable(status) || status.equals(Activity.STATUS_COMPLETED)
				|| status.equals(Activity.STATUS_SKIPPED)) {
			child.compensateOneStep(instance);

			if (STATUS_COMPLETED.equals(getStatus(instance)))
				fireResume(instance);

			return true;
		}

		return false;
	}

	public ActivityReference getInitiatorHumanActivityReference(final ProcessTransactionContext ptc) {

		// if(!isInitiateByFirstWorkitem())
		// throw new RuntimeException("this process definition is not allowed to be
		// initiated by the first workitem.");

		ActivityForLoop findingLoop = new ActivityForLoop() {
			public void logic(Activity activity) {
				if (activity instanceof HumanActivity) {
					stop(activity);
				} else if (activity instanceof SubProcessActivity) {
					stop(null);
					if (GlobalContext.isDesignTime()) {
						return;
					}

					SubProcessActivity spAct = (SubProcessActivity) activity;

					// ProcessManagerRemote pm = null;
					// try{
					// if(ptc.getProcessManager()!=null)
					// pm = ptc.getProcessManager();
					// else
					// pm = (new ProcessManagerFactoryBean()).getProcessManagerForReadOnly();

					// ProcessDefinition spDef =
					// ProcessDefinitionFactory.getInstance(ptc).getDefinition(spAct.getDefinitionId());

					// if(spDef.isInitiateByFirstWorkitem()){
					// ActivityReference actRefReturnedFromSP =
					// spDef.getInitiatorHumanActivityReference(ptc);

					// if(actRefReturnedFromSP==null){
					// stop(null);
					// return;
					// }

					// String scopeOfActFromSP = actRefReturnedFromSP.getAbsoluteTracingTag();

					// if(scopeOfActFromSP == null){
					// if(actRefReturnedFromSP.getActivity()!=null){
					// actRefReturnedFromSP.setAbsoluteTracingTag(spAct.getTracingTag() + "@" +
					// actRefReturnedFromSP.getActivity().getTracingTag());
					// stop(actRefReturnedFromSP);
					// return;
					// }

					// stop(null);
					// return;
					// }

					// actRefReturnedFromSP.setAbsoluteTracingTag(spAct.getTracingTag() + "@" +
					// scopeOfActFromSP);

					// stop(actRefReturnedFromSP);
					// }
					// }catch(Exception e){
					// throw new RuntimeException(e);
					// }finally{
					// if(ptc!=null && ptc.getProcessManager()==null)
					// try {
					// pm.remove();
					// } catch (Exception e) {
					// }
					// }
				}
			}
		};

		findingLoop.run(this);

		Object result = findingLoop.getReturnValue();
		if (result instanceof HumanActivity) {
			ActivityReference ref = new ActivityReference();
			ref.setActivity((Activity) result);
			ref.setAbsoluteTracingTag(((Activity) result).getTracingTag());

			return ref;
		} else {
			return (ActivityReference) result;
		}
	}

	public void usabilityCheck(Map values) {
		super.usabilityCheck(values);

		if (getChildActivities() != null && getChildActivities().size() > 0) {
			List<Activity> childActivities = getChildActivities();
			for (Activity child : childActivities) {
				child.usabilityCheck(values);
			}
		}
	}

}
