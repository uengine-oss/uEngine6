package org.uengine.kernel;

/**
 * Called after a transaction successfully commits when an activity is completed.
 * Implementations should be lightweight and ideally non-blocking.
 */
public interface IActivityCompletionListener {

    void onActivityCompleted(ProcessInstance instance, Activity activity) throws Exception;

}

