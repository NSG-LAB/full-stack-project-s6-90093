package com.example.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ValuationService {

    public Map<String, Object> estimateValue(Map<String, Object> input) {
        double areaSqft = toDouble(input.get("areaSqft"), 0);
        double ageYears = toDouble(input.get("ageYears"), 0);
        int bedrooms = (int) toDouble(input.get("bedrooms"), 0);
        int bathrooms = (int) toDouble(input.get("bathrooms"), 0);
        int conditionScore = (int) Math.min(5, Math.max(1, toDouble(input.get("conditionScore"), 3)));

        int baseRatePerSqft = 4500;
        double baseValue = areaSqft * baseRatePerSqft;

        double bedroomBoost = bedrooms * 0.03;
        double bathroomBoost = bathrooms * 0.015;
        double conditionBoost = (conditionScore - 3) * 0.04;
        double agePenalty = Math.min(0.35, ageYears * 0.01);

        double currentMultiplier = 1 + bedroomBoost + bathroomBoost + conditionBoost - agePenalty;
        long currentValue = Math.max(0, Math.round(baseValue * currentMultiplier));

        double improvementUplift = 0.08 + Math.max(0, (5 - conditionScore) * 0.02);
        long improvedValue = Math.round(currentValue * (1 + improvementUplift));

        long rangeMin = Math.round(currentValue * 0.92);
        long rangeMax = Math.round(currentValue * 1.08);

        Map<String, Object> assumptions = new HashMap<>();
        assumptions.put("baseRatePerSqft", baseRatePerSqft);
        assumptions.put("improvementUplift", improvementUplift);

        Map<String, Object> range = new HashMap<>();
        range.put("min", rangeMin);
        range.put("max", rangeMax);

        Map<String, Object> result = new HashMap<>();
        result.put("currentValue", currentValue);
        result.put("improvedValue", improvedValue);
        result.put("confidence", "medium");
        result.put("range", range);
        result.put("assumptions", assumptions);

        return result;
    }

    private double toDouble(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
