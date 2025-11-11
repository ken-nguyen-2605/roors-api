package com.josephken.roors.payment.service;

import com.josephken.roors.order.entity.Order;
import com.josephken.roors.payment.dto.ConfirmPaymentRequest;
import com.josephken.roors.payment.dto.PaymentMethodInfo;
import com.josephken.roors.payment.dto.PaymentResponse;
import com.josephken.roors.payment.entity.Payment;
import com.josephken.roors.payment.entity.PaymentMethod;
import com.josephken.roors.payment.entity.PaymentStatus;
import com.josephken.roors.payment.repository.PaymentRepository;
import com.josephken.roors.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VietQRService vietQRService;

    @Value("${payment.bank.code:970436}")
    private String bankCode;

    @Value("${payment.bank.account-number:}")
    private String bankAccountNumber;

    @Value("${payment.bank.account-name:}")
    private String bankAccountName;

    @Value("${payment.qr.expiry-minutes:30}")
    private int qrExpiryMinutes;

    @Transactional
    public Payment createPayment(Order order, PaymentMethod paymentMethod) {
        log.info(LogCategory.payment("Creating payment for order: " + order.getOrderNumber()));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(qrExpiryMinutes));

        // Generate QR code for bank transfer
        if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
            payment.setBankCode(bankCode);
            payment.setBankAccountNumber(bankAccountNumber);
            payment.setBankAccountName(bankAccountName);

            String qrData = vietQRService.generateVietQR(
                    bankCode,
                    bankAccountNumber,
                    bankAccountName,
                    order.getTotalAmount(),
                    "Thanh toan don hang " + order.getOrderNumber()
            );
            payment.setQrCodeData(qrData);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info(LogCategory.payment("Payment created successfully: " + savedPayment.getPaymentCode()));

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByCode(String paymentCode) {
        log.info(LogCategory.payment("Fetching payment with code: " + paymentCode));

        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found with code: " + paymentCode));

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(Order order) {
        log.info(LogCategory.payment("Fetching payment for order: " + order.getOrderNumber()));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + order.getOrderNumber()));

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmPayment(String paymentCode, ConfirmPaymentRequest request) {
        log.info(LogCategory.payment("Confirming payment: " + paymentCode));

        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found with code: " + paymentCode));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }

        if (LocalDateTime.now().isAfter(payment.getExpiresAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment has expired");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setNotes(request.getNotes());

        Payment updatedPayment = paymentRepository.save(payment);
        log.info(LogCategory.payment("Payment confirmed successfully: " + paymentCode));

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public void expirePayment(String paymentCode) {
        log.info(LogCategory.payment("Expiring payment: " + paymentCode));

        Payment payment = paymentRepository.findByPaymentCode(paymentCode)
                .orElseThrow(() -> new RuntimeException("Payment not found with code: " + paymentCode));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            log.info(LogCategory.payment("Payment expired: " + paymentCode));
        }
    }

    public List<PaymentMethodInfo> getAvailablePaymentMethods() {
        log.info(LogCategory.payment("Fetching available payment methods"));

        List<PaymentMethodInfo> methods = new ArrayList<>();

        // Cash payment
        methods.add(new PaymentMethodInfo(
                PaymentMethod.CASH,
                "Tiền mặt",
                "Thanh toán bằng tiền mặt khi nhận hàng",
                true,
                null
        ));

        // Bank transfer with QR
        if (bankAccountNumber != null && !bankAccountNumber.isEmpty()) {
            methods.add(new PaymentMethodInfo(
                    PaymentMethod.BANK_TRANSFER,
                    "Chuyển khoản ngân hàng",
                    "Quét mã QR để chuyển khoản nhanh chóng",
                    true,
                    null
            ));
        }

        // Future payment methods (disabled for now)
        methods.add(new PaymentMethodInfo(
                PaymentMethod.CREDIT_CARD,
                "Thẻ tín dụng/Ghi nợ",
                "Thanh toán bằng thẻ Visa, Mastercard (Sắp ra mắt)",
                false,
                null
        ));

        methods.add(new PaymentMethodInfo(
                PaymentMethod.E_WALLET,
                "Ví điện tử",
                "MoMo, ZaloPay, VNPay (Sắp ra mắt)",
                false,
                null
        ));

        return methods;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentCode(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getQrCodeData(),
                payment.getBankAccountNumber(),
                payment.getBankAccountName(),
                payment.getBankCode(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getExpiresAt(),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }
}
