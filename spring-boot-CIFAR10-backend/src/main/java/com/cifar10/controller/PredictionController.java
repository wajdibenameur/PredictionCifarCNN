package com.cifar10.controller;

import com.cifar10.service.PredictionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/predict")
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "CIFAR-10 Prediction"));
    }

    @PostMapping("/single")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> predictSingle(@RequestParam("image") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No file uploaded or file is empty"));
            }

            Map<String, Object> prediction = predictionService.predictSingle(file);

            return ResponseEntity.ok(prediction);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Prediction failed: " + e.getMessage()));
        }
    }

    @PostMapping("/url")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> predictFromUrl(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("url");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "URL is required"));
            }

            Map<String, Object> prediction = predictionService.predictFromUrl(imageUrl);
            return ResponseEntity.ok(prediction);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "URL prediction failed: " + e.getMessage()));
        }
    }
}
