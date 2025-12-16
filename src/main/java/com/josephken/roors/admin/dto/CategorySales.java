package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategorySales {
    private String categoryName;
    private long orderCount;
    private BigDecimal revenue;
}


