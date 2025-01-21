package com.tutorial.dao;

import java.util.List;
import com.tutorial.modelchain.User;

public interface UserDAO {
    User save(User user);
    List<User> findAll();
    List<User> findByName(String name);
    void deleteById(Long id);
    User findById(Long id);
    void updateUserName(Long id, String newName);
}

