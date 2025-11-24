package com.josephken.roors.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private boolean verified;
    private String fullname;
    private String contactNumber;
    private String profileImage;
    private String address;
    private LocalDateTime memberSince;
    private List<LikedDishDto> likedDishes;
    
    @Data
    public static class LikedDishDto {
        private Long id;
        private String name;
        private String imageUrl;
        private Double price;
    }
}
