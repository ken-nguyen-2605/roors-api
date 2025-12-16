package com.josephken.roors.reservation.service;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.service.UserService;
import com.josephken.roors.reservation.dto.*;
import com.josephken.roors.reservation.entity.DiningTable;
import com.josephken.roors.reservation.entity.DiningTableStatus;
import com.josephken.roors.reservation.entity.Reservation;
import com.josephken.roors.reservation.entity.ReservationStatus;
import com.josephken.roors.reservation.exception.CapacityExceededException;
import com.josephken.roors.reservation.exception.DuplicateTableNameException;
import com.josephken.roors.reservation.exception.TableNotAvailableException;
import com.josephken.roors.reservation.exception.TimeNotValidException;
import com.josephken.roors.reservation.mapper.DiningTableMapper;
import com.josephken.roors.reservation.mapper.ReservationMapper;
import com.josephken.roors.reservation.repository.DiningTableRepository;
import com.josephken.roors.reservation.repository.ReservationRepository;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.josephken.roors.auth.service.EmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTableServiceImpl implements ReservationTableService {

    private final ReservationRepository reservationRepository;
    private final DiningTableRepository diningTableRepository;
    private final UserService userService;
    private final EmailService emailService;

    private static final LocalTime OPENING_TIME = LocalTime.of(10, 0);
    private static final LocalTime LAST_RESERVATION_TIME = LocalTime.of(20, 0);
    private static final int RESERVATION_DURATION_HOURS = 2;

    /**
     * Get available reservation times follows business rules:
     * 1. Operating hours: 10:00 AM to 8:00 PM (Last reservation at 8:00 PM for 2 hours)
     * 2. Reservations can be made at least 30 minutes in advance
     * 3. Reservations can be made up to 2 weeks in advance (round up to 10:00 PM of the last day)
     * 4. Time slots are in 1 hour increments
     *
     * @return List of DateTimeValidDto representing available reservation dates and times
     */
    @Override
    public List<DateTimeValidDto> getAvailableReservationTimes() {
        log.info(LogCategory.reservation("Fetching available reservation times"));

        LocalTime timeNow = LocalTime.now();
        LocalDate dateNow = LocalDate.now();

        LocalTime reservationTime;
        LocalDate reservationDate;

        if (timeNow.isBefore(OPENING_TIME.minusMinutes(30))) {
            reservationTime = OPENING_TIME;
            reservationDate = dateNow;
        } else if (timeNow.isAfter(LAST_RESERVATION_TIME.minusMinutes(30))) {
            reservationTime = OPENING_TIME;
            reservationDate = dateNow.plusDays(1);
        } else {
            int nextHour = timeNow.getMinute() > 30 ? timeNow.getHour() + 2 : timeNow.getHour() + 1;
            reservationTime = LocalTime.of(nextHour, 0);
            reservationDate = dateNow;
        }

        List<DateTimeValidDto> availableDateTimes = new ArrayList<>();

        LocalDate endDate = dateNow.plusWeeks(2);

        // Append first date (could contain partial times)
        List<LocalTime> timesForFirstDate = new ArrayList<>();
        while (!reservationTime.isAfter(LAST_RESERVATION_TIME)) {
            timesForFirstDate.add(reservationTime);
            reservationTime = reservationTime.plusHours(1);
        }
        availableDateTimes.add(new DateTimeValidDto(reservationDate, timesForFirstDate));

        // Append remaining full dates all time slots (Time complexity optimization)
        List<LocalTime> fullDayTimes = new ArrayList<>();
        for (LocalTime time = OPENING_TIME; !time.isAfter(LAST_RESERVATION_TIME); time = time.plusHours(1)) {
            fullDayTimes.add(time);
        }

        reservationDate = reservationDate.plusDays(1);
        for (; !reservationDate.isAfter(endDate); reservationDate = reservationDate.plusDays(1)) {
            availableDateTimes.add(new DateTimeValidDto(reservationDate, fullDayTimes));
        }

        log.info(LogCategory.reservation("Available reservation times fetched successfully"));
        return availableDateTimes;
    }

    /**
     * Get reservations by user ID
     */
    @Override
    public List<ReservationDto> getReservationsByUserId(Long userId) {
        log.info(LogCategory.reservation("Fetching reservations for user with id: {}"), userId);

        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        log.info(LogCategory.reservation("Found {} reservations for user with id: {}"), reservations.size(), userId);
        return reservations.stream()
                .map(ReservationMapper::toDto)
                .toList();
    }

    /**
     * Get all reservations (for staff/manager)
     */
    @Override
    public List<ReservationDto> getAllReservations() {
        log.info(LogCategory.reservation("Fetching all reservations"));

        List<Reservation> reservations = reservationRepository.findAll();

        log.info(LogCategory.reservation("Total reservations found: {}"), reservations.size());
        return reservations.stream()
                .map(ReservationMapper::toDto)
                .toList();
    }

    @Override
    public ReservationDto getReservationById(Long reservationId) {
        log.info(LogCategory.reservation("Fetching reservation with id: {}"), reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        log.info(LogCategory.reservation("Found reservation with id: {}"), reservationId);
        return ReservationMapper.toDto(reservation);
    }

    @Override
    public ReservationDto createReservation(Long userId, CreateReservationDto createReservationDto) {
        log.info(LogCategory.reservation("Creating reservation for user with id: {}"), userId);

        User user = userService.findById(userId);

        DiningTable diningTable = diningTableRepository.findById(createReservationDto.getDiningTableId())
                .orElseThrow(() -> new IllegalArgumentException("Dining table not found with id: " + createReservationDto.getDiningTableId()));

        LocalDateTime startTime = createReservationDto.getReservationDateTime();
        LocalDateTime endTime = startTime.plusHours(RESERVATION_DURATION_HOURS);

        if (isTimeNotAvailable(startTime, endTime)) {
            log.warn(LogCategory.reservation("Requested reservation time is not available: {} to {}"), startTime, endTime);
            throw new TimeNotValidException("The requested reservation time is out of operating hours or invalid.");
        }

        if (!isOnTheHour(startTime)) {
            log.warn(LogCategory.reservation("Requested reservation time is not on the hour: {}"), startTime);
            throw new TimeNotValidException("Reservations must be made on the hour (e.g., 10:00, 11:00).");
        }

        if (notCorrectCapacity(diningTable, createReservationDto.getNumberOfGuests())) {
            log.warn(LogCategory.reservation("Dining table id: {} cannot accommodate {} guests"),
                    diningTable.getId(), createReservationDto.getNumberOfGuests());
            throw new CapacityExceededException("The selected dining table cannot accommodate the number of guests.");
        }

        if (isTableOverlapped(diningTable, startTime, endTime)) {
            log.warn(LogCategory.reservation("Dining table id: {} is not available from {} to {}"),
                    diningTable.getId(), startTime, endTime);
            throw new TableNotAvailableException("The selected dining table is not available at the requested time.");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .diningTable(diningTable)
                .status(ReservationStatus.CONFIRMED)
                .phone(createReservationDto.getPhone())
                .numberOfGuests(createReservationDto.getNumberOfGuests())
                .startTime(startTime)
                .endTime(endTime)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        emailService.sendEmailReservationConfirmation(user, savedReservation);

        log.info(LogCategory.reservation("Reservation with id: {} created successfully for user with id: {}"),
                reservation.getId(), userId);
        return ReservationMapper.toDto(savedReservation);
    }

    @Override
    public ReservationDto updateReservation(
            Long userId,
            Long reservationId,
            UpdateReservationDto updateReservationDto
    ) {
        log.info(LogCategory.reservation("Updating reservation with id: {}"), reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            log.warn(LogCategory.reservation("User with id: {} is not authorized to update reservation with id: {}"),
                    userId, reservationId);
            throw new IllegalArgumentException("You are not authorized to update this reservation.");
        }

        if (updateReservationDto.getPhone() != null) {
            log.info(LogCategory.reservation("Updating phone number for reservation id: {}"), reservationId);
            reservation.setPhone(updateReservationDto.getPhone());
        }

        if (updateReservationDto.getNumberOfGuests() != null) {
            if (notCorrectCapacity(
                    reservation.getDiningTable(),
                    updateReservationDto.getNumberOfGuests())) {
                log.warn(LogCategory.reservation("Dining table id: {} cannot accommodate updated number of guests: {}"),
                        reservation.getDiningTable().getId(), updateReservationDto.getNumberOfGuests());
                throw new CapacityExceededException("The selected dining table cannot accommodate the updated number of guests, please cancel and create a new reservation.");
            }
            log.info(LogCategory.reservation("Updating number of guests for reservation id: {}"), reservationId);
            reservation.setNumberOfGuests(updateReservationDto.getNumberOfGuests());
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        // Send email to notify user about updated reservation details
        emailService.sendReservationUpdatedEmail(reservation.getUser(), savedReservation);

        log.info(LogCategory.reservation("Reservation with id: {} updated successfully"), reservationId);
        return ReservationMapper.toDto(savedReservation);
    }

    @Override
    public ReservationDto markReservationAsArrived(Long reservationId) {
        log.info(LogCategory.reservation("Marking reservation with id: {} as arrived"), reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            log.warn(LogCategory.reservation("Only confirmed reservations can be marked as arrived. Reservation id: {} has status: {}"),
                    reservationId, reservation.getStatus());
            throw new IllegalArgumentException("Only confirmed reservations can be marked as arrived.");
        }
        reservation.setStatus(ReservationStatus.ARRIVED);

        log.info(LogCategory.reservation("Reservation with id: {} marked as arrived successfully"), reservationId);
        return ReservationMapper.toDto(reservationRepository.save(reservation));
    }

    @Override
    public ReservationDto cancelReservation(Long userId, Long reservationId) {
        log.info(LogCategory.reservation("Cancelling reservation with id: {}"), reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            log.warn(LogCategory.reservation("User with id: {} is not authorized to cancel reservation with id: {}"),
                    userId, reservationId);
            throw new IllegalArgumentException("You are not authorized to cancel this reservation.");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            log.warn(LogCategory.reservation("Only confirmed reservations can be cancelled. Reservation id: {} has status: {}"),
                    reservationId, reservation.getStatus());
            throw new IllegalArgumentException("Only confirmed reservations can be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation savedReservation = reservationRepository.save(reservation);

        // Send cancellation email
        emailService.sendReservationCancelledEmail(reservation.getUser(), savedReservation);

        log.info(LogCategory.reservation("Reservation with id: {} cancelled successfully"), reservationId);
        return ReservationMapper.toDto(savedReservation);
    }

    @Override
    public void deleteReservation(Long reservationId) {
        log.info(LogCategory.reservation("Deleting reservation with id: {}"), reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + reservationId));

        reservationRepository.delete(reservation);

        log.info(LogCategory.reservation("Reservation with id: {} deleted successfully"), reservationId);
    }

    @Override
    public List<DiningTableDto> getAvailableDiningTables(LocalDate date, LocalTime time, int numberOfGuests) {
        log.info(LogCategory.table("Get available dining tables for {} guests on {} at {}"),
                numberOfGuests, date, time);

        int requiredCapacity = getRequiredCapacity(numberOfGuests);
        LocalDateTime startTime = LocalDateTime.of(date, time);
        LocalDateTime endTime = startTime.plusHours(RESERVATION_DURATION_HOURS);

        if (numberOfGuests <= 0) {
            log.warn(LogCategory.table("Invalid number of guests: {}"), numberOfGuests);
            throw new IllegalArgumentException("Number of guests must be greater than zero");
        }

        if (!isOnTheHour(startTime)) {
            log.warn(LogCategory.table("Requested reservation time is not on the hour: {}"), startTime);
            throw new IllegalArgumentException("Reservations must be made on the hour (e.g., 10:00, 11:00)");
        }

        if (isTimeNotAvailable(startTime, endTime)) {
            log.warn(LogCategory.table("Reservations are not allowed at the requested time: {}"), startTime);
            throw new IllegalArgumentException("Reservations are not allowed at the requested time");
        }

        List<DiningTable> availableTables = diningTableRepository.findAvailableTables(
                requiredCapacity,
                DiningTableStatus.OPEN,
                startTime,
                endTime
        );

        log.info(LogCategory.table("Found {} available tables"), availableTables.size());
        return availableTables.stream()
                .map(DiningTableMapper::toDto)
                .toList();
    }

    @Override
    public List<DiningTableDto> getAllDiningTables() {
        log.info(LogCategory.table("Fetch all dining tables"));

        List<DiningTable> diningTables = diningTableRepository.findAll();

        log.info(LogCategory.table("Total dining tables found: {}"), diningTables.size());
        return diningTables.stream()
                .map(DiningTableMapper::toDto)
                .toList();
    }

    @Override
    public DiningTableDto getDiningTableById(Long tableId) {
        log.info(LogCategory.table("Fetching dining table with id: {}"), tableId);

        DiningTable diningTable = diningTableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Dining table not found with id: " + tableId));

        log.info(LogCategory.table("Found dining table with id: {}"), tableId);
        return DiningTableMapper.toDto(diningTable);
    }

    @Override
    public DiningTableDto createDiningTable(CreateDiningTableDto createDiningTableDto) {
        log.info(LogCategory.table("Creating new dining table with name: {}"), createDiningTableDto.getName());

        if (diningTableRepository.existsByName(createDiningTableDto.getName())) {
            log.warn(LogCategory.table("Duplicate dining table name: {}"), createDiningTableDto.getName());
            throw new DuplicateTableNameException("Dining table with name already exists: " + createDiningTableDto.getName());
        }

        DiningTable newDiningTable = DiningTable.builder()
                .name(createDiningTableDto.getName())
                .floor(createDiningTableDto.getFloor())
                .capacity(createDiningTableDto.getCapacity())
                .status(DiningTableStatus.OPEN)
                .build();

        log.info(LogCategory.user("Dining table {} created successfully"), createDiningTableDto.getName());
        return DiningTableMapper.toDto(diningTableRepository.save(newDiningTable));
    }

    @Override
    public DiningTableDto updateDiningTable(Long id, UpdateDiningTableDto updateDiningTableDto) {
        log.info(LogCategory.table("Updating dining table with id: {}"), id);

        DiningTable existingDiningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dining table not found with id: " + id));

        if (updateDiningTableDto.getName() != null) {
            log.info(LogCategory.table("Updating name to: {}"), updateDiningTableDto.getName());

            if (!existingDiningTable.getName().equals(updateDiningTableDto.getName()) &&
                    diningTableRepository.existsByName(updateDiningTableDto.getName())) {
                log.warn(LogCategory.table("Duplicate dining table name: {}"), updateDiningTableDto.getName());
                throw new DuplicateTableNameException("Dining table with name already exists: " + updateDiningTableDto.getName());
            }

            existingDiningTable.setName(updateDiningTableDto.getName());
        }

        if (updateDiningTableDto.getFloor() != null) {
            log.info(LogCategory.table("Updating floor to: {}"), updateDiningTableDto.getFloor());
            existingDiningTable.setFloor(updateDiningTableDto.getFloor());
        }

        if (updateDiningTableDto.getCapacity() != null) {
            log.info(LogCategory.table("Updating capacity to: {}"), updateDiningTableDto.getCapacity());
            existingDiningTable.setCapacity(updateDiningTableDto.getCapacity());
        }

        if (updateDiningTableDto.getStatus() != null) {
            log.info(LogCategory.table("Updating status to: {}"), updateDiningTableDto.getStatus());
            existingDiningTable.setStatus(updateDiningTableDto.getStatus());
        }

        DiningTable updatedDiningTable = diningTableRepository.save(existingDiningTable);

        log.info(LogCategory.table("Dining table with id: {} updated successfully"), id);
        return DiningTableMapper.toDto(updatedDiningTable);
    }

    @Override
    public void deleteDiningTable(Long id) {
        log.info(LogCategory.table("Deleting dining table with id: {}"), id);

        DiningTable existingDiningTable = diningTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dining table not found with id: " + id));
        diningTableRepository.delete(existingDiningTable);

        log.info(LogCategory.table("Dining table with id: {} deleted successfully"), id);
    }

    private boolean notCorrectCapacity(DiningTable diningTable, int numberOfGuests) {
        int requiredCapacity = getRequiredCapacity(numberOfGuests);
        return diningTable.getCapacity() != requiredCapacity;
    }

    private int getRequiredCapacity(int numberOfGuests) {
        if (numberOfGuests <= 2) {
            return 2;
        } else if (numberOfGuests <= 4) {
            return 4;
        } else if (numberOfGuests <= 8) {
            return 8;
        } else if (numberOfGuests <= 10) {
            return 10;
        } else {
            throw new CapacityExceededException("Maximum capacity exceeded");
        }
    }

    /**
     * Checks if the requested reservation time is:
     * 1. Within operating hours
     * 2. Not in the past and only 2 weeks in advance (round up to 10PM of the last day)
     * 3. At least 30 minutes from now
     * @param reservationStart The requested reservation start time
     * @param reservationEnd The requested reservation end time
     * @return true if the time is available, false otherwise
     */
    private boolean isTimeNotAvailable(LocalDateTime reservationStart, LocalDateTime reservationEnd) {
        if (outOfReservationTime(reservationStart, reservationEnd)) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxAdvanceTime = now.plusWeeks(2).with(LAST_RESERVATION_TIME);
        if (reservationStart.isBefore(now) || reservationEnd.isAfter(maxAdvanceTime)) {
            return true;
        }

        if (reservationStart.isBefore(now.plusMinutes(30))) {
            return true;
        }

        return false;
    }

    private boolean isOnTheHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
    }

    private boolean outOfReservationTime(LocalDateTime reservationStart, LocalDateTime reservationEnd) {
        return reservationStart.toLocalTime().isBefore(OPENING_TIME) ||
                reservationEnd.toLocalTime().isAfter(LAST_RESERVATION_TIME);
    }

    // private boolean satisfiesThirtyMinutesRule(LocalDateTime reservationStart) {
    //     LocalDateTime now = LocalDateTime.now();
    //     return reservationStart.isAfter(now.plusMinutes(30));
    // }

    private boolean isTableOverlapped(DiningTable diningTable, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.existsOverlappingReservations(
                diningTable.getId(),
                startTime,
                endTime
        );
    }
}
