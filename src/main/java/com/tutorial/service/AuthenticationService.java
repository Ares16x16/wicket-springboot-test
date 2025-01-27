package com.tutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;

import com.tutorial.model.LoginUser;

@Service
public class AuthenticationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        return authenticateFromDB(username, password);
    }

    public boolean authenticateFromDB(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return true; // admin credentials
        }
        String sql = "SELECT password FROM login_users WHERE username=?";
        try {
            String storedPassword = jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class);
            if (storedPassword != null) {
                return passwordEncoder.matches(password, storedPassword);
            }
        } catch (EmptyResultDataAccessException e) {
            // User not found
        }
        return false;
    }

    public void createAccountInDB(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        String sql = "INSERT INTO login_users (username, password) VALUES (?, ?)";
        jdbcTemplate.update(sql, username, encodedPassword);
    }

    public List<LoginUser> getAllUsers() {
        String sql = "SELECT username FROM login_users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LoginUser(rs.getString("username")));
    }

    public void deleteUser(String username) {
        String sql = "DELETE FROM login_users WHERE username = ?";
        jdbcTemplate.update(sql, username);
    }
}
