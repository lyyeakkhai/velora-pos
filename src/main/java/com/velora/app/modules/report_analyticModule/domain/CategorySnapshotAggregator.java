package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshotAggregator;

/**
 * Aggregates product snapshots into {@link DailyCategorySnapshot} records.
 *
 * <p>Reads already-persisted {@link DailyProductSnapshot} records for the given shop/date
 * and rolls them up by category.
 *
 * Requirements: 20.3
 */
public class CategorySnapshotAggregator
        extends AbstractSnapshotAggregator<List<DailyCategorySnapshot>>
        implements SnapshotAggregator<List<DailyCategorySnapshot>> {

    private final DailyProductSnapshotRepository productSnapshotRepository;
    private final DailyCategorySnapshotRepository categorySnapshotRepository;

    public CategorySnapshotAggregator(
            DailyProductSnapshotRepository productSnapshotRepository,
            DailyCategorySnapshotRepository categorySnapshotRepository) {
        this.productSnapshotRepository = productSnapshotRepository;
        this.categorySnapshotRepository = categorySnapshotRepository;
    }

    @Override
    public String getAggregatorName() {
        return "CategorySnapshotAggregator";
    }

    @Override
    public boolean alreadyExists(UUID shopId, LocalDate date) {
        return categorySnapshotRepository.existsForShopAndDate(shopId, date);
    }

    @Override
    public List<DailyCategorySnapshot> aggregate(UUID shopId, LocalDate date) {
        List<DailyProductSnapshot> productSnapshots = productSnapshotRepository.findByShopAndDate(shopId, date);

        Map<UUID, CategoryAccumulator> categoryAcc = new HashMap<>();
        for (DailyProductSnapshot ps : productSnapshots) {
            CategoryAccumulator acc = categoryAcc.computeIfAbsent(ps.getCategoryId(), id -> new CategoryAccumulator());
            BigDecimal gross = ps.getUnitSalePrice()
                    .multiply(new BigDecimal(ps.getQtySold()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal profit = ps.getUnitSalePrice().subtract(ps.getBaseCostPrice())
                    .multiply(new BigDecimal(ps.getQtySold()))
                    .setScale(2, RoundingMode.HALF_UP);
            acc.gross = acc.gross.add(gross);
            acc.profit = acc.profit.add(profit);
            acc.itemsSold += ps.getQtySold();
        }

        List<DailyCategorySnapshot> snapshots = new ArrayList<>();
        for (Map.Entry<UUID, CategoryAccumulator> entry : categoryAcc.entrySet()) {
            UUID categoryId = entry.getKey();
            CategoryAccumulator acc = entry.getValue();
            snapshots.add(new DailyCategorySnapshot(UUID.randomUUID(), date, shopId, categoryId,
                    AnalyticsMoney.normalizeNonNegative(acc.gross, "catGrossRevenue"),
                    AnalyticsMoney.normalizeSigned(acc.profit, "catNetProfit"),
                    acc.itemsSold));
        }

        return snapshots;
    }

    @Override
    public void persist(List<DailyCategorySnapshot> snapshots) {
        categorySnapshotRepository.saveAll(snapshots);
    }

    private static final class CategoryAccumulator {
        BigDecimal gross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal profit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int itemsSold;
    }
}

