package com.tutorial.modelchain;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;

public class CustomRoleCheckingStrategy implements IRoleCheckingStrategy {
    @Override
    public boolean hasAnyRole(org.apache.wicket.authroles.authorization.strategies.role.Roles roles) {
        CustomSession session = CustomSession.get();
        for (String role : roles) {
            if (session.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
