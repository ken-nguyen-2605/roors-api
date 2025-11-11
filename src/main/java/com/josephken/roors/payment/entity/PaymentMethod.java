package com.josephken.roors.payment.entity;

public enum PaymentMethod {
    CASH,           // Cash payment at restaurant/delivery
    BANK_TRANSFER,  // Bank transfer via QR code
    CREDIT_CARD,    // Credit/Debit card (future)
    E_WALLET        // MoMo, ZaloPay, etc. (future)
}
