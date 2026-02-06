package org.uengine.five.dto;

/**
 * REST 요청에서 RoleMapping을 직접 주고받지 않고(abstract + polymorphism 문제),
 * 필요한 최소 필드만 전달하기 위한 커맨드 DTO.
 *
 * - 서버에서는 이 값을 받아 RoleMapping.create()로 실제 RoleMapping 구현체를 만들고 필드를 세팅한다.
 */
public class RoleMappingCommand {

    private String endpoint;
    private String resourceName;
    private String scope;
    private Integer assignType;

    public RoleMappingCommand() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getAssignType() {
        return assignType;
    }

    public void setAssignType(Integer assignType) {
        this.assignType = assignType;
    }
}

