package com.example.controller;

import com.example.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final ObjectMapper objectMapper;

    public MonitoringController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics(@AuthenticationPrincipal JwtPrincipal principal) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        try {
            Path metricsPath = Paths.get("..", "scripts", "logs", "metrics.json");

            if (!Files.exists(metricsPath)) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "No metrics data available yet",
                        "data", null
                ));
            }

            String metricsData = Files.readString(metricsPath, StandardCharsets.UTF_8);
            Object metrics = objectMapper.readValue(metricsData, Object.class);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", metrics
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to read monitoring metrics"
            ));
        }
    }

    @GetMapping("/pm2-status")
    public ResponseEntity<?> getPm2Status(@AuthenticationPrincipal JwtPrincipal principal) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pm2", "jlist");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("pm2 command failed");
            }

            List<?> allProcesses = objectMapper.readValue(output.toString(), List.class);
            Object backendProcess = allProcesses.stream()
                    .filter(item -> item instanceof Map<?, ?>)
                    .map(item -> (Map<?, ?>) item)
                    .filter(item -> "real-estate-backend".equals(item.get("name")))
                    .findFirst()
                    .orElse(null);

            Map<String, Object> data = new HashMap<>();
            data.put("process", backendProcess);
            data.put("allProcesses", allProcesses);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get PM2 process status"
            ));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "combined") String type,
            @RequestParam(defaultValue = "100") String lines
    ) {
        ResponseEntity<?> gate = requireAdmin(principal);
        if (gate != null) {
            return gate;
        }

        try {
            int parsedLines;
            try {
                parsedLines = Integer.parseInt(lines);
            } catch (NumberFormatException ex) {
                parsedLines = 100;
            }

            String safeType = switch (type) {
                case "err", "out", "combined" -> type;
                default -> "combined";
            };

            Path logPath = Paths.get("..", "backend", "logs", safeType + ".log");

            if (!Files.exists(logPath)) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "No log data available",
                        "data", List.of()
                ));
            }

            List<String> logLines = Files.readAllLines(logPath, StandardCharsets.UTF_8).stream()
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .toList();

            int from = Math.max(0, logLines.size() - Math.max(parsedLines, 0));
            List<String> tail = logLines.subList(from, logLines.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", tail
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to read application logs"
            ));
        }
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
}
