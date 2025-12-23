package com.cifar10.payload.response;

public class MessageResponse {

    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    // Getters et Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}