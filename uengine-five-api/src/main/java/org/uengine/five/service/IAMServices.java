package org.uengine.five.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.uengine.kernel.GlobalContext;
import org.uengine.util.UEngineUtil;

/**
 * IAMService 정적 접근 팩토리.
 *
 * <p>
 * RoleResolutionContext가 직렬화되어야 하는 구조 특성상, Spring DI 대신
 * 런타임에 정적으로 IAM 구현을 선택하는 방식(Strategy)을 사용합니다.
 * </p>
 *
 * <p>
 * 선택 기준:
 * </p>
 * <ul>
 *   <li>환경변수: IAM_PROVIDER</li>
 *   <li>GlobalContext property: iam.provider</li>
 *   <li>기본값: keycloak</li>
 * </ul>
 */
public final class IAMServices {

    private static final String DEFAULT_PROVIDER_ID = "keycloak";

    private static final Map<String, IAMService> CACHE = new ConcurrentHashMap<>();

    private IAMServices() {
    }

    public static IAMService getDefault() {
        String providerId = System.getenv("IAM_PROVIDER");
        if (!UEngineUtil.isNotEmpty(providerId)) {
            providerId = GlobalContext.getPropertyString("iam.provider", DEFAULT_PROVIDER_ID);
        }
        return get(providerId);
    }

    public static IAMService get(String providerId) {
        final String normalized = UEngineUtil.isNotEmpty(providerId) ? providerId.trim().toLowerCase() : DEFAULT_PROVIDER_ID;
        return CACHE.computeIfAbsent(normalized, IAMServices::createService);
    }

    private static IAMService createService(String providerId) {
        // 최소 구성: providerId -> 구현 직접 매핑
        if (DEFAULT_PROVIDER_ID.equalsIgnoreCase(providerId) || "keycloak".equalsIgnoreCase(providerId)) {
            return KeycloakRoleService.getDefault();
        }

        throw new IllegalStateException("Unsupported iam.provider='" + providerId + "'. " +
                "Currently supported: keycloak. (Add mapping in IAMServices#createService)");
    }
}

