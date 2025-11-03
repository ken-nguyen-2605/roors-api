package com.josephken.roors.auth.service;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.EmailNotFoundException;
import com.josephken.roors.auth.exception.InvalidTokenException;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.util.LogCategory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public AuthService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("No user found with email: " + email));

        String newVerifyToken = UUID.randomUUID().toString();

        user.setVerifyToken(newVerifyToken);
        user.setVerifyTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendVerificationEmail(email, newVerifyToken);
        log.info(LogCategory.user("Verification email resent - email: {}"), email);

    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (user.getVerifyTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }

        user.setVerified(true);
        user.setVerifyToken(null);
        user.setVerifyTokenExpiry(null);
        userRepository.save(user);

        log.info(LogCategory.user("Email verified successfully - userId: {}"), user.getId());
    }
}
