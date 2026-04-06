package com.example.controller;

import com.example.model.Property;
import com.example.model.Recommendation;
import com.example.repository.PropertyRepository;
import com.example.repository.RecommendationRepository;
import com.example.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "kitchen-bathroom",
            "flooring",
            "wall-paint",
            "lighting-fixtures",
            "garden-outdoor",
            "safety-security",
            "energy-efficiency",
            "interior-design",
            "electrical-plumbing"
    );

    private static final Set<String> ALLOWED_DIFFICULTIES = Set.of("easy", "moderate", "difficult");

    private final RecommendationRepository recommendationRepository;
    private final PropertyRepository propertyRepository;
    private final ObjectMapper objectMapper;

    public RecommendationController(
            RecommendationRepository recommendationRepository,
            PropertyRepository propertyRepository,
            ObjectMapper objectMapper
    ) {
        this.recommendationRepository = recommendationRepository;
        this.propertyRepository = propertyRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> getRecommendations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "10") String limit,
            @RequestParam(defaultValue = "0") String offset,
            @RequestParam(defaultValue = "priority") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        int parsedLimit = Math.min(Math.max(parseInt(limit, 10), 1), 100);
        int parsedOffset = Math.max(parseInt(offset, 0), 0);

        String safeSortBy = switch (sortBy) {
            case "title", "difficulty", "createdAt", "updatedAt", "priority" -> sortBy;
            case "expectedROI" -> "expectedRoi";
            default -> "priority";
        };

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;

        List<Recommendation> allActive = recommendationRepository.findByIsActiveTrue(Sort.by(direction, safeSortBy));
        List<Recommendation> filtered = allActive.stream()
                .filter(rec -> category == null || category.isBlank() || category.equalsIgnoreCase(rec.getCategory()))
                .filter(rec -> difficulty == null || difficulty.isBlank() || difficulty.equalsIgnoreCase(rec.getDifficulty()))
                .filter(rec -> {
                    if (q == null || q.isBlank()) {
                        return true;
                    }
                    String needle = q.toLowerCase(Locale.ROOT);
                    String title = rec.getTitle() == null ? "" : rec.getTitle().toLowerCase(Locale.ROOT);
                    return title.contains(needle);
                })
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(parsedOffset, total);
        int toIndex = Math.min(fromIndex + parsedLimit, total);
        List<Recommendation> paged = filtered.subList(fromIndex, toIndex);

        Map<Long, Recommendation> relatedById = loadRelatedRecommendations(paged);
        List<Map<String, Object>> responseRows = paged.stream()
                .map(recommendation -> toResponse(recommendation, true, relatedById))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", total,
                "limit", parsedLimit,
                "offset", parsedOffset,
                "hasMore", toIndex < total,
                "recommendations", responseRows
        ));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<?> getRecommendationsForProperty(@PathVariable Long propertyId) {
        Optional<Property> propertyOptional = propertyRepository.findById(propertyId);
        if (propertyOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Property not found"
            ));
        }

        Property property = propertyOptional.get();
        List<Recommendation> allActive = recommendationRepository.findByIsActiveTrue(Sort.by(Sort.Direction.DESC, "priority"));

        List<Map<String, Object>> filtered = allActive.stream()
                .filter(rec -> matchesProperty(rec, property))
                .map(rec -> toResponse(rec, false, Map.of()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", filtered.size(),
                "recommendations", filtered
        ));
    }

    @PostMapping
    public ResponseEntity<?> createRecommendation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody Map<String, Object> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        if (!isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Admin access required"));
        }

        List<Map<String, String>> errors = validateCreate(payload);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", errors
            ));
        }

        Recommendation recommendation = new Recommendation();
        applyDefaults(recommendation);
        applyPayload(recommendation, payload);
        recommendation.setCreatedBy(principal.userId());
        recommendation.setRelatedRecommendationIdsJson(toJson(extractRelatedIds(payload)));

        Recommendation saved = recommendationRepository.save(recommendation);
        Map<Long, Recommendation> relatedById = loadRelatedRecommendations(List.of(saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Recommendation created successfully",
                "recommendation", toResponse(saved, true, relatedById)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecommendation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        if (!isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Admin access required"));
        }

        Optional<Recommendation> recommendationOptional = recommendationRepository.findById(id);
        if (recommendationOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Recommendation not found"
            ));
        }

        Recommendation recommendation = recommendationOptional.get();
        applyPayload(recommendation, payload);

        if (payload.containsKey("relatedRecommendationIds") || payload.containsKey("relatedRecommendations")) {
            recommendation.setRelatedRecommendationIdsJson(toJson(extractRelatedIds(payload)));
        }

        Recommendation updated = recommendationRepository.save(recommendation);
        Map<Long, Recommendation> relatedById = loadRelatedRecommendations(List.of(updated));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recommendation updated successfully",
                "recommendation", toResponse(updated, true, relatedById)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecommendation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }
        if (!isAdmin(principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Admin access required"));
        }

        Optional<Recommendation> recommendationOptional = recommendationRepository.findById(id);
        if (recommendationOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Recommendation not found"
            ));
        }

        recommendationRepository.delete(recommendationOptional.get());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recommendation deleted successfully"
        ));
    }

    private boolean isAdmin(JwtPrincipal principal) {
        return "admin".equalsIgnoreCase(principal.role());
    }

    private boolean matchesProperty(Recommendation recommendation, Property property) {
        List<String> propertyTypes = toStringList(fromJson(recommendation.getApplicablePropertyTypesJson()));
        List<String> conditions = toStringList(fromJson(recommendation.getApplicableConditionsJson()));
        List<String> cities = toStringList(fromJson(recommendation.getApplicableCitiesJson()));

        boolean typeMatch = propertyTypes.isEmpty()
                || containsIgnoreCase(propertyTypes, "all")
                || containsIgnoreCase(propertyTypes, property.getPropertyType());

        boolean conditionMatch = conditions.isEmpty() || containsIgnoreCase(conditions, property.getCondition());

        String city = property.getLocationCity();
        boolean cityMatch;
        if (city == null || city.isBlank()) {
            cityMatch = cities.isEmpty();
        } else {
            cityMatch = cities.isEmpty() || containsIgnoreCase(cities, city);
        }

        return typeMatch && conditionMatch && cityMatch;
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return values.stream().anyMatch(value -> value != null && value.equalsIgnoreCase(candidate));
    }

    private List<Map<String, String>> validateCreate(Map<String, Object> payload) {
        List<Map<String, String>> errors = new ArrayList<>();

        String title = toStringValue(payload.get("title"));
        if (title == null || title.isBlank()) {
            errors.add(error("title", "title required"));
        }

        String category = toStringValue(payload.get("category"));
        if (category == null || !ALLOWED_CATEGORIES.contains(category)) {
            errors.add(error("category", "invalid category"));
        }

        String description = toStringValue(payload.get("description"));
        if (description == null || description.isBlank()) {
            errors.add(error("description", "description required"));
        }

        BigDecimal expectedRoi = toBigDecimal(payload.get("expectedROI"));
        if (expectedRoi == null || expectedRoi.compareTo(BigDecimal.ZERO) < 0 || expectedRoi.compareTo(BigDecimal.valueOf(500)) > 0) {
            errors.add(error("expectedROI", "expectedROI must be between 0 and 500"));
        }

        String difficulty = toStringValue(payload.get("difficulty"));
        if (difficulty == null || !ALLOWED_DIFFICULTIES.contains(difficulty)) {
            errors.add(error("difficulty", "invalid difficulty"));
        }

        return errors;
    }

    private Map<String, String> error(String path, String msg) {
        Map<String, String> result = new HashMap<>();
        result.put("path", path);
        result.put("msg", msg);
        return result;
    }

    private void applyDefaults(Recommendation recommendation) {
        recommendation.setBenefitsJson("[]");
        recommendation.setEstimatedCostJson("{\"min\":0,\"max\":0}");
        recommendation.setImagesJson("[]");
        recommendation.setTipsJson("[]");
        recommendation.setApplicablePropertyTypesJson("[]");
        recommendation.setApplicableCitiesJson("[]");
        recommendation.setApplicableConditionsJson("[]");
        recommendation.setBeforeAfterImagesJson("[]");
        recommendation.setRelatedRecommendationIdsJson("[]");
    }

    private void applyPayload(Recommendation recommendation, Map<String, Object> payload) {
        if (payload.containsKey("title")) {
            recommendation.setTitle(toStringValue(payload.get("title")));
        }
        if (payload.containsKey("category")) {
            recommendation.setCategory(toStringValue(payload.get("category")));
        }
        if (payload.containsKey("description")) {
            recommendation.setDescription(toStringValue(payload.get("description")));
        }
        if (payload.containsKey("benefits")) {
            recommendation.setBenefitsJson(toJson(payload.get("benefits")));
        }
        if (payload.containsKey("estimatedCost")) {
            recommendation.setEstimatedCostJson(toJson(payload.get("estimatedCost")));
        }
        if (payload.containsKey("expectedROI")) {
            recommendation.setExpectedRoi(toBigDecimal(payload.get("expectedROI")));
        }
        if (payload.containsKey("roiPercentage")) {
            recommendation.setRoiPercentage(toDouble(payload.get("roiPercentage")));
        }
        if (payload.containsKey("difficulty")) {
            recommendation.setDifficulty(toStringValue(payload.get("difficulty")));
        }
        if (payload.containsKey("timeframe")) {
            recommendation.setTimeframe(toStringValue(payload.get("timeframe")));
        }
        if (payload.containsKey("images")) {
            recommendation.setImagesJson(toJson(payload.get("images")));
        }
        if (payload.containsKey("tips")) {
            recommendation.setTipsJson(toJson(payload.get("tips")));
        }
        if (payload.containsKey("applicablePropertyTypes")) {
            recommendation.setApplicablePropertyTypesJson(toJson(payload.get("applicablePropertyTypes")));
        }
        if (payload.containsKey("applicableCities")) {
            recommendation.setApplicableCitiesJson(toJson(payload.get("applicableCities")));
        }
        if (payload.containsKey("applicableConditions")) {
            recommendation.setApplicableConditionsJson(toJson(payload.get("applicableConditions")));
        }
        if (payload.containsKey("beforeAfterImages")) {
            recommendation.setBeforeAfterImagesJson(toJson(payload.get("beforeAfterImages")));
        }
        if (payload.containsKey("priority")) {
            recommendation.setPriority(toInteger(payload.get("priority")));
        }
        if (payload.containsKey("isActive")) {
            recommendation.setIsActive(toBoolean(payload.get("isActive")));
        }
    }

    private Map<Long, Recommendation> asIdMap(List<Recommendation> recommendations) {
        return recommendations.stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(Recommendation::getId, r -> r, (left, right) -> left));
    }

    private Map<Long, Recommendation> loadRelatedRecommendations(List<Recommendation> recommendations) {
        Set<Long> relatedIds = new HashSet<>();
        for (Recommendation recommendation : recommendations) {
            relatedIds.addAll(extractRelatedIds(recommendation));
        }

        if (relatedIds.isEmpty()) {
            return Map.of();
        }

        return asIdMap(recommendationRepository.findAllById(relatedIds));
    }

    private List<Long> extractRelatedIds(Recommendation recommendation) {
        Object parsed = fromJson(recommendation.getRelatedRecommendationIdsJson());
        return toLongList(parsed);
    }

    private List<Long> extractRelatedIds(Map<String, Object> payload) {
        if (payload.get("relatedRecommendationIds") instanceof List<?>) {
            return toLongList(payload.get("relatedRecommendationIds"));
        }
        if (payload.get("relatedRecommendations") instanceof List<?>) {
            return toLongList(payload.get("relatedRecommendations"));
        }
        return List.of();
    }

    private Map<String, Object> toResponse(
            Recommendation recommendation,
            boolean includeRelated,
            Map<Long, Recommendation> relatedById
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", recommendation.getId());
        result.put("title", recommendation.getTitle());
        result.put("category", recommendation.getCategory());
        result.put("description", recommendation.getDescription());
        result.put("benefits", fromJson(recommendation.getBenefitsJson()));
        result.put("estimatedCost", fromJson(recommendation.getEstimatedCostJson()));
        result.put("expectedROI", recommendation.getExpectedRoi());
        result.put("roiPercentage", recommendation.getRoiPercentage());
        result.put("difficulty", recommendation.getDifficulty());
        result.put("timeframe", recommendation.getTimeframe());
        result.put("images", fromJson(recommendation.getImagesJson()));
        result.put("tips", fromJson(recommendation.getTipsJson()));
        result.put("applicablePropertyTypes", fromJson(recommendation.getApplicablePropertyTypesJson()));
        result.put("applicableCities", fromJson(recommendation.getApplicableCitiesJson()));
        result.put("applicableConditions", fromJson(recommendation.getApplicableConditionsJson()));
        result.put("beforeAfterImages", fromJson(recommendation.getBeforeAfterImagesJson()));
        result.put("priority", recommendation.getPriority());
        result.put("isActive", recommendation.getIsActive());
        result.put("createdBy", recommendation.getCreatedBy());
        result.put("createdAt", recommendation.getCreatedAt());
        result.put("updatedAt", recommendation.getUpdatedAt());

        if (includeRelated) {
            List<Map<String, Object>> related = extractRelatedIds(recommendation).stream()
                    .map(relatedById::get)
                    .filter(r -> r != null)
                    .map(this::toRelatedSummary)
                    .toList();
            result.put("relatedRecommendations", related);
        }

        return result;
    }

    private Map<String, Object> toRelatedSummary(Recommendation recommendation) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", recommendation.getId());
        summary.put("title", recommendation.getTitle());
        summary.put("category", recommendation.getCategory());
        summary.put("difficulty", recommendation.getDifficulty());
        summary.put("expectedROI", recommendation.getExpectedRoi());
        summary.put("priority", recommendation.getPriority());
        summary.put("isActive", recommendation.getIsActive());
        return summary;
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value.toString();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        return null;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(this::toStringValue).filter(v -> v != null && !v.isBlank()).toList();
    }

    private List<Long> toLongList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (Object item : list) {
            Long parsed = toLong(item);
            if (parsed != null) {
                ids.add(parsed);
            }
        }
        return ids;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof Map<?, ?> map && map.get("id") != null) {
            return toLong(map.get("id"));
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
