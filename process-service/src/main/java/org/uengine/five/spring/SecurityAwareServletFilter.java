package org.uengine.five.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.uengine.contexts.UserContext;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class SecurityAwareServletFilter implements Filter {

    static String userId;

    static public String getUserId() {
        return userId;
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String accessToken = req.getHeader("Authorization");

        if (accessToken != null) {  
            try {
                res.setHeader("Access-Control-Allow-Origin", "*");
                res.setHeader("Access-Control-Allow-Credentials", "true");
                res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
                res.setHeader("Access-Control-Max-Age", "3600");
                res.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

                accessToken = accessToken.split("Bearer ")[1];
                DecodedJWT decodedJWT = JWT.decode(accessToken);

                List<String> groups = decodedJWT.getClaim("groups").asList(String.class);
                String userId = decodedJWT.getClaim("email").asString();
                if (userId == null || userId.isEmpty()) {
                    userId = decodedJWT.getClaim("preferred_username").asString();
                }
                if (userId == null || userId.isEmpty()) {
                    userId = decodedJWT.getClaim("sub").asString();
                }

                List<String> roles = null;
                try {
                    java.util.Map<String, Object> realmAccess = decodedJWT.getClaim("realm_access").asMap();
                    if (realmAccess != null) {
                        Object r = realmAccess.get("roles");
                        if (r instanceof List) roles = (List<String>) r;
                    }
                } catch (Exception ignore) {}

                if (userId != null && !userId.isEmpty()) {
                    SecurityAwareServletFilter.userId = userId;
                    UserContext.getThreadLocalInstance().setUserId(userId);
                } else {
                    System.err.println("[SecurityAwareServletFilter] JWT userId extraction failed. Check email/preferred_username/sub claims.");
                }
                UserContext.getThreadLocalInstance().setScopes(roles);
                UserContext.getThreadLocalInstance().setGroups(groups);
            } catch (Exception e) {
                System.out.println("Error when to parse accesstoken: " + e.getMessage());
            }
        }

        chain.doFilter(req, res);

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
