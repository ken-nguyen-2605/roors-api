package com.josephken.roors.payment.dto;

import com.josephken.roors.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodInfo {
    private PaymentMethod method;
    private String name;
    private String description;
    private boolean enabled;
    private String iconUrl;
}
