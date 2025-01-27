package com.tutorial.model;

import java.io.Serializable;

public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    
    public LoginUser(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}