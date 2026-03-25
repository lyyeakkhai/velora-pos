package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for high-level daily snapshots.
 */
public interface DailySnapshotRepository {

    boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate);

    DailySnapshot save(DailySnapshot snapshot);

    Optional<DailySnapshot> findByShopAndDate(UUID shopId, LocalDate snapshotDate);

    List<DailySnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive);
}

