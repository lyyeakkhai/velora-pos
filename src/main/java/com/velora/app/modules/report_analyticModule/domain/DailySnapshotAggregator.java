package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshotAggregator;

/**
 * Aggregates category snapshots into a single {@link DailySnapshot} for the shop.
 *
 * <p>Reads already-persisted {@link DailyCategorySnapshot} records and sums gross revenue,
 * net profit, and order count into one top-level daily snapshot.
 *
 * <p>{@code orgId} is injected at construction time because the {@link SnapshotAggregator}
 * interface's {@code aggregate(shopId, date)} signature does not carry it.
 *
 * Requirements: 20.4
 */
public class DailySnapshotAggregator
        extends AbstractSnapshotAggregator<DailySnapshot>
        implements SnapshotAggregator<DailySnapshot> {

    private final DailyCategorySnapshotRepository categorySnapshotRepository;
    private final DailySnapshotRepository dailySnapshotRepository;
    private final UUID orgId;
    private final int orderCount;

    public DailySnapshotAggregator(
            DailyCategorySnapshotRepository categorySnapshotRepository,
            DailySnapshotRepository dailySnapshotRepository,
            UUID orgId,
            int orderCount) {
        this.categorySnapshotRepository = categorySnapshotRepository;
        this.dailySnapshotRepository = dailySnapshotRepository;
        this.orgId = orgId;
        this.orderCount = orderCount;
    }

    @Override
    public String getAggregatorName() {
        return "DailySnapshotAggregator";
    }

    @Override
    public boolean alreadyExists(UUID shopId, LocalDate date) {
        return dailySnapshotRepository.existsForShopAndDate(shopId, date);
    }

    @Override
    public DailySnapshot aggregate(UUID shopId, LocalDate date) {
        List<DailyCategorySnapshot> categorySnapshots = categorySnapshotRepository.findByShopAndDate(shopId, date);

        BigDecimal totalGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (DailyCategorySnapshot cs : categorySnapshots) {
            totalGross = totalGross.add(cs.getCatGrossRevenue());
            totalProfit = totalProfit.add(cs.getCatNetProfit());
        }

        return new DailySnapshot(UUID.randomUUID(), date, shopId, orgId,
                AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross"),
                AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit"),
                orderCount);
    }

    @Override
    public void persist(DailySnapshot snapshot) {
        dailySnapshotRepository.save(snapshot);
    }
}

