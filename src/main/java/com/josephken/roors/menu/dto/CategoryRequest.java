package com.josephken.roors.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    private Boolean isActive = true;
}
