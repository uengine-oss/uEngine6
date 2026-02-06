package org.uengine.five.service;

import java.util.List;
import java.util.Map;

/**
 * IAM(Identity & Access Management) 공급자 추상화 인터페이스.
 *
 * <p>
 * RoleResolutionContext 계열에서 직렬화 제약이 있어 Spring DI 대신
 * {@link IAMServices#getDefault()} 같은 정적 팩토리 접근 패턴을 사용합니다.
 * </p>
 *
 * <p>
 * 새 IAM 연동(예: LDAP, 사내 IAM 등)을 추가할 때는 이 인터페이스를 구현하고,
 * {@link IAMServices}에 providerId 매핑을 추가하면 됩니다.
 * </p>
 */
public interface IAMService {

    /**
     * 공급자 식별자. 예) "keycloak", "ldap", "custom"
     */
    String getProviderId();

    /**
     * 사용자 ID(로그인 ID 등)로 사용자 정보를 조회합니다.
     */
    Map<String, Object> getUserById(String userId) throws Exception;

    /**
     * 그룹 이름으로 그룹에 속한 사용자 목록을 조회합니다.
     */
    List<String> getUsersByGroup(String groupName) throws Exception;

    /**
     * 역할/스코프 이름으로 해당 역할을 가진 사용자 목록을 조회합니다.
     */
    List<String> getUsersByRole(String roleName) throws Exception;

    /**
     * 사용자에게 부여된 역할/스코프 목록을 조회합니다.
     */
    List<String> getUserScopes(String userId) throws Exception;

    /**
     * 사용자가 속한 그룹 목록을 조회합니다.
     */
    List<String> getUserGroups(String userId) throws Exception;

    /**
     * 편의 메서드: 사용자가 특정 scope을 가지고 있는지 확인합니다.
     */
    default boolean hasScope(String userId, String scope) throws Exception {
        List<String> scopes = getUserScopes(userId);
        return scopes != null && scopes.contains(scope);
    }

    /**
     * 편의 메서드: 사용자가 특정 그룹에 속하는지 확인합니다.
     */
    default boolean isInGroup(String userId, String groupName) throws Exception {
        List<String> groups = getUserGroups(userId);
        return groups != null && groups.contains(groupName);
    }
}

