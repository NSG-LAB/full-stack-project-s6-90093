package com.example.service;

import com.example.model.EnhancementChecklist;
import com.example.repository.EnhancementChecklistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EnhancementChecklistService {

    private final EnhancementChecklistRepository enhancementChecklistRepository;
    private final ObjectMapper objectMapper;

    public EnhancementChecklistService(
            EnhancementChecklistRepository enhancementChecklistRepository,
            ObjectMapper objectMapper
    ) {
        this.enhancementChecklistRepository = enhancementChecklistRepository;
        this.objectMapper = objectMapper;
    }

    public EnhancementChecklist createChecklistItem(Map<String, Object> data) {
        EnhancementChecklist item = new EnhancementChecklist();
        applyPayload(item, data, true);
        return enhancementChecklistRepository.save(item);
    }

    public List<EnhancementChecklist> getChecklistItems(Long propertyId, String type) {
        return enhancementChecklistRepository.findByPropertyIdAndTypeOrderByCreatedAtAsc(propertyId, type);
    }

    public EnhancementChecklist updateChecklistItem(Long id, Map<String, Object> updates) {
        EnhancementChecklist existing = enhancementChecklistRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));
        applyPayload(existing, updates, false);
        return enhancementChecklistRepository.save(existing);
    }

    public void deleteChecklistItem(Long id) {
        if (!enhancementChecklistRepository.existsById(id)) {
            throw new IllegalArgumentException("Checklist item not found");
        }
        enhancementChecklistRepository.deleteById(id);
    }

    private void applyPayload(EnhancementChecklist item, Map<String, Object> payload, boolean create) {
        if (create && !payload.containsKey("propertyId")) {
            throw new IllegalArgumentException("propertyId is required");
        }
        if (create && !payload.containsKey("userId")) {
            throw new IllegalArgumentException("userId is required");
        }
        if (create && !payload.containsKey("type")) {
            throw new IllegalArgumentException("type is required");
        }
        if (create && !payload.containsKey("item")) {
            throw new IllegalArgumentException("item is required");
        }

        if (payload.containsKey("propertyId")) {
            item.setPropertyId(toLong(payload.get("propertyId")));
        }
        if (payload.containsKey("userId")) {
            item.setUserId(toLong(payload.get("userId")));
        }
        if (payload.containsKey("type")) {
            item.setType(toStringValue(payload.get("type")));
        }
        if (payload.containsKey("item")) {
            item.setItem(toStringValue(payload.get("item")));
        }
        if (payload.containsKey("completed")) {
            item.setCompleted(toBoolean(payload.get("completed")));
        }
        if (payload.containsKey("notes")) {
            item.setNotes(toStringValue(payload.get("notes")));
        }
        if (payload.containsKey("attachmentUrls")) {
            item.setAttachmentUrlsJson(toJson(payload.get("attachmentUrls")));
        }

        if (item.getCompleted() == null) {
            item.setCompleted(false);
        }
        if (item.getAttachmentUrlsJson() == null || item.getAttachmentUrlsJson().isBlank()) {
            item.setAttachmentUrlsJson("[]");
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
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

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public List<String> parseAttachmentUrls(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            Object parsed = objectMapper.readValue(json, Object.class);
            if (parsed instanceof List<?> list) {
                List<String> values = new ArrayList<>();
                for (Object item : list) {
                    if (item != null) {
                        values.add(item.toString());
                    }
                }
                return values;
            }
            return List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }
}
