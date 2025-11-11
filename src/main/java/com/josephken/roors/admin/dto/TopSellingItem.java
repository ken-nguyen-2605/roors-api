package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopSellingItem {
    private Long menuItemId;
    private String name;
    private long totalQuantity;
}
