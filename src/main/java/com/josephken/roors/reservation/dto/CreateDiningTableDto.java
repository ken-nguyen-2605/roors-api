package com.josephken.roors.reservation.dto;

import com.josephken.roors.reservation.entity.DiningTableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiningTableDto {
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Floor is required")
    private String floor;

    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;

    @NotNull(message = "Status is required")
    private DiningTableStatus status;
}
