package com.josephken.roors.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {
    
    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;
    
    private String notes;
}
