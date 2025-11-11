package com.josephken.roors.order.dto;

import com.josephken.roors.order.entity.OrderType;
import com.josephken.roors.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String customerName;
    
    private String customerPhone;
    
    private String customerEmail;
    
    private String deliveryAddress;
    
    private String specialInstructions;
    
    private String tableNumber;
}
