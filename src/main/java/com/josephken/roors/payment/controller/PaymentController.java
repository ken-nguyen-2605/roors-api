package com.josephken.roors.payment.controller;

import com.josephken.roors.payment.dto.ConfirmPaymentRequest;
import com.josephken.roors.payment.dto.PaymentMethodInfo;
import com.josephken.roors.payment.dto.PaymentResponse;
import com.josephken.roors.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodInfo>> getPaymentMethods() {
        return ResponseEntity.ok(paymentService.getAvailablePaymentMethods());
    }

    @GetMapping("/{paymentCode}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentCode) {
        return ResponseEntity.ok(paymentService.getPaymentByCode(paymentCode));
    }

    @PostMapping("/{paymentCode}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable String paymentCode,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentCode, request));
    }
}
