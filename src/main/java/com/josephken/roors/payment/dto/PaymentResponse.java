package com.josephken.roors.payment.dto;

import com.josephken.roors.payment.entity.PaymentMethod;
import com.josephken.roors.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentCode;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String qrCodeData;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankCode;
    private String transactionReference;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    private String notes;
    private LocalDateTime createdAt;
}
