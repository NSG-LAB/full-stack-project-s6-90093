package com.example.controller;

import com.example.model.Notification;
import com.example.model.Property;
import com.example.model.Recommendation;
import com.example.model.User;
import com.example.repository.NotificationRepository;
import com.example.repository.PropertyRepository;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RecommendationRepository recommendationRepository;
    private final NotificationRepository notificationRepository;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public AnalyticsController(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            RecommendationRepository recommendationRepository,
            NotificationRepository notificationRepository,
            DataSource dataSource,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.recommendationRepository = recommendationRepository;
        this.notificationRepository = notificationRepository;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> overview(@AuthenticationPrincipal JwtPrincipal principal) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long newUsers = userRepository.countByCreatedAtBetween(start, end);

        long totalProperties = propertyRepository.count();
        long newProperties = propertyRepository.countByCreatedAtBetween(start, end);

        long totalRecommendations = recommendationRepository.count();
        long activeRecommendations = recommendationRepository.countByIsActiveTrue();

        long totalNotifications = notificationRepository.count();
        long unreadNotifications = notificationRepository.countByIsReadFalse();

        List<Map<String, Object>> userRoles = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        user -> nullSafe(user.getRole(), "unknown"),
                        Collectors.counting()
                ))
                .entrySet().stream()
            .map(entry -> {
                Map<String, Object> row = new HashMap<>();
                row.put("role", entry.getKey());
                row.put("count", entry.getValue());
                return row;
            })
                .toList();

        List<Map<String, Object>> propertyTypes = propertyRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        property -> nullSafe(property.getPropertyType(), "unknown"),
                        Collectors.counting()
                ))
                .entrySet().stream()
            .map(entry -> {
                Map<String, Object> row = new HashMap<>();
                row.put("type", entry.getKey());
                row.put("count", entry.getValue());
                return row;
            })
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "users", Map.of("total", totalUsers, "active", activeUsers, "new", newUsers),
                        "properties", Map.of("total", totalProperties, "new", newProperties),
                        "recommendations", Map.of("total", totalRecommendations, "active", activeRecommendations),
                        "notifications", Map.of("total", totalNotifications, "unread", unreadNotifications),
                        "distributions", Map.of("userRoles", userRoles, "propertyTypes", propertyTypes)
                )
        ));
    }

    @GetMapping("/user-activity")
    public ResponseEntity<?> userActivity(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "30") String period
    ) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        int days = parsePositiveInt(period, 30);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        List<User> users = userRepository.findByCreatedAtBetween(start, end);
        List<Property> properties = propertyRepository.findByCreatedAtBetween(start, end);

        List<Map<String, Object>> userRegistrations = groupByDate(users.stream()
                .filter(user -> user.getCreatedAt() != null)
                .map(User::getCreatedAt)
                .toList());

        List<Map<String, Object>> propertySubmissions = groupByDate(properties.stream()
                .filter(property -> property.getCreatedAt() != null)
                .map(Property::getCreatedAt)
                .toList());

        Map<Long, Long> propertyCountByUser = properties.stream()
                .map(Property::getOwner)
                .filter(Objects::nonNull)
                .map(User::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        Map<Long, User> usersById = userRepository.findAllById(propertyCountByUser.keySet()).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Map<String, Object>> topUsers = propertyCountByUser.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    User user = usersById.get(entry.getKey());
                    String name = user == null
                            ? "Unknown User"
                            : (nullSafe(user.getFirstName(), "") + " " + nullSafe(user.getLastName(), "")).trim();
                    if (name.isBlank()) {
                        name = "Unknown User";
                    }

                    Map<String, Object> row = new HashMap<>();
                    row.put("id", entry.getKey());
                    row.put("name", name);
                    row.put("email", user == null ? null : user.getEmail());
                    row.put("propertyCount", entry.getValue());
                    return row;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "userRegistrations", userRegistrations,
                        "propertySubmissions", propertySubmissions,
                        "topUsers", topUsers
                )
        ));
    }

    @GetMapping("/properties")
    public ResponseEntity<?> properties(@AuthenticationPrincipal JwtPrincipal principal) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        List<Property> all = propertyRepository.findAll();

        List<Map<String, Object>> statusDistribution = all.stream()
                .collect(Collectors.groupingBy(
                        property -> nullSafe(property.getStatus(), "unknown"),
                        Collectors.counting()
                ))
                .entrySet().stream()
            .map(entry -> {
                Map<String, Object> row = new HashMap<>();
                row.put("status", entry.getKey());
                row.put("count", entry.getValue());
                return row;
            })
                .toList();

        List<Map<String, Object>> locationDistribution = all.stream()
                .map(this::toLocationKey)
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.groupingBy(key -> key, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
            .map(entry -> {
                Map<String, Object> row = new HashMap<>();
                row.put("location", entry.getKey());
                row.put("count", entry.getValue());
                return row;
            })
                .toList();

        List<Map<String, Object>> averageValues = all.stream()
                .filter(property -> property.getCurrentValue() != null)
                .collect(Collectors.groupingBy(
                        property -> nullSafe(property.getPropertyType(), "unknown")
                ))
                .entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    List<Property> properties = entry.getValue();
                    double avg = properties.stream()
                            .map(Property::getCurrentValue)
                            .mapToDouble(BigDecimal::doubleValue)
                            .average()
                            .orElse(0);
                    Map<String, Object> row = new HashMap<>();
                    row.put("type", type);
                    row.put("avgValue", avg);
                    row.put("count", properties.size());
                    return row;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "statusDistribution", statusDistribution,
                        "locationDistribution", locationDistribution,
                        "averageValues", averageValues
                )
        ));
    }

    @GetMapping("/performance")
    public ResponseEntity<?> performance(@AuthenticationPrincipal JwtPrincipal principal) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryUsage = new HashMap<>();
        memoryUsage.put("heapUsed", runtime.totalMemory() - runtime.freeMemory());
        memoryUsage.put("heapTotal", runtime.totalMemory());
        memoryUsage.put("heapMax", runtime.maxMemory());

        Map<String, Object> system = new HashMap<>();
        system.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0);
        system.put("memoryUsage", memoryUsage);
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("os", System.getProperty("os.name"));

        String dbStatus = "disconnected";
        try (Connection ignored = dataSource.getConnection()) {
            dbStatus = "connected";
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "system", system,
                        "database", Map.of("status", dbStatus, "dialect", "mysql"),
                        "cache", Map.of("status", "connected")
                )
        ));
    }

    private ResponseEntity<?> requireAdmin(JwtPrincipal principal) {
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
        return null;
    }

    private List<Map<String, Object>> groupByDate(List<LocalDateTime> dateTimes) {
        Map<LocalDate, Long> grouped = dateTimes.stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, LinkedHashMap::new, Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", entry.getKey().toString());
                    row.put("count", entry.getValue());
                    return row;
                })
                .toList();
    }

    private String toLocationKey(Property property) {
        String city = property.getLocationCity();
        String state = null;

        if (property.getLocationJson() != null && !property.getLocationJson().isBlank()) {
            try {
                Object parsed = objectMapper.readValue(property.getLocationJson(), Object.class);
                if (parsed instanceof Map<?, ?> map) {
                    Object cityValue = map.get("city");
                    if ((city == null || city.isBlank()) && cityValue != null) {
                        city = cityValue.toString();
                    }
                    Object stateValue = map.get("state");
                    if (stateValue != null) {
                        state = stateValue.toString();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (city == null || city.isBlank()) {
            return null;
        }
        if (state == null || state.isBlank()) {
            return city;
        }
        return city + ", " + state;
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String nullSafe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
