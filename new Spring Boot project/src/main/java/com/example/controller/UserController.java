package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        Optional<User> userOptional = userRepository.findById(principal.userId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", toPublicUser(userOptional.get())
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody Map<String, String> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        Optional<User> userOptional = userRepository.findById(principal.userId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        User user = userOptional.get();
        user.setFirstName(payload.getOrDefault("firstName", user.getFirstName()));
        user.setLastName(payload.getOrDefault("lastName", user.getLastName()));
        user.setPhone(payload.getOrDefault("phone", user.getPhone()));
        user.setCity(payload.getOrDefault("city", user.getCity()));
        user.setState(payload.getOrDefault("state", user.getState()));
        user.setBio(payload.getOrDefault("bio", user.getBio()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "user", toPublicUser(user)
        ));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal JwtPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        if (!"admin".equalsIgnoreCase(principal.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Admin access required"
            ));
        }

        List<Map<String, Object>> users = userService.getAllUsersNewestFirst()
                .stream()
                .map(this::toPublicUser)
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", users.size(),
                "users", users
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
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
    }
}