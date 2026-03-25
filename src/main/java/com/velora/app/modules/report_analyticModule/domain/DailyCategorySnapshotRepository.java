package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for daily category snapshots.
 */
public interface DailyCategorySnapshotRepository {

    boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate);

    DailyCategorySnapshot save(DailyCategorySnapshot snapshot);

    List<DailyCategorySnapshot> saveAll(List<DailyCategorySnapshot> snapshots);

    List<DailyCategorySnapshot> findByShopAndDate(UUID shopId, LocalDate snapshotDate);

    List<DailyCategorySnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive);
}

