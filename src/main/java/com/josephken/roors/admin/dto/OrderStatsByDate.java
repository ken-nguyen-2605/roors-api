package com.josephken.roors.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderStatsByDate {
    private LocalDate date;
    private long orderCount;
    private BigDecimal revenue;
}
