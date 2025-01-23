//JPA Repository for User entity
//Unused
package com.tutorial.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorial.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}