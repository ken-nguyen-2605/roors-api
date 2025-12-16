// src/main/java/com/josephken/roors/payment/entity/SePayTransaction.java
package com.josephken.roors.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sepay_transactions", indexes = {
        @Index(name = "idx_sepay_id", columnList = "sepay_id", unique = true),
        @Index(name = "idx_reference_code", columnList = "reference_code"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_matched_order_number", columnList = "matched_order_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SePayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sepay_id", unique = true, nullable = false)
    private Long sepayId;

    private String gateway;

    @Column(name = "transaction_date")
    private Instant transactionDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "sub_account")
    private String subAccount;

    @Column(name = "amount_in", precision = 20, scale = 2)
    private BigDecimal amountIn;

    @Column(name = "amount_out", precision = 20, scale = 2)
    private BigDecimal amountOut;

    @Column(precision = 20, scale = 2)
    private BigDecimal accumulated;

    private String code;

    @Column(name = "transaction_content", columnDefinition = "TEXT")
    private String transactionContent;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "transfer_type")
    private String transferType;

    // Extracted order number from content (for later matching)
    @Column(name = "matched_order_number")
    private String matchedOrderNumber;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (amountIn == null) {
            amountIn = BigDecimal.ZERO;
        }
        if (amountOut == null) {
            amountOut = BigDecimal.ZERO;
        }
    }
}