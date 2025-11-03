package com.josephken.roors.auth.controller;

import com.josephken.roors.auth.JwtUtil;
import com.josephken.roors.auth.dto.*;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.EmailNotFoundException;
import com.josephken.roors.auth.exception.InvalidTokenException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.auth.service.AuthService;
import com.josephken.roors.auth.service.EmailService;
import com.josephken.roors.auth.service.PasswordService;
import com.josephken.roors.util.LogCategory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordService passwordService;
    private final EmailService emailService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          PasswordService passwordService,
                          EmailService emailService,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info(LogCategory.user("Registration attempt - username: {}, email: {}"), request.getUsername(), request.getEmail());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn(LogCategory.user("Registration failed - Username already taken: {}"), request.getUsername());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Username is already taken", HttpStatus.BAD_REQUEST.value()));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(LogCategory.user("Registration failed - Email already registered: {}"), request.getEmail());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Email is already registered", HttpStatus.BAD_REQUEST.value()));
        }

        // Generate verify token
        String verifyToken = UUID.randomUUID().toString();

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setVerified(false);
        user.setVerifyToken(verifyToken);
        user.setVerifyTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        log.info(LogCategory.user("Registration successful - username: {}"), request.getUsername());

        // Send verification email
        emailService.sendVerificationEmail(request.getEmail(), verifyToken);
        log.info(LogCategory.user("Verification email sent - email: {}"), request.getEmail());

        // Prepare response
        RegisterResponse response = new RegisterResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMessage("User registered successfully. Please check your email to verify your account.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        log.info(LogCategory.user("Login attempt - username: {}"), request.getUsername());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), 
                            request.getPassword()
                    )
            );

            // Get user details and generate token
            final User user = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(user.getUsername());

            if (!user.isVerified()) {
                log.warn(LogCategory.user("Login failed - Email not verified for username: {}"), request.getUsername());
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Email not verified. Please verify your email before logging in.", HttpStatus.FORBIDDEN.value()));
            }

            log.info(LogCategory.user("Login successful - username: {}"), request.getUsername());
            
            // Prepare response
            LoginResponse response = new LoginResponse();
            response.setToken(token);

            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            log.warn(LogCategory.user("Login failed - Invalid credentials for username: {}"), request.getUsername());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password", HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Login error - username: {}, error: {}"), request.getUsername(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during login", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Get the authenticated user's username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn(LogCategory.user("Change password failed - User not authenticated"));
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Authentication required", HttpStatus.UNAUTHORIZED.value()));
            }
            
            String username = authentication.getName();
            log.info(LogCategory.user("Change password attempt - user: {}"), username);
            
            passwordService.changePassword(username, request.getOldPassword(), request.getNewPassword());
            
            log.info(LogCategory.user("Password changed successfully - user: {}"), username);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
            
        } catch (RuntimeException e) {
            log.warn(LogCategory.user("Change password failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Change password error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while changing password", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info(LogCategory.user("Forgot password request - email: {}"), request.getEmail());
        try {
            passwordService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(new MessageResponse("Password reset link has been sent to your email"));
            
        } catch (RuntimeException e) {
            // For security reasons, don't reveal if email exists or not
            return ResponseEntity.ok(new MessageResponse("If the email exists, a password reset link has been sent"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while processing your request", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info(LogCategory.user("Password reset attempt - token: {}..."), request.getToken().substring(0, Math.min(8, request.getToken().length())));
        
        try {
            passwordService.resetPassword(request.getToken(), request.getNewPassword());
            log.info(LogCategory.user("Password reset successful - token: {}..."), request.getToken().substring(0, Math.min(8, request.getToken().length())));
            return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
            
        } catch (RuntimeException e) {
            log.warn(LogCategory.user("Password reset failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while resetting password", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendEmailRequest request) {
        log.info(LogCategory.user("Resend verification email request - email: {}"), request.getEmail());

        try {
            authService.resendVerification(request.getEmail());
            return ResponseEntity.ok(new MessageResponse("Verification email has been resent"));

        } catch (EmailNotFoundException e) {
            log.warn(LogCategory.user("Resend verification email failed - {}"), e.getMessage());
            // For security, do not reveal if email exists
            return ResponseEntity.ok(new MessageResponse("Verification email has been resent"));

        } catch (Exception e) {
            log.error(LogCategory.error("Resend verification email error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while resending verification email",
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        String shortenedToken = token.substring(0, Math.min(8, token.length()));

        log.info(LogCategory.user("Email verification attempt - token: {}..."), shortenedToken);

        try {
            authService.verifyEmail(token);
            log.info(LogCategory.user("Email verified successfully - token: {}..."), shortenedToken);
            return ResponseEntity.ok(new MessageResponse("Email has been verified successfully"));
        } catch (InvalidTokenException e) {
            log.warn(LogCategory.user("Email verification failed - {}"), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error(LogCategory.error("Email verification error - {}"), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while verifying email", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}