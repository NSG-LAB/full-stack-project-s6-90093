package com.example.controller;

import com.example.model.Recommendation;
import com.example.repository.RecommendationRepository;
import com.example.service.RoiPlannerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/roi")
public class RoiController {

    private static final List<String> ALLOWED_CONDITIONS = List.of("excellent", "good", "average", "needs-work");

    private final RecommendationRepository recommendationRepository;
    private final RoiPlannerService roiPlannerService;
    private final ObjectMapper objectMapper;

    public RoiController(
            RecommendationRepository recommendationRepository,
            RoiPlannerService roiPlannerService,
            ObjectMapper objectMapper
    ) {
        this.recommendationRepository = recommendationRepository;
        this.roiPlannerService = roiPlannerService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/plan")
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> payload) {
        List<Map<String, String>> errors = validate(payload);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "errors", errors
            ));
        }

        double budget = toDouble(payload.getOrDefault("budget", 0));
        String propertyType = toStringValue(payload.get("propertyType"));
        String propertyCondition = normalizeCondition(toStringValue(payload.get("propertyCondition")));
        String city = toStringValue(payload.get("city"));
        int topN = toInt(payload.getOrDefault("topN", 5), 5);

        List<Recommendation> recommendations = recommendationRepository.findByIsActiveTrue(
                Sort.by(Sort.Direction.DESC, "priority")
        );

        List<Recommendation> filtered = recommendations.stream()
                .filter(rec -> matchesPropertyType(rec, propertyType))
                .filter(rec -> matchesPropertyCondition(rec, propertyCondition))
                .filter(rec -> matchesCity(rec, city))
                .toList();

        Map<String, Object> plan = roiPlannerService.createPlan(filtered, budget, propertyCondition, topN);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ROI plan generated",
                "plan", plan
        ));
    }

    private List<Map<String, String>> validate(Map<String, Object> payload) {
        List<Map<String, String>> errors = new ArrayList<>();

        if (payload.containsKey("budget") && toDouble(payload.get("budget")) < 0) {
            errors.add(error("budget", "budget must be greater than or equal to 0"));
        }

        if (payload.containsKey("propertyCondition")) {
            String condition = normalizeCondition(toStringValue(payload.get("propertyCondition")));
            if (!ALLOWED_CONDITIONS.contains(condition)) {
                errors.add(error("propertyCondition", "propertyCondition must be one of excellent, good, average, needs-work"));
            }
        }

        if (payload.containsKey("topN")) {
            int topN = toInt(payload.get("topN"), -1);
            if (topN < 1 || topN > 10) {
                errors.add(error("topN", "topN must be an integer between 1 and 10"));
            }
        }

        return errors;
    }

    private boolean matchesPropertyType(Recommendation recommendation, String propertyType) {
        if (propertyType == null || propertyType.isBlank()) {
            return true;
        }

        List<String> types = toStringList(recommendation.getApplicablePropertyTypesJson());
        return types.isEmpty()
                || containsIgnoreCase(types, "all")
                || containsIgnoreCase(types, propertyType);
    }

    private boolean matchesPropertyCondition(Recommendation recommendation, String propertyCondition) {
        List<String> conditions = toStringList(recommendation.getApplicableConditionsJson());
        return conditions.isEmpty() || containsIgnoreCase(conditions, propertyCondition);
    }

    private boolean matchesCity(Recommendation recommendation, String city) {
        if (city == null || city.isBlank()) {
            return true;
        }

        List<String> cities = toStringList(recommendation.getApplicableCitiesJson());
        return cities.isEmpty() || containsIgnoreCase(cities, city);
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return values.stream().anyMatch(v -> v != null && v.equalsIgnoreCase(candidate));
    }

    private List<String> toStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            if (parsed instanceof List<?> list) {
                return list.stream()
                        .map(this::toStringValue)
                        .filter(v -> v != null && !v.isBlank())
                        .toList();
            }
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<String, String> error(String path, String msg) {
        Map<String, String> error = new HashMap<>();
        error.put("path", path);
        error.put("msg", msg);
        return error;
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String normalizeCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            return "good";
        }
        return condition.toLowerCase(Locale.ROOT);
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
