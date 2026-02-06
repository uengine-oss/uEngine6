package org.uengine.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.uengine.kernel.bpmn.FlowActivity;
import org.uengine.kernel.bpmn.Gateway;
import org.uengine.kernel.bpmn.SequenceFlow;

/**
 * SKIP 가능 여부를 "정의 그래프 + 변수 사용" 기준으로 정적으로 분석한다.
 *
 * 분석 개요:
 * - 현재 태스크(ReceiveActivity)가 out / in-out 으로 "쓰는" 프로세스 변수를 수집
 * - 현재 태스크 이후로 도달 가능한 모든 노드(Activity/Gateway/Event)를 그래프 탐색(BFS)으로 수집
 * - 이후 노드에서 해당 변수를
 *   - 입력 매핑(in / in-out)으로 사용하거나
 *   - outgoing SequenceFlow 조건에서 참조하면
 *   => SKIP 불가로 판단(보수적)
 */
public final class TaskSkipAnalyzer {

    private TaskSkipAnalyzer() {
    }

    @FunctionalInterface
    public interface DefinitionResolver {
        /**
         * 외부 CallActivity/SubProcessActivity가 참조하는 정의를 로딩한다.
         * @param definitionId 예: "서브프로세스 활용/고장신고서브프로세스_sub.bpmn"
         * @param version 선택 버전(없으면 null)
         */
        ProcessDefinition resolve(String definitionId, String version) throws Exception;
    }

    public static class SkipVarReference {
        public String varName;
        public String whereTracingTag;
        public String whereType;
    }

    public static SkipVarReference findFirstBlockingReference(ProcessInstance instance, Activity currentActivity) throws Exception {
        return findFirstBlockingReference(instance, currentActivity, null);
    }

    /**
     * @param resolver CallActivity의 definitionId를 따라가며 추가 정의까지 분석하고 싶을 때 제공
     */
    public static SkipVarReference findFirstBlockingReference(ProcessInstance instance, Activity currentActivity, DefinitionResolver resolver) throws Exception {
        if (instance == null || currentActivity == null) return null;
        if (!(currentActivity instanceof ReceiveActivity)) return null;

        ReceiveActivity current = (ReceiveActivity) currentActivity;
        ProcessDefinition def = instance.getProcessDefinition();
        Set<String> writtenVars = collectWrittenProcessVariableNames(def, current);
        if (writtenVars.isEmpty()) return null;

        List<Activity> reachable = collectReachableActivities(currentActivity);
        if (reachable.isEmpty()) return null;

        return findFirstVariableReferenceInFuture(def, reachable, writtenVars, resolver, new HashSet<>());
    }

    /** current 이후 도달 가능한 노드(Activity/Gateway/Event) 수집 */
    public static List<Activity> collectReachableActivities(Activity start) {
        List<Activity> res = new ArrayList<>();
        if (start == null) return res;

        Set<String> visited = new HashSet<>();
        LinkedList<Activity> q = new LinkedList<>();

        // start가 서브프로세스(ScopeActivity) 내부에 있는 경우,
        // 해당 스코프가 "완료된 이후" 바깥으로 이어지는 경로도 이후에 발생할 수 있으므로 보수적으로 포함한다.
        // (정적 분석 특성상 runtime 경로를 완전히 알 수 없으므로 over-approximation)
        for (ScopeActivity scope = findEnclosingScope(start); scope != null; scope = findEnclosingScope(scope)) {
            enqueueOutgoingTargets(scope, visited, q, res);
        }

        if (start.getOutgoingSequenceFlows() != null) {
            for (SequenceFlow f : start.getOutgoingSequenceFlows()) {
                if (f == null) continue;
                Activity t = f.getTargetActivity();
                if (t == null) continue;
                String tt = t.getTracingTag();
                if (tt != null && visited.add(tt)) {
                    q.add(t);
                    res.add(t);
                }
            }
        }

        while (!q.isEmpty()) {
            Activity cur = q.removeFirst();
            if (cur == null) continue;

            // 서브프로세스(스코프) 자체를 만났으면 내부(start activities)까지 확장 탐색
            if (cur instanceof ScopeActivity) {
                ScopeActivity scope = (ScopeActivity) cur;
                try {
                    List<Activity> starts = scope.getStartActivities();
                    if (starts != null) {
                        for (Activity s : starts) {
                            enqueue(s, visited, q, res);
                        }
                    }
                } catch (Exception ignore) {
                }
            }

            List<SequenceFlow> outs = cur.getOutgoingSequenceFlows();
            if (outs == null) continue;
            for (SequenceFlow f : outs) {
                if (f == null) continue;
                Activity t = f.getTargetActivity();
                if (t == null) continue;
                enqueue(t, visited, q, res);
            }
        }
        return res;
    }

