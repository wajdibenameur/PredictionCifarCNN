package com.cifar10.payload.response;

import java.util.Map;
import java.util.List;

public class PredictionResponse {

    private String prediction;
    private Double confidence;
    private Integer classId;
    private Map<String, Double> allProbabilities;
    private List<TopPrediction> top3;
    private String filename;

    // Classe interne pour top3
    public static class TopPrediction {
        private String className;
        private Double confidence;
        private Integer classId;

        public TopPrediction(String className, Double confidence, Integer classId) {
            this.className = className;
            this.confidence = confidence;
            this.classId = classId;
        }

        // Getters et Setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }
    }

    // Constructeur
    public PredictionResponse(String prediction, Double confidence, Integer classId,
                              Map<String, Double> allProbabilities, List<TopPrediction> top3,
                              String filename) {
        this.prediction = prediction;
        this.confidence = confidence;
        this.classId = classId;
        this.allProbabilities = allProbabilities;
        this.top3 = top3;
        this.filename = filename;
    }

    // Getters et Setters
    public String getPrediction() { return prediction; }
    public void setPrediction(String prediction) { this.prediction = prediction; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Map<String, Double> getAllProbabilities() { return allProbabilities; }
    public void setAllProbabilities(Map<String, Double> allProbabilities) { this.allProbabilities = allProbabilities; }

    public List<TopPrediction> getTop3() { return top3; }
    public void setTop3(List<TopPrediction> top3) { this.top3 = top3; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}