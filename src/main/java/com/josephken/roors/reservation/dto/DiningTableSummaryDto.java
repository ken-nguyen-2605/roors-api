package com.josephken.roors.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTableSummaryDto {
    private Long id;
    private String name;
    private String floor;
    private int capacity;
}
