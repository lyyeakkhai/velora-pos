package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for atomic daily product snapshots.
 */
public interface DailyProductSnapshotRepository {

    boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate);

    DailyProductSnapshot save(DailyProductSnapshot snapshot);

    List<DailyProductSnapshot> saveAll(List<DailyProductSnapshot> snapshots);

    List<DailyProductSnapshot> findByShopAndDate(UUID shopId, LocalDate snapshotDate);

    List<DailyProductSnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive);
}

