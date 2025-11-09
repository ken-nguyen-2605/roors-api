package com.josephken.roors.auth.controller;

import com.josephken.roors.auth.dto.*;
import com.josephken.roors.auth.exception.*;
import com.josephken.roors.auth.service.AuthService;
import com.josephken.roors.util.LogCategory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(
                        registerRequest.getEmail(),
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                ));
    }

    /* Change password required the user to be authenticated,
       so maybe better handled in UserController
    */
//
//    @PostMapping("/change-password")
//    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
//        try {
//            // Get the authenticated user's username
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            // Check if user is authenticated
//            if (authentication == null || !authentication.isAuthenticated() ||
//                "anonymousUser".equals(authentication.getPrincipal())) {
//                log.warn(LogCategory.user("Change password failed - User not authenticated"));
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(new ErrorResponse("Authentication required", HttpStatus.UNAUTHORIZED.value()));
//            }
//
//            String username = authentication.getName();
//            log.info(LogCategory.user("Change password attempt - user: {}"), username);
//
//            passwordService.changePassword(username, request.getOldPassword(), request.getNewPassword());
//
//            log.info(LogCategory.user("Password changed successfully - user: {}"), username);
//            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
//
//        } catch (RuntimeException e) {
//            log.warn(LogCategory.user("Change password failed - {}"), e.getMessage());
//            return ResponseEntity
//                    .status(HttpStatus.BAD_REQUEST)
//                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
//        }
//    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.resetPassword(request.getToken(), request.getNewPassword()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendEmailRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.resendVerification(request.getEmail()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.verifyEmail(token));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn(LogCategory.user("Login failed - Invalid credentials for username: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn(LogCategory.user("Registration failed - User already exists: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn(LogCategory.user("Login failed - User not found: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        log.warn(LogCategory.user("Login failed - Email not verified: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEmailNotFoundException(EmailNotFoundException ex) {
        log.warn(LogCategory.user("Email not found - {}"), ex.getMessage());
        // Always return 200 OK to prevent email enumeration
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new MessageResponse(ex.getResponseMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn(LogCategory.user("Invalid token - {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error(LogCategory.error("General error - {}"), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}