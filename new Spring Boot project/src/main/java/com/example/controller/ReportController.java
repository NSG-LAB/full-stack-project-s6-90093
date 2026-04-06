package com.example.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @PostMapping("/valuation-pdf")
    public ResponseEntity<byte[]> generateValuationPdf(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> request = payload == null ? Map.of() : payload;
        Map<String, Object> valuationInput = mapOf(request.get("valuationInput"));
        Map<String, Object> valuationResult = mapOf(request.get("valuationResult"));
        Map<String, Object> roiPlan = mapOf(request.get("roiPlan"));

        byte[] pdfBytes = buildPdf(valuationInput, valuationResult, roiPlan.isEmpty() ? null : roiPlan);

        String fileName = "valuation-report-" + System.currentTimeMillis() + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private byte[] buildPdf(
            Map<String, Object> valuationInput,
            Map<String, Object> valuationResult,
            Map<String, Object> roiPlan
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("Property Value Enhancement Report");
        lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        lines.add("");
        lines.add("Valuation Inputs");
        lines.add("Area (sqft): " + valueOrDash(valuationInput.get("areaSqft")));
        lines.add("Age (years): " + valueOrDash(valuationInput.get("ageYears")));
        lines.add("Bedrooms: " + valueOrDash(valuationInput.get("bedrooms")));
        lines.add("Bathrooms: " + valueOrDash(valuationInput.get("bathrooms")));
        lines.add("Condition Score: " + valueOrDash(valuationInput.get("conditionScore")));
        lines.add("");
        lines.add("Valuation Results");
        lines.add("Current Value: " + formatCurrency(valuationResult.get("currentValue")));
        lines.add("Improved Value: " + formatCurrency(valuationResult.get("improvedValue")));
        lines.add("Confidence: " + valueOrDash(valuationResult.get("confidence")));

        Map<String, Object> range = mapOf(valuationResult.get("range"));
        if (!range.isEmpty()) {
            lines.add("Range: " + formatCurrency(range.get("min")) + " - " + formatCurrency(range.get("max")));
        }

        if (roiPlan != null) {
            lines.add("");
            lines.add("ROI Plan Summary");
            lines.add("Budget: " + formatCurrency(roiPlan.get("budget")));
            lines.add("Total Cost: " + formatCurrency(roiPlan.get("totalCost")));
            lines.add("Estimated Gain: " + formatCurrency(roiPlan.get("totalEstimatedGain")));
            lines.add("Blended ROI: " + valueOrDash(roiPlan.get("blendedROI")) + "%");

            Object recsObject = roiPlan.get("recommendations");
            if (recsObject instanceof List<?> recs && !recs.isEmpty()) {
                lines.add("Top Recommendations:");
                int count = 0;
                for (Object recObject : recs) {
                    if (!(recObject instanceof Map<?, ?> recMap) || count >= 5) {
                        continue;
                    }
                    Map<String, Object> rec = toStringKeyMap(recMap);
                    count++;
                    lines.add(count + ". " + valueOrDash(rec.get("title"))
                            + " | Cost: " + formatCurrency(rec.get("estimatedCost"))
                            + " | ROI: " + valueOrDash(rec.get("roiPercentage")) + "%"
                            + " | Payback: " + valueOrDash(rec.get("paybackMonths")) + " months");
                }
            }
        }

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setLeading(16f);
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 11);
                content.newLineAtOffset(50, 760);

                int lineOnPage = 0;
                for (String line : lines) {
                    if (lineOnPage >= 42) {
                        break;
                    }
                    content.showText(safeText(line));
                    content.newLine();
                    lineOnPage++;
                }

                content.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n]", " ");
    }

    private Map<String, Object> mapOf(Object value) {
        if (value instanceof Map<?, ?> map) {
            return toStringKeyMap(map);
        }
        return Map.of();
    }

    private Map<String, Object> toStringKeyMap(Map<?, ?> map) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }

    private String formatCurrency(Object value) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        double amount = 0;
        if (value instanceof Number number) {
            amount = number.doubleValue();
        } else if (value != null) {
            try {
                amount = Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
                amount = 0;
            }
        }
        return "INR " + formatter.format(amount);
    }

    private String valueOrDash(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        return text.isBlank() ? "-" : text;
    }
}
