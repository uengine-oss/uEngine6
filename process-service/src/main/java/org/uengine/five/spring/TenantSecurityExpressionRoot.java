package org.uengine.five.spring;

//import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.uengine.contexts.UserContext;

public class TenantSecurityExpressionRoot {// extends SecurityExpressionRoot {

    public TenantSecurityExpressionRoot(Authentication authentication) {
        // super(authentication);

    }

    // @Override
    public Object getPrincipal() {
        // FIXME: remove me
        return UserContext.getThreadLocalInstance();

    }

}