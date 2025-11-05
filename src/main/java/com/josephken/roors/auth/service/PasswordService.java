package com.josephken.roors.auth.service;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.EmailNotFoundException;
import com.josephken.roors.auth.exception.InvalidTokenException;
import com.josephken.roors.auth.exception.UserNotFoundException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String FORGOT_PASSWORD_RESPONSE = "Password reset email sent. Please check your inbox.";

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(
                        "Email not found: " + email,
                        email,
                        FORGOT_PASSWORD_RESPONSE
                ));

        // Check if there's already a valid (non-expired) reset token
        if (user.getResetToken() != null && 
            user.getResetTokenExpiry() != null && 
            user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
            
            // Reuse the existing valid token instead of generating a new one
            emailService.sendPasswordResetEmail(email, user.getResetToken());
            log.info(LogCategory.user("Password reset email sent with existing token - email: {}"), email);
            return;
        }

        // Generate new token only if no valid token exists
        String resetToken = UUID.randomUUID().toString();
        
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, resetToken);
        log.info(LogCategory.user("Password reset email sent with new token - email: {}"), email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
    }
}