    private static ScopeActivity findEnclosingScope(Activity activity) {
        if (activity == null) return null;
        Activity parent = activity.getParentActivity();
        while (parent != null) {
            if (parent instanceof ScopeActivity) {
                return (ScopeActivity) parent;
            }
            parent = parent.getParentActivity();
        }
        return null;
    }

    private static void enqueueOutgoingTargets(Activity from, Set<String> visited, LinkedList<Activity> q, List<Activity> res) {
        if (from == null) return;
        List<SequenceFlow> outs = from.getOutgoingSequenceFlows();
        if (outs == null) return;
        for (SequenceFlow f : outs) {
            if (f == null) continue;
            Activity t = f.getTargetActivity();
            enqueue(t, visited, q, res);
        }
    }

    private static void enqueue(Activity t, Set<String> visited, LinkedList<Activity> q, List<Activity> res) {
        if (t == null) return;
        String tt = t.getTracingTag();
        if (tt == null) return;
        if (visited.add(tt)) {
            q.add(t);
            res.add(t);
        }
    }

    private static Set<String> collectWrittenProcessVariableNames(ProcessDefinition def, ReceiveActivity activity) throws Exception {
        Set<String> vars = new LinkedHashSet<>();
        if (activity == null || def == null) return vars;

        ParameterContext[] params = activity.getParameters();
        if (params == null) return vars;

        for (ParameterContext pc : params) {
            if (pc == null) continue;
            String dir = pc.getDirection();
            if (dir == null) continue;
            if (!(dir.startsWith("out") || ParameterContext.DIRECTION_INOUT.equals(dir))) continue;
            // 모델에 따라 ProcessVariable이 variable 쪽에 오거나(argument 쪽에 PV명이 오는 경우도 있어 둘 다 보수적으로 체크)
            addIfProcessVariable(vars, def, pc.getVariable() != null ? pc.getVariable().getName() : null);
            addIfProcessVariable(vars, def, pc.getArgument() != null ? pc.getArgument().getText() : null);
        }

        return vars;
    }

    private static Set<String> collectReadProcessVariableNames(ProcessDefinition def, ReceiveActivity activity) throws Exception {
        Set<String> vars = new LinkedHashSet<>();
        if (activity == null || def == null) return vars;

        ParameterContext[] params = activity.getParameters();
        if (params == null) return vars;

        for (ParameterContext pc : params) {
            if (pc == null) continue;
            String dir = pc.getDirection();
            if (dir == null) continue;
            if (!(dir.startsWith("in") || ParameterContext.DIRECTION_INOUT.equals(dir))) continue;
            addIfProcessVariable(vars, def, pc.getVariable() != null ? pc.getVariable().getName() : null);
            addIfProcessVariable(vars, def, pc.getArgument() != null ? pc.getArgument().getText() : null);
        }

        return vars;
    }

