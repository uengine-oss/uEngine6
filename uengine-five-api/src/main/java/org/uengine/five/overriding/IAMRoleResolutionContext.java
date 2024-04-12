package org.uengine.five.overriding;

import java.util.List;
import java.util.Map;

import org.uengine.kernel.IContainsMapping;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.RoleResolutionContext;

/**
 * Created by uengine on 2018. 4. 5..
 */
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

        /* 
         *  KeyClock Logic
         *   call keyclock (currentLoginEndpoint)
         */
        
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
