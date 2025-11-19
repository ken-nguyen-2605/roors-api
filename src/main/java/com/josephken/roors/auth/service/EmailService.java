package com.josephken.roors.auth.service;

import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.order.entity.Order;
import com.josephken.roors.reservation.entity.Reservation;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@roors.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification - Roors API");

            String verificationUrl = baseUrl + "/auth/verify-email?token=" + verificationToken;

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

    @Async
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

    @Async
    public void sendEmailReservationConfirmation(User user, Reservation reservation) {
        String toEmail = user.getEmail();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reservation Confirmation - Roors API");

            LocalDate reservationDate = reservation.getStartTime().toLocalDate();
            String emailBody = "Hello " + user.getUsername() + ",\n\n" +
                    "Your reservation has been confirmed with the following details:\n\n" +
                    "Reservation ID: " + reservation.getId() + "\n" +
                    "Date: " + reservationDate + "\n" +
                    "Time: " + reservation.getStartTime().toLocalTime() + " to " + reservation.getEndTime().toLocalTime() + "\n" +
                    "Number of Guests: " + reservation.getNumberOfGuests() + "\n\n" +
                    "We look forward to serving you!\n\n" +
                    "Best regards,\n" +
                    "Roors API Team";

            message.setText(emailBody);

            mailSender.send(message);
            log.info(LogCategory.system("Reservation confirmation email sent successfully - recipient: {}"), toEmail);

        } catch (Exception e) {
            log.error(LogCategory.system("Failed to send reservation confirmation email - recipient: {}, error: {}"), toEmail, e.getMessage());
            log.error(LogCategory.error("Email configuration may be incorrect. Check application.properties"));
            throw new RuntimeException("Failed to send reservation confirmation email. Please contact support.", e);
        }
    }

    @Async
    public void sendEmailOrderConfirmation(User user, Order order) {
        String toEmail = user.getEmail();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - Roors API");

            // Send email with basic info and order items with quantities and prices
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Hello ").append(user.getUsername()).append(",\n\n")
                    .append("Your order has been confirmed with the following details:\n\n")
                    .append("Order Number: ").append(order.getOrderNumber()).append("\n")
                    .append("Order Type: ").append(order.getOrderType()).append("\n")
                    .append("Status: ").append(order.getStatus()).append("\n\n")
                    .append("Order Items:\n");
            order.getOrderItems().forEach(item -> {
                emailBody.append("- ").append(item.getMenuItem().getName())
                        .append(" x").append(item.getQuantity())
                        .append(" @ $").append(item.getUnitPrice()).append("\n");
            });
            emailBody.append("\nSubtotal: $").append(order.getSubtotal()).append("\n")
                    .append("Tax: $").append(order.getTaxAmount() != null ? order.getTaxAmount() : "0.00").append("\n")
                    .append("Delivery Fee: $").append(order.getDeliveryFee() != null ? order.getDeliveryFee() : "0.00").append("\n")
                    .append("Discount: $").append(order.getDiscountAmount() != null ? order.getDiscountAmount() : "0.00").append("\n")
                    .append("Total Amount: $").append(order.getTotalAmount()).append("\n\n")
                    .append("Thank you for ordering with us!\n\n")
                    .append("Best regards,\n")
                    .append("Roors API Team");

            message.setText(emailBody.toString());
            mailSender.send(message);
            log.info(LogCategory.system("Order confirmation email sent successfully - recipient: {}"), toEmail);
        } catch (Exception e) {
            log.error(LogCategory.system("Failed to send order confirmation email - recipient: {}, error: {}"), toEmail, e.getMessage());
            log.error(LogCategory.error("Email configuration may be incorrect. Check application.properties"));
            throw new RuntimeException("Failed to send order confirmation email. Please contact support.", e);
        }
    }

}
