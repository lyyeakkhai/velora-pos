package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for atomic daily product snapshots.
 */
public interface DailyProductSnapshotRepository {

    boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate);

    List<DailyProductSnapshot> saveAll(List<DailyProductSnapshot> snapshots);

    List<DailyProductSnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive);
}
