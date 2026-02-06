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
 * IAM 역할(Scope) 기반 역할 해석 컨텍스트
 * IAM 공급자에서 특정 scope(역할)을 가진 사용자인지 여부를 판단합니다.
 * 
 * Created by uengine on 2018. 4. 5..
 * Updated: IAM 공급자 추상화 적용 (IAMService)
 */
public class IAMRoleResolutionContext extends RoleResolutionContext implements IContainsMapping {
    
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
            throw new IllegalArgumentException("Scope is required for IAMRoleResolutionContext");
        }
        
        RoleMapping roleMapping = RoleMapping.create();
        roleMapping.setScope(scope);
        roleMapping.setAssignType(Role.ASSIGNTYPE_ROLE);

        return roleMapping;
    }

    @Override
    public String getDisplayName() {
        if (scope != null && !scope.trim().isEmpty()) {
            return "Who has the scope '" + scope + "'";
        }
        return "IAM Role Resolution";
    }

    @Override
    public boolean containsMapping(ProcessInstance instance, RoleMapping testingRoleMapping) throws Exception {
        if (scope == null || testingRoleMapping == null) {
            return false;
        }
        
        String currentLoginEndpoint = testingRoleMapping.getEndpoint();
        if (currentLoginEndpoint == null) {
            return false;
        }
        
        try {
            IAMService iamService = IAMServices.getDefault();
            return iamService.hasScope(currentLoginEndpoint, scope);
        } catch (Exception e) {
            return false;
        }
    }
}
/* 기존 코드 (주석 처리)
package org.uengine.five.overriding;

import java.util.List;
import java.util.Map;

import org.uengine.kernel.IContainsMapping;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.RoleResolutionContext;

// Created by uengine on 2018. 4. 5..
public class IAMRoleResolutionContext extends RoleResolutionContext implements IContainsMapping {
    @Override
    public RoleMapping getActualMapping(ProcessDefinition pd, ProcessInstance instance, String tracingTag, Map options)
            throws Exception {
        RoleMapping roleMapping = RoleMapping.create();
        roleMapping.setEndpoint(getScope());// 혹은 keycloak API 모든 scope을 가진 유저를 검색해서 endpoint에 집어넣느냐.
        return roleMapping;
    }

    @Override
    public String getDisplayName() {
        return "Who has the scope '" + getScope() + "'";
    }

    String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    public boolean containsMapping(ProcessInstance instance, RoleMapping testingRoleMapping) throws Exception {
		RoleMapping thisRoleMapping = getActualMapping(instance.getProcessDefinition(), instance, null, null);
        String instanceEndpoint = thisRoleMapping.getEndpoint(); //
        String currentLoginEndpoint = testingRoleMapping.getEndpoint(); // initiator@uengine.org

        // KeyClock Logic
        //   call keyclock (currentLoginEndpoint)
        
        // List<String> scopes = callKeyClock(currentLoginEndpoint); // keyclock 쪽으로 post 시 scope 정보 리턴 되도록.
        // for( String scope : scopes) {
        //     if(scope.equals(instanceEndpoint)){
        //         return true;
        //     }
        // }
        // return false;

		return true;
	}

	// public boolean containsMapping(RoleMapping thisRoleMapping, RoleMapping testingRoleMapping){
	// 	List<String> endpoints = UserContext.getThreadLocalInstance().getScopesByUserId(testingRoleMapping.getEndpoint());
		
	// 	String endpointToCheck = thisRoleMapping.getEndpoint();
	// 	for (String endpoint : endpoints) {
	// 		if (endpoint.equals(endpointToCheck)) {
	// 			return true;
	// 		}
	// 	}
	// 	return false;
	// }
	

}
*/

