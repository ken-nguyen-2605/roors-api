package com.josephken.roors.reservation.controller;

import com.josephken.roors.auth.dto.ErrorResponse;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.common.exception.BusinessException;
import com.josephken.roors.reservation.dto.*;
import com.josephken.roors.reservation.exception.CapacityExceededException;
import com.josephken.roors.reservation.exception.TableNotAvailableException;
import com.josephken.roors.reservation.exception.TimeNotValidException;
import com.josephken.roors.reservation.service.ReservationTableServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reservations")
public class ReservationController {

    private final ReservationTableServiceImpl reservationTableService;

    public ReservationController(ReservationTableServiceImpl reservationTableService) {
        this.reservationTableService = reservationTableService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationDto>> getMyReservations(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getReservationsByUserId(user.getId()));
    }

    @GetMapping("/date-time-availability")
    public ResponseEntity<List<DateTimeValidDto>> getAvailableReservationTimes() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getAvailableReservationTimes());
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getAllReservations());
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateReservationDto createReservationDto
    ) {
        ReservationDto createdReservation = reservationTableService.createReservation(user.getId(), createReservationDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReservation);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationDto> updateReservation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationDto updateReservationDto
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.updateReservation(user.getId(), id, updateReservationDto));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping("/{id}/mark-arrived")
    public ResponseEntity<ReservationDto> markReservationAsArrived(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.markReservationAsArrived(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationDto> cancelReservation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.cancelReservation(user.getId(), id));
    }

    /**
     * Handle conflict exception
     */
    @ExceptionHandler({
            TableNotAvailableException.class,
            CapacityExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handle bad request exception
     */
    @ExceptionHandler({
            TimeNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handleTimeNotValidException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}