package com.josephken.roors.reservation.dto;

import com.josephken.roors.reservation.entity.DiningTableStatus;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiningTableDto {
    private String name;
    private String floor;
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    private DiningTableStatus status;
}
