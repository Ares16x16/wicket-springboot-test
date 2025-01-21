package com.tutorial.modelchain;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    public boolean authenticate(String username, String password) {
        // Set test credentials here
        if ("admin".equals(username) && "admin".equals(password)) {
            return true;
        }
        return "test".equals(username) && "test".equals(password);
    }
}
