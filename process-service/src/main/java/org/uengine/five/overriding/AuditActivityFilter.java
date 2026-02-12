package org.uengine.five.overriding;

import org.uengine.five.audit.AuditEvent;
import org.uengine.five.audit.AuditEventType;
import org.uengine.five.audit.AuditService;
import org.uengine.kernel.Activity;
import org.uengine.kernel.FaultContext;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ScriptActivity;
import org.uengine.kernel.SensitiveActivityFilter;
import org.uengine.kernel.bpmn.ServiceTask;

/**
 * 액티비티 실행 감사 로그. 확장된 AuditService 패턴으로 기록 (저장소: jpa/file/composite 설정 가능).
 */
public class AuditActivityFilter implements SensitiveActivityFilter {

    private static Long parseIdOrNull(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void beforeExecute(Activity activity, ProcessInstance instance) throws Exception { }

    @Override
    public void afterExecute(Activity activity, ProcessInstance instance) throws Exception {
        if (instance.isSimulation()) return;

        if (activity instanceof HumanActivity || activity instanceof ServiceTask || activity instanceof ScriptActivity) {
            Long rootInstId = parseIdOrNull(instance.getRootProcessInstanceId());
            Long instId = parseIdOrNull(instance.getInstanceId());

            AuditService auditService = GlobalContext.getComponent(AuditService.class);
            if (auditService == null) return;

            AuditEvent event = AuditEvent.of(AuditEventType.ACTIVITY_EXECUTION, rootInstId, instId)
                    .withTracingTag(activity.getTracingTag())
                    .withActor(GlobalContext.getUserId())
                    .withPayload("activityName", activity.getName())
                    .withPayload("fullTracingTag", activity.getTracingTag());
            auditService.record(event);
        }
    }

    @Override
    public void afterComplete(Activity activity, ProcessInstance instance) throws Exception { }

    @Override
    public void afterFault(Activity activity, ProcessInstance instance, FaultContext faultContext) throws Exception { }

    @Override
    public void onPropertyChange(Activity activity, ProcessInstance instance, String propertyName, Object changedValue) throws Exception { }

    @Override
    public void onDeploy(ProcessDefinition definition) throws Exception { }

    @Override
    public void onEvent(Activity activity, ProcessInstance instance, String eventName, Object payload) throws Exception {
    }
}
