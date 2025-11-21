package com.josephken.roors.auth.scheduler;

import com.josephken.roors.auth.repository.RefreshTokenRepository;
import com.josephken.roors.common.util.LogCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository repo;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanup() {
        Instant now = Instant.now();
        repo.deleteAllByExpiresAtBefore(now);
        log.info(LogCategory.system("Expired refresh tokens cleanup executed at " + now));
    }
}