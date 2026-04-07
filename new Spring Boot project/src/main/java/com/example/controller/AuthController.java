package com.example.controller;

import com.example.model.User;
import com.example.security.JwtUtil;
<<<<<<< HEAD
import com.example.security.JwtPrincipal;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
=======
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
>>>>>>> copilot/worktree-2026-04-06T05-00-30
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> copilot/worktree-2026-04-06T05-00-30

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

<<<<<<< HEAD
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email already registered"
            ));
        }

        user.setUsername(user.getUsername() == null || user.getUsername().isBlank()
                ? user.getEmail()
                : user.getUsername());
        user.setRole((user.getRole() == null || user.getRole().isBlank()) ? "user" : user.getRole().toLowerCase());
        user.setFirstName(user.getFirstName() == null ? "" : user.getFirstName());
        user.setLastName(user.getLastName() == null ? "" : user.getLastName());
        user.setBio(user.getBio() == null ? "" : user.getBio());
        user.setIsActive(user.getIsActive() == null ? Boolean.TRUE : user.getIsActive());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User createdUser = userService.createUser(user);
        String token = jwtUtil.generateToken(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "User registered successfully",
                "token", token,
                "user", toPublicUser(createdUser)
        ));
=======
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User newUser = userService.createUser(user);
        return ResponseEntity.ok(newUser);
>>>>>>> copilot/worktree-2026-04-06T05-00-30
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
<<<<<<< HEAD
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid credentials"
            ));
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid credentials"
            ));
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "token", token,
                "user", toPublicUser(user)
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful. Remove token from client storage."
        ));
    }

    private Map<String, Object> toPublicUser(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("role", user.getRole());
        result.put("city", user.getCity());
        result.put("state", user.getState());
        result.put("profileImage", user.getProfileImage());
        result.put("bio", user.getBio());
        result.put("isActive", user.getIsActive());
        result.put("createdAt", user.getCreatedAt());
        result.put("updatedAt", user.getUpdatedAt());
        return result;
=======
        String username = credentials.get("username");
        String password = credentials.get("password");

        User user = userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(username);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
>>>>>>> copilot/worktree-2026-04-06T05-00-30
    }
}