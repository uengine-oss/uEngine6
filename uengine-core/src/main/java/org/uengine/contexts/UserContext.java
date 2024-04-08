package org.uengine.contexts;

import java.util.List;

public class UserContext {

    private static final ThreadLocal<UserContext> instance = new ThreadLocal<UserContext>() {
        @Override
        protected UserContext initialValue() {
            return new UserContext();
        }
    };

    private UserContext() {
    }

    public static UserContext getThreadLocalInstance() {
        return instance.get();
    }

    private String userId;
    private List<String> scopes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

}
