package org.uengine.five.scenario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Draft(UserTask/Gateway) + DMN 분기를 합친 시나리오 생성기.
 * BPMN 기반 초안 생성과 비즈니스 룰(DMN) 규칙별 분기 시나리오를 한 번에 생성.
 */
@Component
public class ScenarioGenerator {

    private final ScenarioLoader scenarioLoader;
    private final ScenarioDraftGenerator scenarioDraftGenerator;
    private final ScenarioBranchGenerator scenarioBranchGenerator;

    public ScenarioGenerator(
            @Autowired ScenarioLoader scenarioLoader,
            @Autowired ScenarioDraftGenerator scenarioDraftGenerator,
            @Autowired ScenarioBranchGenerator scenarioBranchGenerator) {
        this.scenarioLoader = scenarioLoader;
        this.scenarioDraftGenerator = scenarioDraftGenerator;
        this.scenarioBranchGenerator = scenarioBranchGenerator;
    }

    /**
     * BPMN 기반 시나리오 생성 (Draft + 옵션 Gateway 분기 + 옵션 DMN 분기).
     * merge=true면 기존 시나리오와 이름 기준 병합 후 저장.
     *
     * @param merge                 true면 기존 시나리오와 병합 후 저장
     * @param includeGatewayBranches true면 일반 Gateway 조건별 분기 시나리오 포함
     * @param includeDmnBranches     true면 DMN 규칙별 분기 시나리오 포함 (비즈니스 룰 조건값이 given에 채워짐)
     */
    public List<Scenario> generateScenarios(String processDefinitionId, boolean merge,
            boolean includeGatewayBranches, boolean includeDmnBranches) throws Exception {
        List<Scenario> drafts = scenarioDraftGenerator.generateDrafts(processDefinitionId, includeGatewayBranches);
        if (includeDmnBranches) {
            List<Scenario> dmnBranches = scenarioBranchGenerator.generateDmnBranchScenarios(processDefinitionId);
            drafts = new ArrayList<>(drafts);
            drafts.addAll(dmnBranches);
        }
        if (merge) {
            List<Scenario> existing = scenarioLoader.loadByProcess(processDefinitionId);
            Map<String, Scenario> byName = existing.stream()
                    .collect(Collectors.toMap(Scenario::getName, s -> s, (a, b) -> b, LinkedHashMap::new));
            for (Scenario s : drafts) {
                if (s.getName() != null) {
                    byName.put(s.getName(), s);
                }
            }
            List<Scenario> merged = new ArrayList<>(byName.values());
            scenarioLoader.saveByProcess(processDefinitionId, merged);
            return merged;
        }
        return drafts;
    }
}
