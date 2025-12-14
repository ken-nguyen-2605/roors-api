package com.josephken.roors.admin.dto;

import com.josephken.roors.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean verified;
    private boolean disabled;
}
