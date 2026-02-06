package org.uengine.five.service;

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
}

