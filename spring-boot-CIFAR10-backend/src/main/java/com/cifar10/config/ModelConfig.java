package com.cifar10.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Configuration
public class ModelConfig {

    @Value("${cifar10.model.input-size:32}")
    private int inputSize;

    @Value("#{'${cifar10.model.classes}'.split(',')}")
    private List<String> classes;

    @Value("${cifar10.model.mean}")
    private String meanStr;

    @Value("${cifar10.model.std}")
    private String stdStr;

    private List<Float> mean;
    private List<Float> std;

    // Méthode d'initialisation après injection
    @javax.annotation.PostConstruct
    private void init() {
        mean = Arrays.stream(meanStr.split(","))
                .map(Float::parseFloat)
                .collect(Collectors.toList());

        std = Arrays.stream(stdStr.split(","))
                .map(Float::parseFloat)
                .collect(Collectors.toList());
    }

    // Getters
    public int getInputSize() { return inputSize; }
    public List<String> getClasses() { return classes; }
    public List<Float> getMean() { return mean; }
    public List<Float> getStd() { return std; }
}
