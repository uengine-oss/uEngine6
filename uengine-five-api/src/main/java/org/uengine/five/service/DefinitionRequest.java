package org.uengine.five.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload for /definition/raw/** (putRawDefinition).
 *
 * Kept in uengine-five-api so other services (e.g. process-service) can call
 * definition-service via Feign.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefinitionRequest {

    private String definition;
    private String version;
    /** 선택: 넣으면 Oracle 저장 시 TB_BPM_PROCDEF.name 을 이 값으로 덮어씀 (최대 255자). */
    private String name;
    /**
     * 선택: Oracle 저장 시 created_by_name / updated_by_name 에 기록할 표시 문자열.
     * 비어 있거나 생략 시 서버 기본값 {@code system} 을 사용한다.
     */
    @JsonAlias("updated_by_name")
    private String updatedByName;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }
}

