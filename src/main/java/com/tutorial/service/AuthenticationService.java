package com.tutorial.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.tutorial.model.LoginUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AuthenticationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    private RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public boolean authenticateAndStoreToken(String username, String password, HttpServletResponse response) {
        try {
            String url = "http://localhost:9081/api/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> payload = Map.of("username", username, "password", password);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> authResponse = restTemplate.postForEntity(new URI(url), request, Map.class);
            Map<String, Object> responseBody = authResponse.getBody();
            
            logger.info("Authentication response: {}", responseBody);
            
            if (responseBody != null && responseBody.containsKey("token")) {
                String token = responseBody.get("token").toString();
                Cookie cookie = new Cookie("auth_token", token);
                // security settings
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setPath("/");
                cookie.setMaxAge(5);
                response.addCookie(cookie);
                logger.info("Token set in cookie: {}", token);
                return true;
            }
            logger.warn("Authentication failed for user: {}", username);
            return false;
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", username, e);
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            String url = "http://localhost:9081/api/auth/validate?token=" + token;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Boolean> response = restTemplate.exchange(new URI(url), HttpMethod.GET, request, Boolean.class);
            
            logger.info("Token validation response status: {}", response.getStatusCode());
            
            return response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            logger.error("Error during token validation", e);
            return false;
        }
    }

    public String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // abandoned
    public boolean authenticateFromDB(String username, String password) {
        // naive check for admin credentials
        if ("admin".equals(username) && "admin".equals(password)) {
            return true;
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
