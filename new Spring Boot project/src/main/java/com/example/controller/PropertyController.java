package com.example.controller;

import com.example.model.Property;
import com.example.model.User;
import com.example.repository.PropertyRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PropertyController(PropertyRepository propertyRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam("image") MultipartFile image
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file uploaded"));
        }

        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);

            String originalName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + originalName;
            Path destination = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Image uploaded successfully",
                    "filePath", "/uploads/" + fileName
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv() {
        List<Property> properties = propertyRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("id,userId,title,description,propertyType,age,builUpArea,bedrooms,bathrooms,location,condition,currentValue,features,images,improvements,estimatedNewValue,potentialValueIncrease,status,createdAt,updatedAt\n");

        for (Property property : properties) {
            csv.append(csvEscape(property.getId())).append(',')
                    .append(csvEscape(property.getOwner() == null ? null : property.getOwner().getId())).append(',')
                    .append(csvEscape(property.getTitle())).append(',')
                    .append(csvEscape(property.getDescription())).append(',')
                    .append(csvEscape(property.getPropertyType())).append(',')
                    .append(csvEscape(property.getAge())).append(',')
                    .append(csvEscape(property.getBuilUpArea())).append(',')
                    .append(csvEscape(property.getBedrooms())).append(',')
                    .append(csvEscape(property.getBathrooms())).append(',')
                    .append(csvEscape(property.getLocationJson())).append(',')
                    .append(csvEscape(property.getCondition())).append(',')
                    .append(csvEscape(property.getCurrentValue())).append(',')
                    .append(csvEscape(property.getFeaturesJson())).append(',')
                    .append(csvEscape(property.getImagesJson())).append(',')
                    .append(csvEscape(property.getImprovementsJson())).append(',')
                    .append(csvEscape(property.getEstimatedNewValue())).append(',')
                    .append(csvEscape(property.getPotentialValueIncrease())).append(',')
                    .append(csvEscape(property.getStatus())).append(',')
                    .append(csvEscape(property.getCreatedAt())).append(',')
                    .append(csvEscape(property.getUpdatedAt()))
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=properties_export.csv")
                .contentType(MediaType.valueOf("text/csv"))
                .body(csv.toString());
    }

    @PostMapping
    public ResponseEntity<?> createProperty(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody Map<String, Object> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Long userId = principal.userId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "User ID not found"));
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "User not found"));
        }

        Property property = new Property();
        property.setOwner(userOptional.get());
        applyFromPayload(property, payload);
        if (property.getStatus() == null || property.getStatus().isBlank()) {
            property.setStatus("pending");
        }

        Property saved = propertyRepository.save(Objects.requireNonNull(property));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Property submitted successfully",
                "property", toPropertyResponse(saved)
        ));
    }

    @GetMapping
    public ResponseEntity<?> getAllProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "10") String limit,
            @RequestParam(defaultValue = "0") String offset,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        int parsedLimit = Math.min(Math.max(parseInt(limit, 10), 1), 100);
        int parsedOffset = Math.max(parseInt(offset, 0), 0);

        String safeSortBy = switch (sortBy) {
            case "updatedAt", "currentValue", "title", "status" -> sortBy;
            default -> "createdAt";
        };

        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        List<Property> sorted = propertyRepository.findAll(Sort.by(direction, safeSortBy));

        List<Property> filtered = sorted.stream()
                .filter(p -> city == null || city.isBlank() || (p.getLocationCity() != null && p.getLocationCity().equalsIgnoreCase(city)))
                .filter(p -> propertyType == null || propertyType.isBlank() || propertyType.equalsIgnoreCase(p.getPropertyType()))
                .filter(p -> status == null || status.isBlank() || status.equalsIgnoreCase(p.getStatus()))
                .filter(p -> {
                    if (q == null || q.isBlank()) {
                        return true;
                    }
                    String needle = q.toLowerCase(Locale.ROOT);
                    String titleValue = p.getTitle() == null ? "" : p.getTitle().toLowerCase(Locale.ROOT);
                    String descriptionValue = p.getDescription() == null ? "" : p.getDescription().toLowerCase(Locale.ROOT);
                    return titleValue.contains(needle) || descriptionValue.contains(needle);
                })
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(parsedOffset, total);
        int toIndex = Math.min(fromIndex + parsedLimit, total);

        List<Map<String, Object>> properties = filtered.subList(fromIndex, toIndex)
                .stream()
                .map(this::toPropertyResponse)
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", total,
                "limit", parsedLimit,
                "offset", parsedOffset,
                "hasMore", toIndex < total,
                "properties", properties
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        Optional<Property> propertyOptional = propertyRepository.findById(Objects.requireNonNull(id));
        if (propertyOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Property not found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "property", toPropertyResponse(Objects.requireNonNull(propertyOptional.get()))
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Property> propertyOptional = propertyRepository.findById(Objects.requireNonNull(id));
        if (propertyOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Property not found"
            ));
        }

        Property property = propertyOptional.get();
        if (!isOwnerOrAdmin(property, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Not authorized to update this property"
            ));
        }

        applyFromPayload(property, payload);
        Property saved = propertyRepository.save(Objects.requireNonNull(property));

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Property updated successfully",
                "property", toPropertyResponse(saved)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Optional<Property> propertyOptional = propertyRepository.findById(Objects.requireNonNull(id));
        if (propertyOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Property not found"
            ));
        }

        Property property = propertyOptional.get();
        if (!isOwnerOrAdmin(property, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Not authorized to delete this property"
            ));
        }

        propertyRepository.delete(Objects.requireNonNull(property));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Property deleted successfully"
        ));
    }

    private boolean isOwnerOrAdmin(Property property, JwtPrincipal principal) {
        Long ownerId = property.getOwner() == null ? null : Objects.requireNonNull(property.getOwner()).getId();
        return ownerId != null && ownerId.equals(principal.userId())
                || "admin".equalsIgnoreCase(principal.role());
    }

    private void applyFromPayload(Property property, Map<String, Object> payload) {
        if (payload.containsKey("title")) {
            property.setTitle(toStringValue(payload.get("title")));
        }
        if (payload.containsKey("description")) {
            property.setDescription(toStringValue(payload.get("description")));
        }
        if (payload.containsKey("propertyType")) {
            property.setPropertyType(toStringValue(payload.get("propertyType")));
        }
        if (payload.containsKey("age")) {
            property.setAge(toInteger(payload.get("age")));
        }
        if (payload.containsKey("builUpArea")) {
            property.setBuilUpArea(toInteger(payload.get("builUpArea")));
        }
        if (payload.containsKey("bedrooms")) {
            property.setBedrooms(toInteger(payload.get("bedrooms")));
        }
        if (payload.containsKey("bathrooms")) {
            property.setBathrooms(toInteger(payload.get("bathrooms")));
        }
        if (payload.containsKey("condition")) {
            property.setCondition(toStringValue(payload.get("condition")));
        }
        if (payload.containsKey("currentValue")) {
            property.setCurrentValue(toBigDecimal(payload.get("currentValue")));
        }
        if (payload.containsKey("estimatedNewValue")) {
            property.setEstimatedNewValue(toBigDecimal(payload.get("estimatedNewValue")));
        }
        if (payload.containsKey("potentialValueIncrease")) {
            property.setPotentialValueIncrease(toBigDecimal(payload.get("potentialValueIncrease")));
        }
        if (payload.containsKey("status")) {
            property.setStatus(toStringValue(payload.get("status")));
        }
        if (payload.containsKey("location")) {
            Object location = payload.get("location");
            property.setLocationJson(toJson(location));
            if (location instanceof Map<?, ?> map && map.containsKey("city")) {
                property.setLocationCity(toStringValue(map.get("city")));
            }
        }
        if (payload.containsKey("features")) {
            property.setFeaturesJson(toJson(payload.get("features")));
        }
        if (payload.containsKey("images")) {
            property.setImagesJson(toJson(payload.get("images")));
        }
        if (payload.containsKey("improvements")) {
            property.setImprovementsJson(toJson(payload.get("improvements")));
        }
    }

    private Map<String, Object> toPropertyResponse(Property property) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", property.getId());
        result.put("userId", property.getOwner() == null ? null : property.getOwner().getId());
        result.put("title", property.getTitle());
        result.put("description", property.getDescription());
        result.put("propertyType", property.getPropertyType());
        result.put("age", property.getAge());
        result.put("builUpArea", property.getBuilUpArea());
        result.put("bedrooms", property.getBedrooms());
        result.put("bathrooms", property.getBathrooms());
        result.put("location", fromJson(property.getLocationJson()));
        result.put("condition", property.getCondition());
        result.put("currentValue", property.getCurrentValue());
        result.put("features", fromJson(property.getFeaturesJson()));
        result.put("images", fromJson(property.getImagesJson()));
        result.put("improvements", fromJson(property.getImprovementsJson()));
        result.put("estimatedNewValue", property.getEstimatedNewValue());
        result.put("potentialValueIncrease", property.getPotentialValueIncrease());
        result.put("status", property.getStatus());
        result.put("createdAt", property.getCreatedAt());
        result.put("updatedAt", property.getUpdatedAt());

        if (property.getOwner() != null) {
            Map<String, Object> owner = new HashMap<>();
            owner.put("id", property.getOwner().getId());
            owner.put("firstName", property.getOwner().getFirstName());
            owner.put("lastName", property.getOwner().getLastName());
            owner.put("email", property.getOwner().getEmail());
            result.put("owner", owner);
        }

        return result;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Object fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
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
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String csvEscape(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
