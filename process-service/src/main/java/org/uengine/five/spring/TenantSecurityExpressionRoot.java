package org.uengine.five.spring;

import org.oce.garuda.multitenancy.TenantContext;
import org.springframework.security.access.expression.*;
import org.springframework.security.core.Authentication;
import org.uengine.five.framework.ProcessTransactionContext;


public class TenantSecurityExpressionRoot{ //extends SecurityExpressionRoot {

    public TenantSecurityExpressionRoot(Authentication authentication) {
        //super(authentication);
        // TODO Auto-generated constructor stub
    }

    //@Override
    public Object getPrincipal() {
        //FIXME:  remove me
        String userId = SecurityAwareServletFilter.getUserId();
        TenantContext.getThreadLocalInstance().setUserId(userId);
        //

        return TenantContext.getThreadLocalInstance();
 
    }


}