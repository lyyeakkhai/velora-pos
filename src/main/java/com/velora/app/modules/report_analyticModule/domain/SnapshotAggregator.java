package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contract for all snapshot aggregation pipelines.
 *
 * <p>Implementations extend {@link com.velora.app.common.AbstractSnapshotAggregator} and
 * implement this interface to add a human-readable aggregator name.
 *
 * @param <T> the snapshot type produced by this aggregator
 * Requirements: 20.1
 */
public interface SnapshotAggregator<T> {

    /**
     * Returns a human-readable name identifying this aggregator.
     */
    String getAggregatorName();

    /**
     * Returns true if a snapshot already exists for the given shop and date.
     */
    boolean alreadyExists(UUID shopId, LocalDate date);

    /**
     * Aggregates and returns the snapshot for the given shop and date.
     */
    T aggregate(UUID shopId, LocalDate date);

    /**
     * Persists the aggregated snapshot.
     */
    void persist(T snapshot);

    /**
     * Template method: checks idempotency, aggregates, and persists.
     * Skips if snapshot already exists.
     */
    default void run(UUID shopId, LocalDate date) {
        if (alreadyExists(shopId, date)) return;
        T snapshot = aggregate(shopId, date);
        persist(snapshot);
    }
}

