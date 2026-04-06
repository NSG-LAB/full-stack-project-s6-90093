package com.example.controller;

import com.example.model.Notification;
import com.example.model.User;
import com.example.repository.NotificationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) String unreadOnly,
            @RequestParam(required = false) String dueOnly
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        boolean unread = "true".equalsIgnoreCase(unreadOnly);
        boolean due = "true".equalsIgnoreCase(dueOnly);

        List<Notification> notifications;
        if (unread && due) {
            notifications = notificationRepository.findByOwnerIdAndIsReadAndDueAtLessThanEqualOrderByCreatedAtDesc(
                    principal.userId(),
                    false,
                    LocalDateTime.now()
            );
        } else if (unread) {
            notifications = notificationRepository.findByOwnerIdAndIsReadOrderByCreatedAtDesc(principal.userId(), false);
        } else if (due) {
            notifications = notificationRepository.findByOwnerIdAndDueAtLessThanEqualOrderByCreatedAtDesc(
                    principal.userId(),
                    LocalDateTime.now()
            );
        } else {
            notifications = notificationRepository.findByOwnerIdOrderByCreatedAtDesc(principal.userId());
        }

        List<Map<String, Object>> response = notifications.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", response.size(),
                "notifications", response
        ));
    }

    @PostMapping
    public ResponseEntity<?> createNotification(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody Map<String, Object> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        List<Map<String, String>> errors = validateCreate(payload);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", errors
            ));
        }

        Optional<User> owner = userRepository.findById(java.util.Objects.requireNonNull(principal.userId()));
        if (owner.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Notification notification = new Notification();
        notification.setOwner(owner.get());
        notification.setTitle(payload.get("title").toString());
        notification.setMessage(payload.get("message").toString());
        notification.setDueAt(parseDueAt(payload.get("dueAt")));

        String type = payload.containsKey("type") && payload.get("type") != null && !payload.get("type").toString().isBlank()
                ? payload.get("type").toString()
                : "reminder";
        notification.setType(type);

        Notification saved = notificationRepository.save(notification);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Reminder created successfully",
                "notification", toResponse(saved)
        ));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Notification> optional = notificationRepository.findByIdAndOwnerId(id, principal.userId());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Notification not found"
            ));
        }

        Notification notification = optional.get();
        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read",
                "notification", toResponse(updated)
        ));
    }

    private List<Map<String, String>> validateCreate(Map<String, Object> payload) {
        List<Map<String, String>> errors = new ArrayList<>();

        if (!payload.containsKey("title") || payload.get("title") == null || payload.get("title").toString().isBlank()) {
            errors.add(error("title", "title should not be empty"));
        }

        if (!payload.containsKey("message") || payload.get("message") == null || payload.get("message").toString().isBlank()) {
            errors.add(error("message", "message should not be empty"));
        }

        if (payload.containsKey("dueAt") && payload.get("dueAt") != null && !payload.get("dueAt").toString().isBlank()) {
            if (parseDueAt(payload.get("dueAt")) == null) {
                errors.add(error("dueAt", "dueAt must be an ISO-8601 date-time"));
            }
        }

        return errors;
    }

    private LocalDateTime parseDueAt(Object value) {
        if (value == null) {
            return null;
        }

        String text = value.toString();
        if (text.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, String> error(String path, String msg) {
        Map<String, String> error = new HashMap<>();
        error.put("path", path);
        error.put("msg", msg);
        return error;
    }

    private Map<String, Object> toResponse(Notification notification) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", notification.getId());
        result.put("userId", notification.getOwner() == null ? null : notification.getOwner().getId());
        result.put("title", notification.getTitle());
        result.put("message", notification.getMessage());
        result.put("dueAt", notification.getDueAt());
        result.put("type", notification.getType());
        result.put("isRead", notification.getIsRead());
        result.put("createdAt", notification.getCreatedAt());
        result.put("updatedAt", notification.getUpdatedAt());
        return result;
    }
}
