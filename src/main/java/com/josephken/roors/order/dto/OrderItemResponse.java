package com.josephken.roors.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String specialInstructions;
    
    // NEW: Dish rating fields
    private Integer dishRating;
    private String dishFeedback;
    private LocalDateTime dishRatedAt;
    private String adminDishResponse;
    private LocalDateTime dishRespondedAt;
}