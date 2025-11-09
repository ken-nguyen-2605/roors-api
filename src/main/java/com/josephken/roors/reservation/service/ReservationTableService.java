package com.josephken.roors.reservation.service;

import com.josephken.roors.reservation.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationTableService {
    // DATE TIME AVAILABILITY
    List<DateTimeValidDto> getAvailableReservationTimes();

    // RESERVATION
    List<ReservationDto> getReservationsByUserId(Long userId);
    List<ReservationDto> getAllReservations();
    ReservationDto getReservationById(Long reservationId);
    ReservationDto createReservation(Long userId, CreateReservationDto createReservationDto);
    ReservationDto updateReservation(Long userId, Long reservationId, UpdateReservationDto updateReservationDto);
    ReservationDto markReservationAsArrived(Long reservationId);
    ReservationDto cancelReservation(Long reservationId, Long userId);
    void deleteReservation(Long reservationId);

    // TABLE
    List<DiningTableDto> getAvailableDiningTables(LocalDate date, LocalTime time, int partySize);
    List<DiningTableDto> getAllDiningTables();
    DiningTableDto getDiningTableById(Long tableId);
    DiningTableDto createDiningTable(CreateDiningTableDto createDiningTableDto);
    DiningTableDto updateDiningTable(Long tableId, UpdateDiningTableDto updateDiningTableDto);
    void deleteDiningTable(Long tableId);
}
