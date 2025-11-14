package com.josephken.roors.reservation.controller;

import com.josephken.roors.auth.dto.ErrorResponse;
import com.josephken.roors.common.exception.BusinessException;
import com.josephken.roors.reservation.dto.CreateDiningTableDto;
import com.josephken.roors.reservation.dto.DiningTableDto;
import com.josephken.roors.reservation.dto.UpdateDiningTableDto;
import com.josephken.roors.reservation.exception.CapacityExceededException;
import com.josephken.roors.reservation.exception.DuplicateTableNameException;
import com.josephken.roors.reservation.service.ReservationTableServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class DiningTableController {

    private final ReservationTableServiceImpl reservationTableService;

    @GetMapping("/availability")
    public ResponseEntity<List<DiningTableDto>> getAvailableDiningTables(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime time,
            @RequestParam int numberOfGuests
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getAvailableDiningTables(date, time, numberOfGuests));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<DiningTableDto>> getAllDiningTables() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getAllDiningTables());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<DiningTableDto> getDiningTableById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.getDiningTableById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PostMapping
    public ResponseEntity<DiningTableDto> createDiningTable(@Valid @RequestBody CreateDiningTableDto createDiningTableDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationTableService.createDiningTable(createDiningTableDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PatchMapping("/{id}")
    public ResponseEntity<DiningTableDto> updateDiningTable(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiningTableDto updateDiningTableDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationTableService.updateDiningTable(id, updateDiningTableDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiningTable(@PathVariable Long id) {
        reservationTableService.deleteDiningTable(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * Handle conflict exception
     */
    @ExceptionHandler({
            DuplicateTableNameException.class,
            CapacityExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }
}
