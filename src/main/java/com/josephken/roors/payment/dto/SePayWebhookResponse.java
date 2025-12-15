package com.josephken.roors.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SePayWebhookResponse {

    private boolean success;
    private String message;

    public static SePayWebhookResponse success() {
        return SePayWebhookResponse.builder()
                .success(true)
                .build();
    }

    public static SePayWebhookResponse success(String message) {
        return SePayWebhookResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static SePayWebhookResponse error(String message) {
        return SePayWebhookResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}