package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(Objects.requireNonNull(id));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAllUsersNewestFirst() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public User createUser(User user) {
        return userRepository.save(Objects.requireNonNull(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(Objects.requireNonNull(id));
    }
}