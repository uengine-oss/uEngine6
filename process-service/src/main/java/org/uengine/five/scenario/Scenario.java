package org.uengine.five.scenario;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

/**
 * 시나리오 데이터 모델 (Given-When만 사용).
 * 프로세스 실행 시나리오 정의. 관찰/판정(Then)은 시뮬레이터 UI 또는 수동 확인.
 *
 * <ul>
 *   <li><b>Given</b>: 프로세스 변수, roleMappings, instanceName 등 사전 조건 (최소 입력/초기 상태)</li>
 *   <li><b>When</b>: 현재 태스크. activityId/activityName(현재 태스크), status(이 태스크에 대한 동작: "Running"=해당 태스크까지 도달, "Completed"=이 태스크 완료), payloadMapping(이 태스크 Complete 시 넣을 payload). steps 있으면 다단계(여러 현재 태스크 순서).</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenario implements Serializable {

    private String name;
    private String processDefinitionId;
    /** (조건·계산 미사용) JSON 파싱용. 시작 위치는 when.startFromActivityId 사용. */
    private String startFromActivityName;
    /** (조건·계산 미사용) JSON 파싱용. 시작 위치는 when.startFromActivityId 사용. */
    private String startFromActivityId;
    private Map<String, Object> given;
    private Map<String, Object> when;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }

    public String getStartFromActivityName() { return startFromActivityName; }
    public void setStartFromActivityName(String startFromActivityName) { this.startFromActivityName = startFromActivityName; }

    public String getStartFromActivityId() { return startFromActivityId; }
    public void setStartFromActivityId(String startFromActivityId) { this.startFromActivityId = startFromActivityId; }

    public Map<String, Object> getGiven() { return given; }
    public void setGiven(Map<String, Object> given) { this.given = given; }

    public Map<String, Object> getWhen() { return when; }
    public void setWhen(Map<String, Object> when) { this.when = when; }
}
