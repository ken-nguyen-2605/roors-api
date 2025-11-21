package com.josephken.roors.auth.repository;

import com.josephken.roors.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.familyId = :familyId" )
    void revokeTokensByFamilyId(UUID familyId);

//    void deleteByUserIdAndExpiresAtBefore(Long userId, Instant now);

    List<RefreshToken> findAllByUserIdAndRevokedFalseOrderByExpiresAtAsc(Long userId);

    void deleteAllByExpiresAtBefore(Instant now);
}
