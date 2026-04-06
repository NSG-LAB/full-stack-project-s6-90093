package com.example.controller;

import com.example.model.EnhancementChecklist;
import com.example.repository.EnhancementChecklistRepository;
import com.example.service.EnhancementChecklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/enhancement-checklist")
public class EnhancementChecklistController {

    private final EnhancementChecklistService enhancementChecklistService;
    private final EnhancementChecklistRepository enhancementChecklistRepository;

    public EnhancementChecklistController(
            EnhancementChecklistService enhancementChecklistService,
            EnhancementChecklistRepository enhancementChecklistRepository
    ) {
        this.enhancementChecklistService = enhancementChecklistService;
        this.enhancementChecklistRepository = enhancementChecklistRepository;
    }

    @DeleteMapping("/file/{itemId}")
    public ResponseEntity<?> deleteUploadedFile(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> payload
    ) {
        try {
            String url = payload.get("url");
            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file URL provided"));
            }

            if (itemId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No item ID provided"));
            }
            Optional<EnhancementChecklist> optional = enhancementChecklistRepository.findById(itemId);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Checklist item not found"));
            }

            EnhancementChecklist item = optional.get();
            List<String> existingUrls = new ArrayList<>(enhancementChecklistService.parseAttachmentUrls(item.getAttachmentUrlsJson()));
            List<String> updatedUrls = existingUrls.stream().filter(existing -> !existing.equals(url)).toList();
            item.setAttachmentUrlsJson(enhancementChecklistService.toJson(updatedUrls));
            enhancementChecklistRepository.save(Objects.requireNonNull(item));

            if (url.startsWith("/uploads/")) {
                String relative = url.substring("/uploads/".length());
                Path filePath = Paths.get("uploads").resolve(relative).normalize();
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ignored) {
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "urls", updatedUrls
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<?> uploadChecklistPhotos(
            @PathVariable Long id,
            @RequestParam("photos") List<MultipartFile> photos
    ) {
        try {
            if (photos == null || photos.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No files uploaded"));
            }

            if (id == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No item ID provided"));
            }
            Optional<EnhancementChecklist> optional = enhancementChecklistRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Checklist item not found"));
            }

            EnhancementChecklist item = optional.get();
            List<String> urls = new ArrayList<>(enhancementChecklistService.parseAttachmentUrls(item.getAttachmentUrlsJson()));

            Path uploadDir = Paths.get("uploads", "checklist");
            Files.createDirectories(uploadDir);

            for (MultipartFile photo : photos) {
                if (photo.isEmpty()) {
                    continue;
                }

                String originalFilename = photo.getOriginalFilename();
                if (originalFilename == null) {
                    continue;
                }
                String filename = StringUtils.cleanPath(originalFilename);
                Path destination = uploadDir.resolve(filename).normalize();

                Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                urls.add("/uploads/checklist/" + filename);
            }

            item.setAttachmentUrlsJson(enhancementChecklistService.toJson(urls));
            enhancementChecklistRepository.save(Objects.requireNonNull(item));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "urls", urls
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createChecklistItem(@RequestBody Map<String, Object> payload) {
        try {
            EnhancementChecklist created = enhancementChecklistService.createChecklistItem(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{propertyId}/{type}")
    public ResponseEntity<?> getChecklistItems(
            @PathVariable Long propertyId,
            @PathVariable String type
    ) {
        try {
            if (propertyId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No property ID provided"));
            }
            List<Map<String, Object>> rows = enhancementChecklistService.getChecklistItems(propertyId, type)
                    .stream()
                    .map(this::toResponse)
                    .toList();
            return ResponseEntity.ok(rows);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChecklistItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No item ID provided"));
            }
            EnhancementChecklist updated = enhancementChecklistService.updateChecklistItem(id, payload);
            return ResponseEntity.ok(toResponse(updated));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChecklistItem(@PathVariable Long id) {
        try {
            enhancementChecklistService.deleteChecklistItem(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private Map<String, Object> toResponse(EnhancementChecklist item) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", item.getId());
        response.put("propertyId", item.getPropertyId());
        response.put("userId", item.getUserId());
        response.put("type", item.getType());
        response.put("item", item.getItem());
        response.put("completed", item.getCompleted());
        response.put("notes", item.getNotes());
        response.put("attachmentUrls", enhancementChecklistService.parseAttachmentUrls(item.getAttachmentUrlsJson()));
        response.put("createdAt", item.getCreatedAt());
        response.put("updatedAt", item.getUpdatedAt());
        return response;
    }
}
