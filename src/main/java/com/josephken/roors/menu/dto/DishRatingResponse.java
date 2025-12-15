package com.josephken.roors.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishRatingResponse {
    private Integer rating;
    private String feedback;
    private LocalDateTime ratedAt;
    private String customerName; // From order
}

