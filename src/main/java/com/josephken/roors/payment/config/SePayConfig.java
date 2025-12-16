// src/main/java/com/josephken/roors/payment/config/SePayConfig.java
package com.josephken.roors.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sepay")
@Data
public class SePayConfig {

    private String apiKey;  // Your API key for webhook authentication
}