    private static SkipVarReference findFirstVariableReferenceInFuture(
            ProcessDefinition def,
            List<Activity> future,
            Set<String> writtenVars,
            DefinitionResolver resolver,
            Set<String> visitedDefinitionIds) throws Exception {

        for (Activity a : future) {
            if (a == null) continue;
            String where = a.getTracingTag();
            String whereType = a.getClass() != null ? a.getClass().getSimpleName() : null;

            // 0) CallActivity/SubProcessActivity(외부 정의 참조)까지 확장 분석
            if (a instanceof SubProcessActivity) {
                SubProcessActivity spa = (SubProcessActivity) a;

                // forEachVariable이 현재(상위) PV에 의존하는 경우도 있음 → 쓰는 변수면 SKIP 불가
                if (spa.getForEachVariable() != null) {
                    String pv = normalizeProcessVariableName(spa.getForEachVariable().getName());
                    if (pv != null && writtenVars.contains(pv)) {
                        SkipVarReference hit = new SkipVarReference();
                        hit.varName = pv;
                        hit.whereTracingTag = where;
                        hit.whereType = "CallActivity";
                        return hit;
                    }
                }

                if (resolver != null && spa.getDefinitionId() != null && !spa.getDefinitionId().trim().isEmpty()) {
                    String defId = spa.getDefinitionId().trim();
                    String ver = spa.getVersion();

                    // variableBindings로 상위 PV -> 하위 PV(argument) 매핑 후, 하위 정의에서 해당 PV 사용 여부 검사
                    Set<String> childVarsToCheck = new LinkedHashSet<>();
                    SubProcessParameterContext[] bindings = spa.getVariableBindings();
                    if (bindings != null) {
                        for (SubProcessParameterContext b : bindings) {
                            if (b == null) continue;
                            String dir = b.getDirection(); // lower-case
                            if (dir == null) continue;
                            boolean inLike = dir.startsWith("in") || ParameterContext.DIRECTION_INOUT.equals(dir);
                            if (!inLike) continue;

                            String mainVar = b.getVariable() != null ? normalizeProcessVariableName(b.getVariable().getName()) : null;
                            if (mainVar == null || !writtenVars.contains(mainVar)) continue;

                            String childArg = b.getArgument() != null ? normalizeProcessVariableName(b.getArgument().getText()) : null;
                            if (childArg != null) childVarsToCheck.add(childArg);
                        }
                    }

                    if (!childVarsToCheck.isEmpty()) {
                        ProcessDefinition subDef = resolver.resolve(defId, ver);
                        if (subDef != null) {
                            // 무한 재귀 방지
                            if (visitedDefinitionIds.add(defId + "@" + (ver == null ? "" : ver))) {
                                SkipVarReference subHit = findFirstReferenceInDefinition(subDef, childVarsToCheck, resolver, visitedDefinitionIds);
                                if (subHit != null) {
                                    SkipVarReference hit = new SkipVarReference();
                                    hit.varName = subHit.varName;
                                    hit.whereTracingTag = where + "->" + subHit.whereTracingTag;
                                    hit.whereType = "CallActivity";
                                    return hit;
                                }
                            }
                        }
                    }
                }
            }

            // 1) 이후 Activity가 입력(in/in-out)으로 읽는 변수와 교집합이면 불가
            if (a instanceof ReceiveActivity) {
                Set<String> readVars = collectReadProcessVariableNames(def, (ReceiveActivity) a);
                for (String v : writtenVars) {
                    if (readVars.contains(v)) {
                        SkipVarReference hit = new SkipVarReference();
                        hit.varName = v;
                        hit.whereTracingTag = where;
                        hit.whereType = whereType;
                        return hit;
                    }
                }
            }

            // 2) 이후 Gateway/Activity의 outgoing SequenceFlow 조건에서 변수 참조하면 불가
            List<SequenceFlow> outs = a.getOutgoingSequenceFlows();
            if (outs != null) {
                for (SequenceFlow f : outs) {
                    if (f == null) continue;
                    Condition c = f.getCondition();
                    if (c == null) continue;
                    for (String v : writtenVars) {
                        if (conditionReferencesVariable(c, v)) {
                            SkipVarReference hit = new SkipVarReference();
                            hit.varName = v;
                            hit.whereTracingTag = where;
                            hit.whereType = (a instanceof Gateway) ? "Gateway" : whereType;
                            return hit;
                        }
                    }
                }
            }

            // 3) fallback: 문자열 기반(보수적)
            try {
                String s = a.toString();
                if (s != null) {
                    for (String v : writtenVars) {
                        if (containsWord(s, v)) {
                            SkipVarReference hit = new SkipVarReference();
                            hit.varName = v;
                            hit.whereTracingTag = where;
                            hit.whereType = whereType;
                            return hit;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private static SkipVarReference findFirstReferenceInDefinition(
            ProcessDefinition def,
            Set<String> varsToCheck,
            DefinitionResolver resolver,
            Set<String> visitedDefinitionIds) throws Exception {

        if (def == null || varsToCheck == null || varsToCheck.isEmpty()) return null;

        // 시작 액티비티(여러 개 가능)부터 전체 그래프를 수집
        Set<String> visited = new HashSet<>();
        List<Activity> all = new ArrayList<>();

        if (def instanceof FlowActivity) {
            List<Activity> starts = ((FlowActivity) def).getStartActivities();
            if (starts != null) {
                for (Activity s : starts) {
                    if (s == null) continue;
                    if (s.getTracingTag() != null && visited.add(s.getTracingTag())) {
                        all.add(s);
                    }
                    for (Activity r : collectReachableActivities(s)) {
                        if (r != null && r.getTracingTag() != null && visited.add(r.getTracingTag())) {
                            all.add(r);
                        }
                    }
                }
            }
        }

        return findFirstVariableReferenceInFuture(def, all, varsToCheck, resolver, visitedDefinitionIds);
    }

    private static boolean conditionReferencesVariable(Condition c, String varName) {
        if (c == null || varName == null || varName.trim().isEmpty()) return false;
        String v = varName.trim();

        if (c instanceof Evaluate) {
            String key = ((Evaluate) c).getKey();
            // ScopeActivity.getProcessVariable은 "a.b"면 "a"로 해석할 수 있으므로, 키도 PV명으로 정규화해서 비교
            String pv = normalizeProcessVariableName(key);
            return pv != null && pv.equals(v);
        }
        if (c instanceof ExpressionEvaluateCondition) {
            String exp = ((ExpressionEvaluateCondition) c).getConditionExpression();
            return exp != null && containsWord(exp, v);
        }
        if (c instanceof And) {
            Condition[] cs = ((And) c).getConditions();
            if (cs != null) {
                for (Condition cc : cs) {
                    if (conditionReferencesVariable(cc, v)) return true;
                }
            }
            return false;
        }
        if (c instanceof Or) {
            Condition[] cs = ((Or) c).getConditions();
            if (cs != null) {
                for (Condition cc : cs) {
                    if (conditionReferencesVariable(cc, v)) return true;
                }
            }
            return false;
        }
        if (c instanceof Not) {
            return conditionReferencesVariable(((Not) c).getCondition(), v);
        }

        try {
            String s = c.toString();
            return s != null && containsWord(s, v);
        } catch (Exception ignore) {
            return false;
        }
    }

    private static void addIfProcessVariable(Set<String> out, ProcessDefinition def, String candidateName) throws Exception {
        if (out == null || def == null) return;
        String pvName = normalizeProcessVariableName(candidateName);
        if (pvName == null) return;
        ProcessVariable pv = def.getProcessVariable(pvName);
        if (pv == null) return;
        // "a.b" 같은 입력이라도 실제 PV는 "a"로 반환될 수 있으니 최종 PV명을 넣는다.
        if (pv.getName() != null) out.add(pv.getName());
    }

    private static String normalizeProcessVariableName(String name) {
        if (name == null) return null;
        String n = name.trim();
        if (n.isEmpty()) return null;
        // ScopeActivity.getProcessVariable이 "." 앞만 PV로 취급하므로 동일 기준으로 정규화
        int dot = n.indexOf('.');
        if (dot > 0) {
            n = n.substring(0, dot);
        }
        return n.trim().isEmpty() ? null : n.trim();
    }

    private static boolean containsWord(String text, String word) {
        if (text == null || word == null) return false;
        int idx = text.indexOf(word);
        while (idx >= 0) {
            boolean leftOk = idx == 0 || (!Character.isLetterOrDigit(text.charAt(idx - 1)) && text.charAt(idx - 1) != '_');
            int r = idx + word.length();
            boolean rightOk = r >= text.length() || (!Character.isLetterOrDigit(text.charAt(r)) && text.charAt(r) != '_');
            if (leftOk && rightOk) return true;
            idx = text.indexOf(word, idx + 1);
        }
        return false;
    }
}

