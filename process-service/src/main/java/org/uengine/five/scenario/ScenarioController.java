package org.uengine.five.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 시나리오 비즈니스 로직 담당.
 * REST API는 제공하지 않으며, {@link org.uengine.five.service.InstanceServiceImpl}에서
 * 시나리오 관련 API를 노출하고 이 컨트롤러를 호출하는 방식으로 사용.
 */
@Component
public class ScenarioController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ScenarioLoader scenarioLoader;

    @Autowired
    private ScenarioGenerator scenarioGenerator;

    @Autowired
    private ScenarioBranchGenerator scenarioBranchGenerator;

    @Autowired
    private ScenarioRunner scenarioRunner;

    /**
     * 프로세스별 저장된 시나리오 목록 조회. 파일 없으면 빈 리스트.
     */
    public List<Scenario> getScenarios(String processDefinitionId) throws Exception {
        return scenarioLoader.loadByProcess(processDefinitionId);
    }

    /**
     * 프로세스별 시나리오 목록 저장 후 저장된 목록 반환.
     */
    public List<Scenario> putScenarios(String processDefinitionId, List<Scenario> scenarios) throws Exception {
        if (scenarios == null) {
            scenarios = new ArrayList<>();
        }
        scenarioLoader.saveByProcess(processDefinitionId, scenarios);
        return scenarios;
    }

    /**
     * BPMN 기반 시나리오 초안 생성. merge=true면 기존 시나리오와 이름 기준 병합 후 저장.
     * Draft(UserTask/Gateway) + DMN 분기를 합친 {@link ScenarioGenerator}에 위임.
     *
     * @param merge                 true면 기존 시나리오와 병합 후 저장
     * @param includeGatewayBranches true면 일반 Gateway 분기 시나리오 포함
     * @param includeDmnBranches     true면 DMN 규칙별 분기 시나리오 포함
     */
    public List<Scenario> generateScenarios(String processDefinitionId, boolean merge,
            boolean includeGatewayBranches, boolean includeDmnBranches) throws Exception {
        return scenarioGenerator.generateScenarios(processDefinitionId, merge, includeGatewayBranches, includeDmnBranches);
    }

    /**
     * DMN 분기 시나리오만 생성하여 반환 (저장하지 않음).
     */
    public List<Scenario> generateBranches(String processDefinitionId) throws Exception {
        return scenarioBranchGenerator.generateDmnBranchScenarios(processDefinitionId);
    }

    /**
     * 시나리오 실행. body는 시나리오 본문(Map) 또는 scenarioId만 담긴 Map.
     *
     * @param body "scenarioId" 키가 있으면 해당 이름으로 저장된 시나리오 로드, 없으면 body를 시나리오로 사용
     */
    public RunResult runScenario(String processDefinitionId, Map<String, Object> body) throws Exception {
        Scenario scenario;
        if (body != null && body.containsKey("scenarioId")) {
            String nameOrId = String.valueOf(body.get("scenarioId"));
            scenario = scenarioLoader.loadOne(processDefinitionId, nameOrId);
        } else {
            scenario = mapToScenario(processDefinitionId, body);
        }
        return scenarioRunner.runWithResult(scenario);
    }

    private static Scenario mapToScenario(String processDefinitionId, Map<String, Object> body) throws Exception {
        if (body == null) {
            throw new IllegalArgumentException("요청 본문이 없습니다.");
        }
        Scenario scenario = objectMapper.convertValue(body, Scenario.class);
        if (scenario.getProcessDefinitionId() == null) {
            scenario.setProcessDefinitionId(processDefinitionId);
        }
        return scenario;
    }
}
