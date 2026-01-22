package org.uengine.five.overriding;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.businessrule.BusinessRuleEvaluator;
import org.uengine.five.businessrule.BusinessRuleStore;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityFilter;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.bpmn.BusinessRuleTask;

/**
 * Evaluates {@link BusinessRuleTask} automatically at runtime.
 *
 * <p>
 * BusinessRuleTask is a {@link org.uengine.kernel.ReceiveActivity} (waits for
 * external payload).
 * This filter evaluates the DMN in process-service layer and completes the task
 * immediately.
 * </p>
 */
public class BusinessRuleTaskAutoEvaluatorFilter implements ActivityFilter {

    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    @Autowired
    BusinessRuleStore businessRuleStore;

    @Autowired
    BusinessRuleEvaluator businessRuleEvaluator;

    @Override
    public void beforeExecute(Activity activity, ProcessInstance instance) throws Exception {
    }

    @Override
    public void afterExecute(Activity activity, ProcessInstance instance) throws Exception {
        if (!(activity instanceof BusinessRuleTask)) {
            return;
        }

        BusinessRuleTask task = (BusinessRuleTask) activity;
        String ruleId = task.getBusinessRuleId();
        if (ruleId == null) {
            // If there is no rule id, we can't evaluate.
            return;
        }

        // 1) Build DMN inputs from mapping elements
        Map<String, Object> inputs = task.buildRuleInputs(instance);
        // 입력이 하나도 구성되지 않으면 DMN 평가를 시도하지 않는다.
        if (inputs.isEmpty()) {
            return;
        }

        // 2) Evaluate DMN (ruleJson.dmnXml or ruleJson.rules)
        BusinessRuleStore.BusinessRuleFile file = businessRuleStore.loadOrThrow(ruleId);
        Map<String, Object> outputs = businessRuleEvaluator.evaluate(file.getRuleJson(), inputs);

        // 3) Translate outcome to match BPMN gateway conditions (approve/reject ->
        // 승인/거절)
        Object outcome = outputs.get("outcome");
        if (outcome != null) {
            String s = String.valueOf(outcome);
            if ("approve".equalsIgnoreCase(s)) {
                outputs.put("outcome", "승인");
            } else if ("reject".equalsIgnoreCase(s)) {
                outputs.put("outcome", "거절");
            }
        }

        // 4) Apply outputs to process variables based on mapping
        boolean applied = task.applyRuleOutputs(instance, outputs);

        // Fallback: if no mapping found, try the conventional variable name.
        if (!applied && outputs.get("outcome") != null) {
            try {
                instance.setBeanProperty("평가결과", (Serializable) String.valueOf(outputs.get("outcome")));
            } catch (Exception ignore) {
            }
        }

        // 5) Complete this ReceiveActivity-like task without waiting external message
        try {
            instance.removeMessageListener(task.getMessage(), task.getTracingTag());
        } catch (Exception ignore) {
        }
        task.fireComplete(instance);
    }

    @Override
    public void afterComplete(Activity activity, ProcessInstance instance) throws Exception {
    }

    @Override
    public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue)
            throws Exception {
    }

    @Override
    public void onDeploy(ProcessDefinition definition) throws Exception {
    }
}
