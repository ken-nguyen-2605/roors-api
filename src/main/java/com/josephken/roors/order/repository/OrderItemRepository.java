package com.josephken.roors.order.repository;

import com.josephken.roors.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT new com.josephken.roors.admin.dto.TopSellingItem(oi.menuItem.id, oi.menuItem.name, SUM(oi.quantity)) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.menuItem.id, oi.menuItem.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<com.josephken.roors.admin.dto.TopSellingItem> findTopSellingItems();
}
