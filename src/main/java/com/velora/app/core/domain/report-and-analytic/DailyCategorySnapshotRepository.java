package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for daily category snapshots.
 */
public interface DailyCategorySnapshotRepository {

    boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate);

    List<DailyCategorySnapshot> saveAll(List<DailyCategorySnapshot> snapshots);

    List<DailyCategorySnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive);
}
