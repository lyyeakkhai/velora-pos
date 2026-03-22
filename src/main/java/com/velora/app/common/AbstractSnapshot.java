package com.velora.app.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable base class for all daily analytics snapshot entities.
 * All fields are write-once, set in the constructor — no public setters.
 *
 * Requirements: 7.1, 7.2, 7.4, 25.6
 */
public abstract class AbstractSnapshot {

    private final UUID snapshotId;
    private final LocalDate snapshotDate;
    private final UUID shopId;
    private final LocalDateTime createdAt;

    protected AbstractSnapshot(UUID snapshotId, LocalDate snapshotDate, UUID shopId) {
        if (snapshotId == null) throw new DomainException("snapshotId must not be null");
        if (snapshotDate == null) throw new DomainException("snapshotDate must not be null");
        if (shopId == null) throw new DomainException("shopId must not be null");
        this.snapshotId = snapshotId;
        this.snapshotDate = snapshotDate;
        this.shopId = shopId;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public UUID getShopId() {
        return shopId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
