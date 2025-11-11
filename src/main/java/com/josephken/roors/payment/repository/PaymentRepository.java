package com.josephken.roors.payment.repository;

import com.josephken.roors.order.entity.Order;
import com.josephken.roors.payment.entity.Payment;
import com.josephken.roors.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentCode(String paymentCode);
    
    Optional<Payment> findByOrder(Order order);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime expiresAt);
    
    boolean existsByPaymentCode(String paymentCode);
}
