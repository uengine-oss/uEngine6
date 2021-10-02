package org.uengine.five.spring;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.metaworks.annotation.Order;
import org.oce.garuda.multitenancy.TenantContext;
import org.springframework.stereotype.Component;
import org.uengine.five.framework.ProcessTransactionContext;

@Component
@Order(1)
public class SecurityAwareServletFilter implements Filter {

    static String userId;
    static public String getUserId(){
        return userId;
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String accessToken = req.getHeader("authorization");

        if (accessToken != null) {

            try {

                accessToken = accessToken.split("Bearer ")[1];
                DecodedJWT decodedJWT = JWT.decode(accessToken);

                String userId = decodedJWT.getClaim("email").asString();
                // ProcessTransactionContext.getThreadLocalInstance().setSharedContext("loggedUserId", userId);
                // TenantContext.getThreadLocalInstance().setUserId(userId);

                SecurityAwareServletFilter.userId = (userId);
            } catch (Exception e) {
                System.out.println("Error when to parse accesstoken: " + e.getMessage());
            }
        }

        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    // other methods 
}
