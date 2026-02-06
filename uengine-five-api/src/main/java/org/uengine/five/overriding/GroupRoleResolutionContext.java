package org.uengine.five.overriding;

import java.util.Map;

import org.uengine.five.service.IAMService;
import org.uengine.five.service.IAMServices;
import org.uengine.kernel.IContainsMapping;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.Role;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.RoleResolutionContext;

/**
 * 그룹 이름을 기반으로 역할을 해석하는 컨텍스트
 * IAM 공급자에서 특정 그룹에 속한 사용자인지 여부를 판단합니다.
 * 
 * @author uengine
 */
public class GroupRoleResolutionContext extends RoleResolutionContext implements IContainsMapping {
    
    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
    
    private String scope;
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public RoleMapping getActualMapping(ProcessDefinition pd, ProcessInstance instance, String tracingTag, Map options)
            throws Exception {
        if (scope == null || scope.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required for GroupRoleResolutionContext");
        }
        
        RoleMapping roleMapping = RoleMapping.create();
        roleMapping.setScope(scope);
        roleMapping.setAssignType(Role.ASSIGNTYPE_GROUP);

        return roleMapping;
    }
    
    @Override
    public String getDisplayName() {
        if (scope != null && !scope.trim().isEmpty()) {
            return "Group: " + scope;
        }
        return "Group Role Resolution";
    }
    
    @Override
    public boolean containsMapping(ProcessInstance instance, RoleMapping testingRoleMapping) throws Exception {
        if (scope == null || testingRoleMapping == null) {
            return false;
        }
        
        String testingEndpoint = testingRoleMapping.getEndpoint();
        if (testingEndpoint == null) {
            return false;
        }
        
        try {
            IAMService iamService = IAMServices.getDefault();
            return iamService.isInGroup(testingEndpoint, scope);
        } catch (Exception e) {
            return false;
        }
    }
}
