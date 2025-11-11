package com.josephken.roors.payment.entity;

public enum PaymentStatus {
    PENDING,        // Waiting for payment
    PROCESSING,     // Payment is being processed
    PAID,           // Payment successful
    FAILED,         // Payment failed
    EXPIRED,        // Payment link/QR expired
    REFUNDED        // Payment refunded
}
