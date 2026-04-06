package com.example.controller;

import com.example.service.ValuationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/valuations")
public class ValuationController {

    private final ValuationService valuationService;

    public ValuationController(ValuationService valuationService) {
        this.valuationService = valuationService;
    }

    @PostMapping("/estimate")
    public ResponseEntity<?> estimate(@RequestBody Map<String, Object> payload) {
        List<Map<String, String>> errors = validate(payload);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        Map<String, Object> result = valuationService.estimateValue(payload);
        return ResponseEntity.ok(result);
    }

    private List<Map<String, String>> validate(Map<String, Object> payload) {
        List<Map<String, String>> errors = new ArrayList<>();

        Double areaSqft = toDouble(payload.get("areaSqft"));
        if (areaSqft == null || areaSqft <= 100) {
            errors.add(error("areaSqft", "areaSqft must be a number greater than 100"));
        }

        Double ageYears = toDouble(payload.get("ageYears"));
        if (ageYears == null || ageYears < 0) {
            errors.add(error("ageYears", "ageYears must be a number greater than or equal to 0"));
        }

        Integer bedrooms = toInt(payload.get("bedrooms"));
        if (bedrooms == null || bedrooms < 0) {
            errors.add(error("bedrooms", "bedrooms must be an integer greater than or equal to 0"));
        }

        Integer bathrooms = toInt(payload.get("bathrooms"));
        if (bathrooms == null || bathrooms < 0) {
            errors.add(error("bathrooms", "bathrooms must be an integer greater than or equal to 0"));
        }

        if (payload.containsKey("conditionScore")) {
            Integer conditionScore = toInt(payload.get("conditionScore"));
            if (conditionScore == null || conditionScore < 1 || conditionScore > 5) {
                errors.add(error("conditionScore", "conditionScore must be an integer between 1 and 5"));
            }
        }

        return errors;
    }

    private Map<String, String> error(String field, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("path", field);
        error.put("msg", message);
        return error;
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

    private Integer toInt(Object value) {
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
}
