package org.uengine.five.overriding;

import java.util.Map;
import java.util.List;
import org.uengine.five.service.IAMService;
import org.uengine.contexts.UserContext;
import org.uengine.five.service.IAMServices;
import org.uengine.kernel.IContainsMapping;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.Role;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.RoleResolutionContext;

/**
 * IAM ????Scope) ?リ옇?↑???????怨댄맍 ???쳜????덈콦
 * IAM ??ㅻ?????????獄???scope(???????띠럾?嶺???????逾η춯?뼿 ????????堉??紐껊퉵??
 * 
 * Created by uengine on 2018. 4. 5..
 * Updated: IAM ??ㅻ?????怨뺣뾼疫????⑤챷??(IAMService)
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
            List<String> requestScopes = UserContext.getThreadLocalInstance().getScopes();
            if (requestScopes != null && requestScopes.contains(scope)) {
                return true;
            }

            IAMService iamService = IAMServices.getDefault();
            return iamService.hasScope(currentLoginEndpoint, scope);
        } catch (Exception e) {
            return false;
        }
    }
}
/* ?リ옇????袁⑤?獄?(?낅슣?섋땻?嶺뚳퐣瑗??
package org.uengine.five.overriding;

import java.util.List;
import java.util.Map;
import java.util.List;

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
        roleMapping.setEndpoint(getScope());// ?獄?? keycloak API 嶺뚮ㅄ維獄?scope???띠럾?嶺???????롪틵???????endpoint??嶺뚯쉶理먨젆?影?ル츇??
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
        
        // List<String> scopes = callKeyClock(currentLoginEndpoint); // keyclock 嶺뚯옕????뿉?post ??scope ?筌먲퐢沅??洹먮뿪????濡レ┣??
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

