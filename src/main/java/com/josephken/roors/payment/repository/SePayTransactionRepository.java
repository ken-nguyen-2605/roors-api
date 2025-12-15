// src/main/java/com/josephken/roors/payment/repository/SePayTransactionRepository.java
package com.josephken.roors.payment.repository;

import com.josephken.roors.payment.entity.SePayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SePayTransactionRepository extends JpaRepository<SePayTransaction, Long> {

    boolean existsBySepayId(Long sepayId);

    Optional<SePayTransaction> findBySepayId(Long sepayId);

    Optional<SePayTransaction> findByCode(String code);

    List<SePayTransaction> findByCodeOrderByCreatedAtDesc(String code);

    Optional<SePayTransaction> findByReferenceCode(String referenceCode);

    boolean existsByReferenceCode(String referenceCode);

    // Find by matched order number
    List<SePayTransaction> findByMatchedOrderNumber(String orderNumber);
}