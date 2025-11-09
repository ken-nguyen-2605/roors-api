package com.josephken.roors.reservation.mapper;

import com.josephken.roors.auth.mapper.UserMapper;
import com.josephken.roors.reservation.dto.ReservationDto;
import com.josephken.roors.reservation.entity.Reservation;

public class ReservationMapper {
    public static ReservationDto toDto(Reservation reservation) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .user(UserMapper.toSummaryDto(reservation.getUser()))
                .diningTable(DiningTableMapper.toSummaryDto(reservation.getDiningTable()))
                .status(reservation.getStatus())
                .phone(reservation.getPhone())
                .numberOfGuests(reservation.getNumberOfGuests())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .build();
    }
}
