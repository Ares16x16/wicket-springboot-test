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

    public CustomSession(Request request) {
        super(request);
        this.roles = new HashSet<>();
    }

    public static CustomSession get() {
        return (CustomSession) Session.get();
    }

    public void signIn(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
        dirty();  // Mark session as dirty
        bind();   // Ensure session is bound
    }

    public boolean isSignedIn() {
        return username != null;
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
