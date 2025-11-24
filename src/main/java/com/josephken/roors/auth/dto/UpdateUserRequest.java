package com.josephken.roors.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
    
    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;
    
    private String profileImage;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
