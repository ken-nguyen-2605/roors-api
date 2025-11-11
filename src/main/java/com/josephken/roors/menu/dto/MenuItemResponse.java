package com.josephken.roors.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private CategoryResponse category;
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isFeatured;
    private Integer preparationTime;
    private Integer spicyLevel;
    private String ingredients;
    private String allergens;
    private Integer calories;
    private String servingSize;
    private Double rating;
    private Integer reviewCount;
    private Integer orderCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
