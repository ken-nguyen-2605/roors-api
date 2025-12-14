package com.josephken.roors.auth.service;

import com.josephken.roors.auth.JwtUtil;
import com.josephken.roors.auth.dto.LoginResponse;
import com.josephken.roors.auth.dto.MessageResponse;
import com.josephken.roors.auth.dto.RegisterResponse;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.entity.UserRole;
import com.josephken.roors.auth.exception.EmailNotFoundException;
import com.josephken.roors.auth.exception.EmailNotVerifiedException;
import com.josephken.roors.auth.exception.InvalidTokenException;
import com.josephken.roors.auth.exception.UserAlreadyExistsException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.common.util.LogCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;

    private final String RESEND_EMAIL_RESPONSE = "Verification email resent. Please check your inbox.";
    private final String FORGOT_PASSWORD_RESPONSE = "Password reset email sent. Please check your inbox.";

    @Autowired
    public AuthService(UserRepository userRepository,
                          EmailService emailService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          PasswordService passwordService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.passwordService = passwordService;
    }

    public RegisterResponse register(String email, String username, String password) {
        log.info(LogCategory.user("Registration attempt - username: {}, email: {}"), username, email);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User already exists with username: " + username);
        }

        String verifyToken = UUID.randomUUID().toString();

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setVerified(false);
        newUser.setVerifyToken(verifyToken);
        newUser.setVerifyTokenExpiry(LocalDateTime.now().plusHours(24));
        newUser.setRole(UserRole.CUSTOMER);
        userRepository.save(newUser);
        log.info(LogCategory.user("Registration successful - username: {}"), username);

        emailService.sendVerificationEmail(email, verifyToken);
        log.info(LogCategory.user("Email verification sent - email: {}"), email);

        return new RegisterResponse(
                newUser.getUsername(),
                newUser.getEmail(),
                "User registered successfully. Please check your email to verify your account."
        );
    }

    public LoginResponse login(String username, String password) {
        log.info(LogCategory.user("Login attempt - username: {}"), username);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername());
        // Guard against legacy rows with null role
        var role = user.getRole() != null ? user.getRole().name() : UserRole.CUSTOMER.name();

        if (!user.isVerified()) {
            throw new EmailNotVerifiedException("Email not verified for user: " + username);
        }

        log.info(LogCategory.user("Login successful - username: {}"), username);
        return new LoginResponse(token, user.getUsername(), role);
    }

    public MessageResponse forgotPassword(String email) {
        log.info(LogCategory.user("Forgot password request - email: {}"), email);

        passwordService.initiatePasswordReset(email);
        return new MessageResponse(FORGOT_PASSWORD_RESPONSE);
    }

    public MessageResponse resetPassword(String token, String newPassword) {
        String shortenedToken = token.substring(0, Math.min(8, token.length()));
        log.info(LogCategory.user("Password reset attempt - token: {}..."), shortenedToken);

        passwordService.resetPassword(token, newPassword);
        log.info(LogCategory.user("Password reset successful - token: {}..."), shortenedToken);

        return new MessageResponse("Password has been reset successfully.");
    }

    public MessageResponse resendVerification(String email) {
        log.info(LogCategory.user("Resend verification email request - email: {}"), email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(
                        "No user found with email: " + email,
                        email,
                        RESEND_EMAIL_RESPONSE
                ));

        String newVerifyToken = UUID.randomUUID().toString();

        user.setVerifyToken(newVerifyToken);
        user.setVerifyTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendVerificationEmail(email, newVerifyToken);
        log.info(LogCategory.user("Verification email resent - email: {}"), email);

        return new MessageResponse(RESEND_EMAIL_RESPONSE);
    }

    public MessageResponse verifyEmail(String token) {
        String shortenedToken = token.substring(0, Math.min(8, token.length()));
        log.info(LogCategory.user("Email verification attempt - token: {}..."), shortenedToken);

        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getVerifyTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setVerified(true);
        user.setVerifyToken(null);
        user.setVerifyTokenExpiry(null);
        userRepository.save(user);

        log.info(LogCategory.user("Email verified successfully - username: {}, token: {}..."),
                user.getUsername(), shortenedToken);

        return new MessageResponse("Email has been verified successfully.");
    }
}
