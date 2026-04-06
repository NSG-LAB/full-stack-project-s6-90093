package com.example.service;

import com.example.model.Recommendation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoiPlannerService {

    private static final Map<String, Double> CONDITION_MULTIPLIER = Map.of(
            "excellent", 0.9,
            "good", 1.0,
            "average", 1.05,
            "needs-work", 1.1
    );

    private static final Map<String, Double> DIFFICULTY_WEIGHT = Map.of(
            "easy", 1.0,
            "moderate", 0.92,
            "difficult", 0.85
    );

    private final ObjectMapper objectMapper;

    public RoiPlannerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> createPlan(
            List<Recommendation> recommendations,
            double budget,
            String propertyCondition,
            int topN
    ) {
        List<Map<String, Object>> candidates = recommendations.stream()
                .map(rec -> mapPlanItem(rec, propertyCondition))
                .filter(item -> ((Number) item.get("estimatedCost")).doubleValue() > 0)
                .sorted(
                        Comparator.<Map<String, Object>, Double>comparing(
                                        item -> ((Number) item.get("roiPercentage")).doubleValue()
                                )
                                .reversed()
                                .thenComparing(item -> ((Number) item.get("paybackMonths")).doubleValue())
                )
                .toList();

        List<Map<String, Object>> selected = new ArrayList<>();
        double totalCost = 0;
        for (Map<String, Object> item : candidates) {
            if (selected.size() >= topN) {
                break;
            }

            double itemCost = ((Number) item.get("estimatedCost")).doubleValue();
            if (budget > 0 && totalCost + itemCost > budget) {
                continue;
            }

            selected.add(item);
            totalCost += itemCost;
        }

        double totalEstimatedGain = selected.stream()
                .mapToDouble(item -> ((Number) item.get("estimatedGain")).doubleValue())
                .sum();
        double totalNetGain = selected.stream()
                .mapToDouble(item -> ((Number) item.get("netGain")).doubleValue())
                .sum();

        double blendedRoi = totalCost > 0
                ? round(((totalEstimatedGain / totalCost) * 100), 2)
                : 0;

        Map<String, Object> plan = new HashMap<>();
        plan.put("budget", (long) budget);
        plan.put("totalCost", (long) totalCost);
        plan.put("totalEstimatedGain", (long) totalEstimatedGain);
        plan.put("totalNetGain", (long) totalNetGain);
        plan.put("blendedROI", blendedRoi);
        plan.put("selectedCount", selected.size());
        plan.put("recommendations", selected);
        return plan;
    }

    private Map<String, Object> mapPlanItem(Recommendation recommendation, String propertyCondition) {
        double estimatedCost = averageCost(parseCostObject(recommendation.getEstimatedCostJson()));

        double baseRoiPercent = recommendation.getRoiPercentage() != null
                ? recommendation.getRoiPercentage()
                : recommendation.getExpectedRoi() == null ? 0 : recommendation.getExpectedRoi().doubleValue();

        double adjustedRoiPercent = round(
                baseRoiPercent
                        * CONDITION_MULTIPLIER.getOrDefault(propertyCondition, 1.0)
                        * DIFFICULTY_WEIGHT.getOrDefault(recommendation.getDifficulty(), 1.0),
                2
        );

        double estimatedGain = Math.round(estimatedCost * (adjustedRoiPercent / 100));
        double netGain = estimatedGain - estimatedCost;
        double paybackMonths = round(estimatedCost / Math.max(estimatedGain / 12.0, 1.0), 1);
        double durationMonths = round(monthsFromTimeframe(recommendation.getTimeframe()), 1);

        Map<String, Object> item = new HashMap<>();
        item.put("id", recommendation.getId());
        item.put("title", recommendation.getTitle());
        item.put("category", recommendation.getCategory());
        item.put("difficulty", recommendation.getDifficulty());
        item.put("timeframe", recommendation.getTimeframe());
        item.put("estimatedCost", (long) estimatedCost);
        item.put("roiPercentage", adjustedRoiPercent);
        item.put("estimatedGain", (long) estimatedGain);
        item.put("netGain", (long) netGain);
        item.put("paybackMonths", paybackMonths);
        item.put("durationMonths", durationMonths);
        return item;
    }

    private Map<String, Object> parseCostObject(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private double averageCost(Map<String, Object> costObject) {
        double min = toDouble(costObject.get("min"));
        double max = toDouble(costObject.get("max"));

        if (min == 0 && max == 0) {
            return 0;
        }
        if (min == 0) {
            return max;
        }
        if (max == 0) {
            return min;
        }
        return Math.round((min + max) / 2.0);
    }

    private double monthsFromTimeframe(String timeframe) {
        if (timeframe == null || timeframe.isBlank()) {
            return 6;
        }

        String lower = timeframe.toLowerCase();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)\\s*(day|week|month|year)")
                .matcher(lower);

        if (!matcher.find()) {
            return 6;
        }

        double value = toDouble(matcher.group(1));
        String unit = matcher.group(2);

        if (unit.startsWith("day")) {
            return Math.max(1, value / 30.0);
        }
        if (unit.startsWith("week")) {
            return Math.max(1, value / 4.0);
        }
        if (unit.startsWith("month")) {
            return Math.max(1, value);
        }
        if (unit.startsWith("year")) {
            return Math.max(1, value * 12);
        }
        return 6;
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
            return 0;
        }
    }

    private double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
