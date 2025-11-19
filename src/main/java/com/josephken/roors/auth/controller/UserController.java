package com.josephken.roors.auth.controller;

import com.josephken.roors.auth.dto.ErrorResponse;
import com.josephken.roors.auth.dto.UpdateUserRequest;
import com.josephken.roors.auth.dto.UserProfileResponse;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.UserNotFoundException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.auth.util.AuthenticationHelper;
import com.josephken.roors.common.util.LogCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        log.info(LogCategory.user("Get user profile - id: {}"), id);
        
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            UserProfileResponse response = new UserProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setVerified(user.isVerified());

            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            log.warn(LogCategory.user("Get user profile failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Get user profile error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while fetching user profile", 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        log.info(LogCategory.user("Update user profile - id: {}"), id);
        
        try {
            User currentUser = AuthenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Authentication required"));

            // Check if user is updating their own profile
            if (!currentUser.getId().equals(id)) {
                log.warn(LogCategory.user("Update user profile denied - user {} tried to update user {}"), 
                        currentUser.getId(), id);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You can only update your own profile", 
                                HttpStatus.FORBIDDEN.value()));
            }

            // Get user from database
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

            // Update email if provided
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                // Check if email is already taken
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Email is already in use", 
                                    HttpStatus.BAD_REQUEST.value()));
                }
                
                user.setEmail(request.getEmail());
                user.setVerified(false); // Require re-verification for new email
            }

            userRepository.save(user);
            log.info(LogCategory.user("User profile updated successfully - id: {}"), id);

            UserProfileResponse response = new UserProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setVerified(user.isVerified());

            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
            }
            
            log.warn(LogCategory.user("Update user profile failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(e instanceof UserNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), 
                            e instanceof UserNotFoundException ? HttpStatus.NOT_FOUND.value() : HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Update user profile error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating user profile", 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
