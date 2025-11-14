package com.josephken.roors.order.entity;

import com.josephken.roors.menu.entity.MenuItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "menu_item_name", nullable = false)
    private String menuItemName;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

     // NEW: Dish-specific rating
    @Column(name = "dish_rating")
    private Integer dishRating; // 1-5 stars

    @Column(name = "dish_feedback", columnDefinition = "TEXT")
    private String dishFeedback;

    @Column(name = "dish_rated_at")
    private LocalDateTime dishRatedAt;

    @Column(name = "admin_dish_response", columnDefinition = "TEXT")
    private String adminDishResponse;

    @Column(name = "dish_responded_at")
    private LocalDateTime dishRespondedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
