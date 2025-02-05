package com.tutorial.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

import com.tutorial.model.LoginUser;

@Service
@Transactional
public class AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        return authenticateFromDB(username, password);
    }

    public boolean authenticateFromDB(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return true; // admin credentials
        }
        String sql = "SELECT password FROM login_users WHERE username = ?";
        try {
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, username);
            String storedPassword = (String) query.getSingleResult();
            if (storedPassword != null) {
                return passwordEncoder.matches(password, storedPassword);
            }
        } catch (NoResultException e) {
            // User not found
        }
        return false;
    }

    @CacheEvict(value = "login_users", allEntries = true)
    public void createAccountInDB(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        String sql = "INSERT INTO login_users (username, password) VALUES (?, ?)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, username);
        query.setParameter(2, encodedPassword);
        query.executeUpdate();
    }

    @Cacheable("login_users")
    public List<LoginUser> getAllUsers() {
        String sql = "SELECT username FROM login_users";
        Query query = entityManager.createNativeQuery(sql);
        List<String> usernames = query.getResultList();
        // Map usernames to LoginUser objects
        return usernames.stream().map(LoginUser::new).toList();
    }

    @CacheEvict(value = "login_users", allEntries = true)
    public void deleteUser(String username) {
        String sql = "DELETE FROM login_users WHERE username = ?";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, username);
        query.executeUpdate();
    }
}
