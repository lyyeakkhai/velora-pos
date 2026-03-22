package com.velora.app.common;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Template-method base for all snapshot aggregation pipelines.
 *
 * <p>Subclasses provide domain-specific aggregation and persistence logic.
 * The {@link #run(UUID, LocalDate)} method enforces the idempotency guarantee:
 * if a snapshot already exists for the given shop and date, aggregation is skipped.
 *
 * @param <T> the snapshot type produced by this aggregator
 */
public abstract class AbstractSnapshotAggregator<T> {

    /**
     * Aggregate a snapshot for the given shop and date.
     *
     * @param shopId the shop to aggregate for
     * @param date   the date to aggregate
     * @return the aggregated snapshot (never null)
     */
    public abstract T aggregate(UUID shopId, LocalDate date);

    /**
     * Persist the aggregated snapshot.
     *
     * @param snapshot the snapshot to persist
     */
    public abstract void persist(T snapshot);

    /**
     * Check whether a snapshot already exists for the given shop and date.
     * Subclasses query their respective repository.
     *
     * @param shopId the shop to check
     * @param date   the date to check
     * @return true if a snapshot already exists
     */
    public abstract boolean alreadyExists(UUID shopId, LocalDate date);

    /**
     * Run the aggregation pipeline for the given shop and date.
     *
     * <p>Execution order: {@code alreadyExists()} → {@code aggregate()} → {@code persist()}.
     * If {@code alreadyExists()} returns true the method returns immediately without
     * calling {@code aggregate()} or {@code persist()}, making the pipeline idempotent.
     *
     * @param shopId the shop to aggregate for
     * @param date   the date to aggregate
     */
    public final void run(UUID shopId, LocalDate date) {
        if (alreadyExists(shopId, date)) {
            return;
        }
        T snapshot = aggregate(shopId, date);
        persist(snapshot);
    }
}
