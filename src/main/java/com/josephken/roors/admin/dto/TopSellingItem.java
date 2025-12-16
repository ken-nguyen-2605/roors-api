package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopSellingItem {
    private Long menuItemId;
    private String name;
    private long totalQuantity;
    private BigDecimal totalRevenue;
}
