package com.josephken.roors.reservation.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableAvailabilityRequestDto {
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDate date;

    @NotNull(message = "Hour is required")
    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer hour; // 0-23 (1h00 = 1, 14h00 = 14)

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest required")
    @Max(value = 10, message = "Maximum 10 guests")
    private Integer numberOfGuests;

    // Optional
    private Integer floor;
}

// For creating reservation
//@Getter @Setter
//public class CreateReservationDTO {
//    @NotNull(message = "User ID is required")
//    private Long userId;
//
//    @NotNull(message = "Date is required")
//    @Future(message = "Date must be in the future")
//    private LocalDate date;
//
//    @NotNull(message = "Hour is required")
//    @Min(value = 0)
//    @Max(value = 23)
//    private Integer hour; // Only hour, no minutes
//
//    @NotBlank(message = "Phone is required")
//    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
//    private String phone;
//
//    @NotNull(message = "Number of guests is required")
//    @Min(value = 1)
//    @Max(value = 10)
//    private Integer numberOfGuests;
//}