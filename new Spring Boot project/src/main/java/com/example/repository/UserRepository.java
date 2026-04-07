package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByOrderByCreatedAtDesc();
    long countByIsActiveTrue();
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
=======
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
>>>>>>> copilot/worktree-2026-04-06T05-00-30
}