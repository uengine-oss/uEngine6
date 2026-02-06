package org.uengine.five.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.uengine.processmanager.DefinitionServiceLocal;

import java.util.ArrayList;
import java.util.List;

/**
 * definitions/scenarios/에서 시나리오를 로드하는 클래스
 * BusinessRuleStore와 동일한 방식으로 DefinitionServiceLocal을 사용
 */
@Component
public class ScenarioLoader {

    static final String SCENARIOS_DIR = "scenarios";
    static final String SCENARIO_EXT = ".scenario";
    /** 프로세스당 1파일(배열) 확장자 */
    static final String SCENARIOS_ARRAY_EXT = ".scenarios";

    private final DefinitionServiceLocal definitionService;
    private final ObjectMapper objectMapper;

    public ScenarioLoader(@Autowired DefinitionServiceLocal definitionService) {
        this.definitionService = definitionService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * definitions/scenarios/에서 시나리오 로드
     * 경로 형식: "xxx" 또는 "xxx.scenario" (scenarios/ 접두사는 자동 추가)
     *
     * @param scenarioName 시나리오 이름 (예: "승인_케이스_전체" 또는 "승인_케이스_전체.scenario")
     * @return Scenario 객체
     * @throws Exception 시나리오 파일을 찾을 수 없거나 파싱 오류 시
     */
    public Scenario load(String scenarioName) throws Exception {
        String path = buildScenarioPath(scenarioName);
        return readScenario(path);
    }

    /**
     * processDefinitionId를 파일 경로에 쓸 수 있는 형태로 정규화.
     * 한 곳에서만 정의. trim, \ → /, 선행 / 제거, / → _ (파일명 단일 세그먼트).
     */
    public static String normalizeProcessDefinitionId(String processDefinitionId) {
        if (processDefinitionId == null) {
            return "";
        }
        String p = processDefinitionId.trim().replace("\\", "/");
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p.replace("/", "_");
    }

    /**
     * 프로세스당 1파일(배열) 경로: scenarios/{normalize(processDefinitionId)}.scenarios
     */
    private String buildScenarioArrayPath(String processDefinitionId) {
        return SCENARIOS_DIR + "/" + normalizeProcessDefinitionId(processDefinitionId) + SCENARIOS_ARRAY_EXT;
    }

    /**
     * 해당 프로세스의 시나리오 배열 로드. 파일 없으면 빈 리스트.
     */
    public List<Scenario> loadByProcess(String processDefinitionId) throws Exception {
        String path = buildScenarioArrayPath(processDefinitionId);
        try {
            Object raw = definitionService.getRawDefinition(path);
            String json = toJsonString(raw);
            List<Scenario> list = objectMapper.readValue(json, new TypeReference<List<Scenario>>() {});
            return list != null ? list : new ArrayList<>();
        } catch (ResponseStatusException rse) {
            if (rse.getStatus() == HttpStatus.NOT_FOUND) {
                return new ArrayList<>();
            }
            throw rse;
        } catch (Exception e) {
            if (e != null && e.getMessage() != null && e.getMessage().contains("404")) {
                return new ArrayList<>();
            }
            throw e;
        }
    }

    /**
     * 해당 프로세스의 시나리오 배열 저장.
     */
    public void saveByProcess(String processDefinitionId, List<Scenario> scenarios) throws Exception {
        if (scenarios == null) {
            scenarios = new ArrayList<>();
        }
        String path = buildScenarioArrayPath(processDefinitionId);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scenarios);
        definitionService.putRawDefinition(path, json);
    }

    /**
     * 프로세스당 배열에서 name 또는 id로 단일 시나리오 조회.
     * 없으면 NOT_FOUND.
     */
    public Scenario loadOne(String processDefinitionId, String nameOrId) throws Exception {
        List<Scenario> list = loadByProcess(processDefinitionId);
        for (Scenario s : list) {
            if (s.getName() != null && s.getName().equals(nameOrId)) {
                return s;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "시나리오를 찾을 수 없습니다: processDefinitionId=" + processDefinitionId + ", nameOrId=" + nameOrId);
    }

    /**
     * 모든 시나리오 목록 조회
     */
    public List<String> listAll() throws Exception {
        String json;
        try {
            json = definitionService.listDefinitionRaw(SCENARIOS_DIR);
        } catch (Exception e) {
            // 디렉토리가 없을 수 있음
            return new ArrayList<>();
        }

        JsonNode root = objectMapper.readTree(json);
        JsonNode defs = root != null ? root.path("_embedded").path("definitions") : null;
        if (defs == null || !defs.isArray()) {
            return new ArrayList<>();
        }

        List<String> scenarioNames = new ArrayList<>();
        for (JsonNode d : defs) {
            String name = d.path("name").asText(null);
            if (name != null && name.endsWith(SCENARIO_EXT)) {
                // 확장자 제거
                String scenarioName = name.substring(0, name.length() - SCENARIO_EXT.length());
                scenarioNames.add(scenarioName);
            }
        }
        return scenarioNames;
    }

    private String buildScenarioPath(String scenarioName) {
        if (scenarioName.endsWith(SCENARIO_EXT)) {
            return SCENARIOS_DIR + "/" + scenarioName;
        }
        return SCENARIOS_DIR + "/" + scenarioName + SCENARIO_EXT;
    }

    /**
     * JSON을 Scenario로 파싱. given, when, then 모두 매핑.
     * when 생략 시 null, 암묵 When(startProcess) 적용.
     */
    private Scenario readScenario(String path) throws Exception {
        try {
            Object raw = definitionService.getRawDefinition(path);
            String json = toJsonString(raw);
            return objectMapper.readValue(json, Scenario.class);
        } catch (ResponseStatusException rse) {
            if (rse.getStatus() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다: " + path);
            }
            throw rse;
        } catch (Exception e) {
            // Feign 오류 등에서 404가 포함된 경우
            if (e != null && e.getMessage() != null && e.getMessage().contains("404")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다: " + path);
            }
            throw e;
        }
    }

    private String toJsonString(Object raw) throws Exception {
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "시나리오 파일이 비어있습니다");
        }
        if (raw instanceof String) {
            return (String) raw;
        }
        if (raw instanceof JsonNode) {
            return objectMapper.writeValueAsString((JsonNode) raw);
        }
        return objectMapper.writeValueAsString(raw);
    }
}
