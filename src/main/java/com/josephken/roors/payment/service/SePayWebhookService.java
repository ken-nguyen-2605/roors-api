// src/main/java/com/josephken/roors/payment/service/SePayWebhookService.java
package com.josephken.roors.payment.service;

import com.josephken.roors.order.entity.Order;
import com.josephken.roors.order.service.OrderService;
import com.josephken.roors.payment.config.SePayConfig;
import com.josephken.roors.payment.dto.PaymentResponse;
import com.josephken.roors.payment.dto.SePayWebhookRequest;
import com.josephken.roors.payment.dto.SePayWebhookResponse;
import com.josephken.roors.payment.entity.Payment;
import com.josephken.roors.payment.entity.PaymentStatus;
import com.josephken.roors.payment.entity.SePayTransaction;
import com.josephken.roors.payment.repository.SePayTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookService {

    private final SePayTransactionRepository transactionRepository;
    private final OrderService orderService;
    private final SePayConfig sePayConfig;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String API_KEY_PREFIX = "Apikey ";

    // Pattern: "Thanh toan don hang ORD1234567890" or just "ORD1234567890"
    private static final Pattern ORDER_PATTERN = Pattern.compile(
            "\\b(ORD\\d+)\\b",
            Pattern.CASE_INSENSITIVE
    );
    private final PaymentService paymentService;

    /**
     * Validate API Key from Authorization header
     */
    public boolean validateApiKey(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            log.warn("❌ Missing Authorization header");
            return false;
        }

        if (!authorizationHeader.startsWith(API_KEY_PREFIX)) {
            log.warn("❌ Invalid Authorization format");
            return false;
        }

        String providedApiKey = authorizationHeader.substring(API_KEY_PREFIX.length()).trim();
        boolean isValid = sePayConfig.getApiKey().equals(providedApiKey);

        if (!isValid) {
            log.warn("❌ Invalid API Key");
        }

        return isValid;
    }

    /**
     * Process webhook from SePay
     */
    @Transactional
    public SePayWebhookResponse processWebhook(SePayWebhookRequest payload) {
        log.info("📥 Processing SePay webhook: id={}, amount={}, type={}, content='{}'",
                payload.getId(), payload.getTransferAmount(),
                payload.getTransferType(), payload.getContent());

        try {
            // 1. Check duplicate by SePay ID
            if (transactionRepository.existsBySepayId(payload.getId())) {
                log.warn("⚠️ Duplicate transaction: sepayId={}", payload.getId());
                return SePayWebhookResponse.success("Duplicate transaction");
            }

            // 2. Parse date
            Instant transactionDate = parseToInstant(payload.getTransactionDate());

            // 3. Determine amount
            BigDecimal amountIn = BigDecimal.ZERO;
            BigDecimal amountOut = BigDecimal.ZERO;
            long amount = payload.getTransferAmount() != null ? payload.getTransferAmount() : 0;

            if ("in".equalsIgnoreCase(payload.getTransferType())) {
                amountIn = BigDecimal.valueOf(amount);
            } else if ("out".equalsIgnoreCase(payload.getTransferType())) {
                amountOut = BigDecimal.valueOf(amount);
            }

            // 4. Extract order number from content
            String orderNumber = extractOrderNumber(payload.getContent(), payload.getCode());

            // 5. Save transaction
            SePayTransaction transaction = SePayTransaction.builder()
                    .sepayId(payload.getId())
                    .gateway(payload.getGateway())
                    .transactionDate(transactionDate)
                    .accountNumber(payload.getAccountNumber())
                    .subAccount(payload.getSubAccount())
                    .amountIn(amountIn)
                    .amountOut(amountOut)
                    .accumulated(BigDecimal.valueOf(payload.getAccumulated() != null ? payload.getAccumulated() : 0))
                    .code(payload.getCode())
                    .transactionContent(payload.getContent())
                    .referenceCode(payload.getReferenceCode())
                    .description(payload.getDescription())
                    .transferType(payload.getTransferType())
                    .matchedOrderNumber(orderNumber)
                    .build();

            transactionRepository.save(transaction);

            log.info("✅ Transaction saved: id={}, sepayId={}, orderNumber={}, amount={}",
                    transaction.getId(), transaction.getSepayId(), orderNumber, amount);

            // 6. TODO: Process payment matching logic here later
            if ("in".equalsIgnoreCase(payload.getTransferType()) && orderNumber != null) {
                processPaymentMatching(transaction);
            }

            return SePayWebhookResponse.success();

        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return SePayWebhookResponse.error("Error: " + e.getMessage());
        }
    }

    /**
     * Extract order number from content
     * Pattern: "Thanh toan don hang ORD1705312000000"
     */
    private String extractOrderNumber(String content, String code) {
        // First check code field
        if (code != null && code.toUpperCase().startsWith("ORD")) {
            return code.toUpperCase();
        }

        // Extract from content
        if (content != null) {
            Matcher matcher = ORDER_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1).toUpperCase();
            }
        }

        return null;
    }

    /**
     * TODO: Implement your payment matching logic here
     */
    private void processPaymentMatching(SePayTransaction transaction) {
        log.info("🔄 TODO: Match order {} with amount {}",
                transaction.getMatchedOrderNumber(), transaction.getAmountIn());

        // TODO: Implement later
        // 1. Find order by orderNumber
        // 2. Verify amount matches order.totalAmount
        // 3. Update order status to PAID
        // 4. Create/update Payment entity
        // 5. Send notification

        Order existingOrder = orderService.getOrderByOrderNumber(transaction.getMatchedOrderNumber());
        if (existingOrder == null) {
            log.warn("⚠️ Order not found: {}", transaction.getMatchedOrderNumber());
            return;
        }

        if (existingOrder.getTotalAmount().compareTo(transaction.getAmountIn()) != 0) {
            log.warn("⚠️ Amount mismatch for order {}: expected {}, got {}",
                    existingOrder.getOrderNumber(),
                    existingOrder.getTotalAmount(),
                    transaction.getAmountIn());
            return;
        }

        Payment payment = paymentService.getPaymentEntityByOrder(existingOrder);
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("ℹ️ Payment already marked as PAID for order {}", existingOrder.getOrderNumber());
            return;
        }

        paymentService.markPaymentAsPaid(payment, transaction);
        log.info("✅ Payment marked as PAID for order {}", existingOrder.getOrderNumber());

    }

    private Instant parseToInstant(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return Instant.now();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
            return localDateTime.atZone(VIETNAM_ZONE).toInstant();
        } catch (Exception e) {
            log.warn("Could not parse date '{}', using now", dateStr);
            return Instant.now();
        }
    }
}