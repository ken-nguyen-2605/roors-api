package com.josephken.roors.reservation.dto;

import com.josephken.roors.reservation.entity.DiningTableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTableDto {
    private Long id;
    private String name;
    private String floor;
    private int capacity;
    private DiningTableStatus status;
}
