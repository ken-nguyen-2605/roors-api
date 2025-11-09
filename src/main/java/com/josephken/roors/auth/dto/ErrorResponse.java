package com.josephken.roors.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String message;
    private int status;
    private long timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ErrorResponse(String message, int status, Map<String, String> validationErrors) {
        this.message = message;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
        this.validationErrors = validationErrors;
    }
}