// src/main/java/com/josephken/roors/payment/controller/SePayWebhookController.java
package com.josephken.roors.payment.controller;

import com.josephken.roors.payment.dto.SePayWebhookRequest;
import com.josephken.roors.payment.dto.SePayWebhookResponse;
import com.josephken.roors.payment.service.SePayWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookController {

    private final SePayWebhookService sePayWebhookService;

    @PostMapping("/sepay")
    public ResponseEntity<SePayWebhookResponse> handleSePayWebhook(
            @RequestBody SePayWebhookRequest payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        log.info("📨 Received SePay webhook - id={}", payload.getId());

        // Validate API Key
        if (!sePayWebhookService.validateApiKey(authorization)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(SePayWebhookResponse.error("Invalid API Key"));
        }

        // Validate payload
        if (payload == null || payload.getId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(SePayWebhookResponse.error("Invalid payload"));
        }

        // Process
        SePayWebhookResponse response = sePayWebhookService.processWebhook(payload);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sepay/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now().toString()
        ));
    }
}