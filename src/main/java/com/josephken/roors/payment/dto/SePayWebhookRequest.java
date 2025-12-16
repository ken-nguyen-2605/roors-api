package com.josephken.roors.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SePayWebhookRequest {

    @JsonProperty("id")
    private Long id;  // SePay transaction ID

    @JsonProperty("gateway")
    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("code")
    private String code;  // Optional - payment code

    @JsonProperty("content")
    private String content;  // Transfer content

    @JsonProperty("transferType")
    private String transferType;  // "in" or "out"

    @JsonProperty("transferAmount")
    private Long transferAmount;

    @JsonProperty("accumulated")
    private Long accumulated;

    @JsonProperty("subAccount")
    private String subAccount;  // Optional - sub account

    @JsonProperty("referenceCode")
    private String referenceCode;

    @JsonProperty("description")
    private String description;  // Full SMS content
}