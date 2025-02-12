package com.tutorial.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import com.tutorial.model.LoginUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AuthenticationService {

    private RestTemplate restTemplate = new RestTemplate();

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    public boolean authenticateAndStoreToken(String username, String password, HttpServletResponse response) {
        logger.debug("authenticateAndStoreToken() called with username: {}", username);
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
    
    @CacheEvict(value = "loginUsers", allEntries = true)
    public void createAccountInDB(String username, String password) {
        try {
            String url = "http://localhost:9081/api/auth/signup";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> payload = Map.of("username", username, "password", password);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Signup successful for user: {}", username);
            } else {
                logger.warn("Signup failed for user: {}. Status: {}", username, response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("API error during signup for user {}: {}", username, e.getStatusCode());
        } catch (Exception e) {
            logger.error("Exception during signup for user: {}", username, e);
        }
    }

    @Cacheable("loginUsers")
    public List<LoginUser> getAllUsers() {
        try {
            String url = "http://localhost:9081/api/auth/users";
            ResponseEntity<LoginUser[]> response = restTemplate.getForEntity(url, LoginUser[].class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Fetched {} users from auth API", response.getBody().length);
                return Arrays.asList(response.getBody());
            }
            logger.warn("Failed to fetch users. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Error fetching users from auth API", e);
        }
        return List.of();
    }

    @CacheEvict(value = "loginUsers", allEntries = true)
    public void deleteUser(String username) {
        try {
            String url = "http://localhost:9081/api/auth/user/{username}";
            restTemplate.delete(url, username);
            logger.info("Deleted user {} via API", username);
        } catch (HttpClientErrorException e) {
            logger.error("API error when deleting user {}: {}", username, e.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to delete user {} via API", username, e);
        }
    }
}
