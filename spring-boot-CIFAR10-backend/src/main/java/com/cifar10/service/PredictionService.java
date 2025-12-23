package com.cifar10.service;

import com.cifar10.exception.PredictionException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

@Service
public class PredictionService {

    private final PyTorchService pytorchService;

    public PredictionService(PyTorchService pytorchService) {
        this.pytorchService = pytorchService;
    }

    public Map<String, Object> predictSingle(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PredictionException("File is empty or null");
        }

        try {
            // Envoyez simplement le fichier brut au PyTorchService
            Map<String, Object> prediction = pytorchService.predict(file.getBytes());
            prediction.put("filename", file.getOriginalFilename());
            prediction.put("size", file.getSize());
            return prediction;
        } catch (IOException e) {
            throw new PredictionException("Cannot read image file", e);
        }
    }

    public Map<String, Object> predictFromUrl(String imageUrl) {
        try {
            byte[] imageBytes = new URL(imageUrl).openStream().readAllBytes();
            // Vous pouvez créer un MultipartFile temporaire ou modifier PyTorchService pour accepter bytes directement
            // Pour l'instant, modifions PyTorchService pour accepter bytes
            Map<String, Object> prediction = pytorchService.predict(imageBytes);
            prediction.put("source_url", imageUrl);
            return prediction;
        } catch (IOException e) {
            throw new PredictionException("Cannot read image from URL", e);
        }
    }
}