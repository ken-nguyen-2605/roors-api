package com.josephken.roors.order.repository;

import com.josephken.roors.order.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT new com.josephken.roors.admin.dto.TopSellingItem(" +
           "oi.menuItem.id, " +
           "oi.menuItem.name, " +
           "SUM(oi.quantity), " +
           "SUM(oi.subtotal)) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.menuItem.id, oi.menuItem.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<com.josephken.roors.admin.dto.TopSellingItem> findTopSellingItems();

    @Query("SELECT new com.josephken.roors.admin.dto.CategorySales(" +
           "COALESCE(oi.menuItem.category.name, 'Uncategorized'), " +
           "SUM(oi.quantity), " +
           "SUM(oi.subtotal)) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.menuItem.category.name " +
           "ORDER BY SUM(oi.subtotal) DESC")
    List<com.josephken.roors.admin.dto.CategorySales> findCategorySales();

    /**
     * Calculate average rating for a menu item from all rated order items
     */
    @Query("SELECT AVG(oi.dishRating) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId AND oi.dishRating IS NOT NULL")
    Double calculateAverageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);

    /**
     * Count total number of ratings for a menu item
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId AND oi.dishRating IS NOT NULL")
    Long countRatingsByMenuItemId(@Param("menuItemId") Long menuItemId);

    /**
     * Get recent dish ratings for a menu item, ordered by rating date descending
     * Uses DTO projection with explicit JOIN to avoid lazy loading issues
     */
    @Query("SELECT new com.josephken.roors.menu.dto.DishRatingResponse(" +
           "oi.dishRating, " +
           "oi.dishFeedback, " +
           "oi.dishRatedAt, " +
           "COALESCE(o.customerName, 'Anonymous')" +
           ") FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE oi.menuItem.id = :menuItemId AND oi.dishRating IS NOT NULL " +
           "ORDER BY oi.dishRatedAt DESC")
    List<com.josephken.roors.menu.dto.DishRatingResponse> findRecentRatingsByMenuItemId(@Param("menuItemId") Long menuItemId);
}
