package com.josephken.roors.reservation.dto;

// import com.fasterxml.jackson.annotation.JsonFormat;
// import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
// import org.springframework.cglib.core.Local;
// import org.springframework.format.annotation.DateTimeFormat;

// import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationDto {

    @NotNull(message = "Dining table ID is required")
    private Long diningTableId;

    @NotNull(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least one guest is required")
    private int numberOfGuests;

    @NotNull(message = "Reservation date and time is required")
    @FutureOrPresent(message = "Reservation date and time must be today or in the future")
    private LocalDateTime reservationDateTime;
}
