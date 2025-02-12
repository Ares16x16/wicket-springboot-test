package com.tutorial.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import com.tutorial.entity.User;

@Repository
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }
    @Override
    @Cacheable("users")
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }

    @Override
    public List<User> findByName(String name) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findByName", User.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    @Override
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public void deleteById(Long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    // Cache user by id.
    @Override
    @Cacheable(value = "user", key = "#id")
    public User findById(Long id) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findById", User.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    // Clear caches when updating a user.
    @Override
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public void updateUserName(Long id, String newName) {
        User user = findById(id);
        if (user != null) {
            user.setName(newName);
            entityManager.merge(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
}

