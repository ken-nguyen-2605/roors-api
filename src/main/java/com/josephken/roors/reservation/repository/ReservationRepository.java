package com.josephken.roors.reservation.repository;

import com.josephken.roors.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByDiningTableId(Long diningTableId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.diningTable.id = :diningTableId " +
            "AND (r.startTime < :endTime AND r.endTime > :startTime)")
    boolean existsOverlappingReservations(
            @Param("diningTableId") Long diningTableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
