package com.velora.app.core.domain.payment;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Domain repository interface for PlatformRevenueSnapshot persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 */
public interface PlatformRevenueSnapshotRepository {

    /**
     * Persists a platform revenue snapshot and returns the saved instance.
     *
     * @param snapshot The snapshot to save (cannot be null)
     * @return The saved snapshot
     */
    PlatformRevenueSnapshot save(PlatformRevenueSnapshot snapshot);

    /**
     * Finds a platform revenue snapshot for a specific date.
     *
     * @param date The snapshot date
     * @return An Optional containing the snapshot, or empty if not found
     */
    Optional<PlatformRevenueSnapshot> findByDate(LocalDate date);
}
