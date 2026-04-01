package org.uengine.five.service;

import org.springframework.stereotype.Component;

/**
 * Oracle 저장 시 TB_BPM_PROCDEF / TB_BPM_PDEF_VER 의 작성자·수정자 표시명을 결정한다.
 * <p>
 * {@code /definition/raw/**} 로 들어온 {@link org.uengine.five.service.DefinitionRequest#getUpdatedByName()}
 * 가 있으면 그 값을 쓰고, 없으면 {@value #DEFAULT_ACTOR} 을 쓴다. JWT·헤더는 사용하지 않는다.
 * raw 저장이 아닌 경로(업로드·메트릭 등)에서 호출되면 항상 {@value #DEFAULT_ACTOR} 이다.
 */
@Component
public class DefinitionActorNameProvider {

    public static final String DEFAULT_ACTOR = "system";

    private final ThreadLocal<Boolean> rawSaveActive = new ThreadLocal<>();
    private final ThreadLocal<String> rawPayloadActor = new ThreadLocal<>();

    /**
     * {@code putRawDefinition} 등 raw 저장 직전에 호출하고, {@link #endRawSave()} 는 finally 에서 호출한다.
     *
     * @param updatedByNameFromPayload 요청 JSON 의 updatedByName / updated_by_name (null 허용)
     */
    public void beginRawSave(String updatedByNameFromPayload) {
        rawSaveActive.set(Boolean.TRUE);
        if (updatedByNameFromPayload != null && !updatedByNameFromPayload.isBlank()) {
            rawPayloadActor.set(updatedByNameFromPayload.trim());
        } else {
            rawPayloadActor.remove();
        }
    }

    public void endRawSave() {
        rawSaveActive.remove();
        rawPayloadActor.remove();
    }

    public String resolveDisplayName() {
        if (!Boolean.TRUE.equals(rawSaveActive.get())) {
            return DEFAULT_ACTOR;
        }
        String p = rawPayloadActor.get();
        return (p != null && !p.isBlank()) ? p : DEFAULT_ACTOR;
    }
}
