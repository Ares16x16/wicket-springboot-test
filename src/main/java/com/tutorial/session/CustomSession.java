package com.tutorial.session;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import java.util.HashSet;
import java.util.Set;
import org.apache.wicket.Session;

public class CustomSession extends WebSession {
    private static final long serialVersionUID = 1L;
    private String username;
    private Set<String> roles;
    private long tokenExpirationTime;

    public CustomSession(Request request) {
        super(request);
        this.roles = new HashSet<>();
    }

    public static CustomSession get() {
        return (CustomSession) Session.get();
    }

    // New signIn method that accepts token max age in seconds
    public void signIn(String username, Set<String> roles, int tokenMaxAgeSeconds) {
        this.username = username;
        this.roles = roles;
        this.tokenExpirationTime = System.currentTimeMillis() + tokenMaxAgeSeconds * 1000L;
        dirty();
        bind();
    }

    // Modified isSignedIn to check token expiration
    //@Override
    public boolean isSignedIn() {
        return username != null && System.currentTimeMillis() < tokenExpirationTime;
    }

    public void signOut() {
        username = null;
        roles.clear();
        invalidate();
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
