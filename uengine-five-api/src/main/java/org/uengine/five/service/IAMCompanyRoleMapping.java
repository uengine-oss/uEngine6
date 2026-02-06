package org.uengine.five.service;

import org.uengine.kernel.RoleMapping;
import org.uengine.util.UEngineUtil;

import java.util.Map;

/**
 * IAM 기반 RoleMapping 구현체 (resName(resourceName) 채움 전용).
 *
 * <p>
 * - `process-service`에서 `rolemapping.class`로 이 클래스를 지정하면
 *   `RoleMapping.create()`가 이 구현체를 생성합니다.
 * - `fill()` 시점에만 IAMService를 호출하며, endpoint 기준 LRU 캐시로 중복 호출을 줄입니다.
 * </p>
 */
public class IAMCompanyRoleMapping extends RoleMapping {

    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    @Override
    protected String doFill() throws Exception {
        String endpoint = getEndpoint();
        if (!UEngineUtil.isNotEmpty(endpoint)) return null;

        IAMService iamService = IAMServices.getDefault();
        if (iamService == null) return endpoint;

        Map<String, Object> user = iamService.getUserById(endpoint);
        if (user == null) return endpoint;

        String username = asString(user.get("username"));

        return username;
    }
}

