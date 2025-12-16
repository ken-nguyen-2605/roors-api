package com.josephken.roors.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResponseRequest {
    @NotBlank(message = "Response is required")
    private String response;
}