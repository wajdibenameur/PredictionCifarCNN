package com.cifar10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Cifar10Application {
    public static void main(String[] args) {
        SpringApplication.run(Cifar10Application.class, args);

    }
}