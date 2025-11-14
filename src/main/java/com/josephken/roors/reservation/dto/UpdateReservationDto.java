package com.josephken.roors.reservation.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// import java.time.LocalDate;
// import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationDto {
    @Min(value = 1, message = "At least one guest is required")
    private Integer numberOfGuests;
    private String phone;
}
