package com.josephken.roors.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {
    
    @NotBlank(message = "Menu item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String imageUrl;

    private Boolean isAvailable = true;

    private Boolean isFeatured = false;

    private Integer preparationTime;

    private Integer spicyLevel = 0;

    private String ingredients;

    private String allergens;

    private Integer calories;

    private String servingSize;
}
