package com.josephken.roors.order.repository;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.order.entity.Order;
import com.josephken.roors.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND " +
           "o.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
    
    long countByUserAndStatus(User user, OrderStatus status);
    
    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT new com.josephken.roors.admin.dto.OrderStatsByDate(CAST(o.createdAt AS LocalDate), COUNT(o), SUM(o.totalAmount)) " +
           "FROM Order o " +
           "WHERE o.createdAt >= :since " +
           "GROUP BY CAST(o.createdAt AS LocalDate) " +
           "ORDER BY CAST(o.createdAt AS LocalDate) ASC")
    List<com.josephken.roors.admin.dto.OrderStatsByDate> findOrderStatsByDate(@Param("since") LocalDateTime since);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status);
}
