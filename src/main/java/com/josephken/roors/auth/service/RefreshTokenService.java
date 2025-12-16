package com.josephken.roors.auth.service;

import com.josephken.roors.auth.entity.RefreshToken;
import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.exception.InvalidTokenException;
import com.josephken.roors.auth.repository.RefreshTokenRepository;
import com.josephken.roors.auth.util.TokenUtils;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void revokeTokensByFamilyId(UUID familyId) {
        log.info(LogCategory.user("Revoking refresh tokens with family ID: " + familyId));
        refreshTokenRepository.revokeTokensByFamilyId(familyId);
    }

    public void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info(LogCategory.user("Revoked refresh token ID: " + refreshToken.getId()));
    }

    public List<RefreshToken> getActiveTokensForUser(Long userId) {
        log.info(LogCategory.user("Fetching active refresh tokens for user ID: " + userId));
        return refreshTokenRepository.findAllByUserIdAndRevokedFalseOrderByExpiresAtAsc(userId);
    }

    public RefreshToken getRefreshToken(String rawToken) throws InvalidTokenException {
        String hashedToken = TokenUtils.hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> {
                    log.warn(LogCategory.user("Invalid refresh token attempt."));
                    return new InvalidTokenException("Invalid refresh token.");
                });
    }

    public String createRefreshToken(User user) {
        UUID familyId = UUID.randomUUID();
        return createRefreshToken(user, familyId);
    }

    public String createRefreshToken(User user, UUID familyId) {
        String rawRefreshToken = TokenUtils.generateSecureToken();
        String hashedRefreshToken = TokenUtils.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(hashedRefreshToken)
                .familyId(familyId)
                .revoked(false)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .user(user)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info(LogCategory.user("Created new refresh token for user ID: " + user.getId() + " with family ID: " + familyId));
        return rawRefreshToken;
    }

    public void deleteOldestActiveTokens(Long userId) {
        List<RefreshToken> activeTokens = getActiveTokensForUser(userId);
        refreshTokenRepository.delete(activeTokens.get(0));
        log.info(LogCategory.user("Deleted oldest refresh token with ID {} for user ID: {}"), activeTokens.get(0).getId(), userId);
    }

//    public void deleteOldestTokenChain(Long userId) {
//        List<RefreshToken> activeTokens = getActiveTokensForUser(userId);
//        if (!activeTokens.isEmpty()) {
//            UUID oldestFamilyId = activeTokens.get(0).getFamilyId();
//            refreshTokenRepository.revokeTokensByFamilyId(oldestFamilyId);
//            log.info(LogCategory.user("Deleted oldest refresh token chain for user ID: " + userId + " with family ID: " + oldestFamilyId));
//        }
//    }
}
