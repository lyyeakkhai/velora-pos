package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Persistence port for job locks to prevent concurrent aggregation runs.
 */
public interface AnalyticsJobLockStore {

    /**
     * Attempts to acquire a lock for (shopId, snapshotDate). Returns a token if
     * acquired, otherwise null.
     */
    LockToken tryAcquire(UUID shopId, LocalDate snapshotDate);

    void release(LockToken token);

    /**
     * Opaque lock token.
     */
    record LockToken(UUID shopId, LocalDate snapshotDate, String token) {
    }
}
