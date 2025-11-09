package com.josephken.roors.auth.mapper;

import com.josephken.roors.auth.dto.UserDto;
import com.josephken.roors.auth.dto.UserSummaryDto;
import com.josephken.roors.auth.entity.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public static UserSummaryDto toSummaryDto(User user) {
        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }
}
