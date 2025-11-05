package com.josephken.roors.auth.service;

import com.josephken.roors.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@roors.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification - Roors API");

            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;

            String emailBody = "Welcome to Roors API!\n\n" +
                    "Please verify your email address by clicking the link below:\n" +
                    verificationUrl + "\n\n" +
                    "Or use this token in your API request:\n" +
                    verificationToken + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "Best regards,\n" +
                    "Roors API Team";

            message.setText(emailBody);

            mailSender.send(message);
            log.info(LogCategory.system("Verification email sent successfully - recipient: {}"), toEmail);

        } catch (Exception e) {
            log.error(LogCategory.system("Failed to send verification email - recipient: {}, error: {}"), toEmail, e.getMessage());
            log.error(LogCategory.error("Email configuration may be incorrect. Check application.properties"));
            throw new RuntimeException("Failed to send verification email. Please contact support.", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Roors API");
            
            String resetUrl = baseUrl + "/api/auth/reset-password?token=" + resetToken;
            
            String emailBody = "Hello,\n\n" +
                    "You have requested to reset your password.\n\n" +
                    "Please click the link below to reset your password:\n" +
                    resetUrl + "\n\n" +
                    "Or use this token in your API request:\n" +
                    resetToken + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "If you did not request this password reset, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Roors API Team";
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info(LogCategory.system("Password reset email sent successfully - recipient: {}"), toEmail);
            
        } catch (Exception e) {
            log.error(LogCategory.system("Failed to send password reset email - recipient: {}, error: {}"), toEmail, e.getMessage());
            log.error(LogCategory.error("Email configuration may be incorrect. Check application.properties"));
            throw new RuntimeException("Failed to send password reset email. Please contact support.", e);
        }
    }
}
