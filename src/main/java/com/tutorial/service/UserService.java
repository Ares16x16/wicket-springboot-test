package com.tutorial.service;

import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tutorial.dao.UserDAO;
import com.tutorial.entity.User;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @PostConstruct
    public void init() {
        if (getAllUsers().isEmpty()) {
            createUser("Initial User", "initial@example.com");
            createUser("User 1", "1@example.com");
            createUser("User 2", "2@example.com");
            createUser("User 3", "3@example.com");
        }
    }

    public User createUser(String name, String email) {
        return userDAO.save(new User(name, email));
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getUsersByName(String name) {
        return userDAO.findByName(name);
    }

    public void deleteUser(Long id) {
        userDAO.deleteById(id);
    }

    public void updateUserName(Long id, String newName) {
        userDAO.updateUserName(id, newName);
    }
}