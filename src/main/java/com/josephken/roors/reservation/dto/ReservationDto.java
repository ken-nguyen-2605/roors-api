package com.josephken.roors.reservation.dto;

import com.josephken.roors.auth.dto.UserSummaryDto;
import com.josephken.roors.reservation.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long id;
    private UserSummaryDto user;
    private DiningTableSummaryDto diningTable;
    private ReservationStatus status;
    private String phone;
    private int numberOfGuests;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
