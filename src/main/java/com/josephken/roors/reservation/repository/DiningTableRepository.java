package com.josephken.roors.reservation.repository;

import com.josephken.roors.reservation.entity.DiningTable;
import com.josephken.roors.reservation.entity.DiningTableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    boolean existsByName(String name);
    @Query("SELECT t FROM DiningTable t " +
            "WHERE t.capacity = :capacity " +
            "AND t.status = :status " +
            "AND t.id NOT IN (" +
            "   SELECT r.diningTable.id FROM Reservation r " +
            "   WHERE (" +
            "       (r.startTime < :endTime AND r.endTime > :startTime)" +
            "   )" +
            ")")
    List<DiningTable> findAvailableTables(
            @Param("capacity") Integer capacity,
            @Param("status") DiningTableStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
