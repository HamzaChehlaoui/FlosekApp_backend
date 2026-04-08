package com.flosek.flosek.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.db.keepalive.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseKeepAliveScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(
            fixedDelayString = "${app.db.keepalive.interval-ms:240000}",
            initialDelayString = "${app.db.keepalive.initial-delay-ms:120000}"
    )
    public void keepDatabaseConnectionWarm() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("Database keep-alive ping succeeded");
        } catch (Exception ex) {
            // Avoid noisy errors while still indicating transient DB wake-up issues in logs.
            log.warn("Database keep-alive ping failed: {}", ex.getMessage());
        }
    }
}