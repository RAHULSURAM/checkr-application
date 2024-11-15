package com.example.CheckrApplication.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class ErrorResponse {
    private int status;
    private String message;
    private long timestamp;
    private Map<String, String> errors; // Optional, for validation errors

    public ErrorResponse(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ErrorResponse(int status, String message, long timestamp, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.errors = errors;
    }

// Getters and Setters
}
