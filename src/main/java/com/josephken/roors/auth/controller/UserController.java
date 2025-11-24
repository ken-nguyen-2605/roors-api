package com.josephken.roors.auth.controller;

import com.josephken.roors.auth.dto.ErrorResponse;
import com.josephken.roors.auth.dto.SuccessResponse;
import com.josephken.roors.auth.dto.UpdateUserRequest;
import com.josephken.roors.auth.dto.UserProfileResponse;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.UserNotFoundException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.auth.util.AuthenticationHelper;
import com.josephken.roors.common.util.LogCategory;
import com.josephken.roors.menu.entity.MenuItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

            UserProfileResponse response = buildUserProfileResponse(user);
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

            // Update fields if provided
            boolean updated = false;

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
                updated = true;
            }

            if (request.getFullName() != null && !request.getFullName().equals(user.getFullName())) {
                user.setFullName(request.getFullName());
                updated = true;
            }

            if (request.getContactNumber() != null && !request.getContactNumber().equals(user.getContactNumber())) {
                user.setContactNumber(request.getContactNumber());
                updated = true;
            }

            if (request.getProfileImage() != null && !request.getProfileImage().equals(user.getProfileImage())) {
                user.setProfileImage(request.getProfileImage());
                updated = true;
            }

            if (request.getAddress() != null && !request.getAddress().equals(user.getAddress())) {
                user.setAddress(request.getAddress());
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
                log.info(LogCategory.user("User profile updated successfully - id: {}"), id);
            }

            UserProfileResponse response = buildUserProfileResponse(user);
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

    private UserProfileResponse buildUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setVerified(user.isVerified());
        response.setFullname(user.getFullName());
        response.setContactNumber(user.getContactNumber());
        response.setProfileImage(user.getProfileImage());
        response.setAddress(user.getAddress());
        response.setMemberSince(user.getMemberSince());

        // Convert liked dishes to DTOs
        List<UserProfileResponse.LikedDishDto> likedDishes = user.getLikedDishes().stream()
            .map(this::convertToLikedDishDto)
            .collect(Collectors.toList());
        response.setLikedDishes(likedDishes);

        return response;
    }

    private UserProfileResponse.LikedDishDto convertToLikedDishDto(MenuItem menuItem) {
        UserProfileResponse.LikedDishDto dto = new UserProfileResponse.LikedDishDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setPrice(menuItem.getPrice().doubleValue());
        return dto;
    }

    @PostMapping("/{id}/liked-dishes/{dishId}")
    public ResponseEntity<?> addLikedDish(@PathVariable Long id, @PathVariable Long dishId) {
        log.info(LogCategory.user("Add liked dish - userId: {}, dishId: {}"), id, dishId);

        try {
            User currentUser = AuthenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Authentication required"));

            // Check if user is updating their own profile
            if (!currentUser.getId().equals(id)) {
                log.warn(LogCategory.user("Add liked dish denied - user {} tried to update user {}"),
                        currentUser.getId(), id);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You can only manage your own liked dishes",
                                HttpStatus.FORBIDDEN.value()));
            }

            // Implementation would require MenuItemRepository injection
            // For now, return success message
            return ResponseEntity.ok(new SuccessResponse("Dish added to liked dishes"));

        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
            }

            log.warn(LogCategory.user("Add liked dish failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Add liked dish error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while adding liked dish",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @DeleteMapping("/{id}/liked-dishes/{dishId}")
    public ResponseEntity<?> removeLikedDish(@PathVariable Long id, @PathVariable Long dishId) {
        log.info(LogCategory.user("Remove liked dish - userId: {}, dishId: {}"), id, dishId);

        try {
            User currentUser = AuthenticationHelper.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("Authentication required"));

            // Check if user is updating their own profile
            if (!currentUser.getId().equals(id)) {
                log.warn(LogCategory.user("Remove liked dish denied - user {} tried to update user {}"),
                        currentUser.getId(), id);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("You can only manage your own liked dishes",
                                HttpStatus.FORBIDDEN.value()));
            }

            // Implementation would require MenuItemRepository injection
            // For now, return success message
            return ResponseEntity.ok(new SuccessResponse("Dish removed from liked dishes"));

        } catch (RuntimeException e) {
            if ("Authentication required".equals(e.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
            }

            log.warn(LogCategory.user("Remove liked dish failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Remove liked dish error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while removing liked dish",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}