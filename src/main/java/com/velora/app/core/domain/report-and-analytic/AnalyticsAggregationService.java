package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.core.domain.salemanagement.TransactionRunner;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Aggregates transactional read models into immutable analytics snapshots.
 */
public class AnalyticsAggregationService extends AbstractDomainService {

    private final TransactionRunner transactionRunner;
    private final AnalyticsJobLockStore lockStore;
    private final AnalyticsJobRunStore runStore;
    private final AnalyticsJobLogStore logStore;
    private final ConfirmedOrderReadRepository orderReadRepository;
    private final OrderItemReadRepository orderItemReadRepository;
    private final InventoryAnalyticsReadRepository inventoryReadRepository;
    private final DailyProductSnapshotRepository productSnapshotRepository;
    private final DailyCategorySnapshotRepository categorySnapshotRepository;
    private final DailySnapshotRepository dailySnapshotRepository;
    private final Clock clock;

    public AnalyticsAggregationService(TransactionRunner transactionRunner, AnalyticsJobLockStore lockStore,
            AnalyticsJobRunStore runStore, AnalyticsJobLogStore logStore,
            ConfirmedOrderReadRepository orderReadRepository,
            OrderItemReadRepository orderItemReadRepository, InventoryAnalyticsReadRepository inventoryReadRepository,
            DailyProductSnapshotRepository productSnapshotRepository,
            DailyCategorySnapshotRepository categorySnapshotRepository, DailySnapshotRepository dailySnapshotRepository,
            Clock clock) {
        requireNotNull(transactionRunner, "transactionRunner");
        this.transactionRunner = transactionRunner;
        requireNotNull(lockStore, "lockStore");
        this.lockStore = lockStore;
        requireNotNull(runStore, "runStore");
        this.runStore = runStore;
        requireNotNull(logStore, "logStore");
        this.logStore = logStore;
        requireNotNull(orderReadRepository, "orderReadRepository");
        this.orderReadRepository = orderReadRepository;
        requireNotNull(orderItemReadRepository, "orderItemReadRepository");
        this.orderItemReadRepository = orderItemReadRepository;
        requireNotNull(inventoryReadRepository, "inventoryReadRepository");
        this.inventoryReadRepository = inventoryReadRepository;
        requireNotNull(productSnapshotRepository, "productSnapshotRepository");
        this.productSnapshotRepository = productSnapshotRepository;
        requireNotNull(categorySnapshotRepository, "categorySnapshotRepository");
        this.categorySnapshotRepository = categorySnapshotRepository;
        requireNotNull(dailySnapshotRepository, "dailySnapshotRepository");
        this.dailySnapshotRepository = dailySnapshotRepository;
        requireNotNull(clock, "clock");
        this.clock = clock;
    }

    /**
     * Runs a full daily aggregation in a single database transaction.
     * <p>
     * Idempotent: if snapshot already exists, it will be skipped.
     */
    public AnalyticsAggregationResult runDailyAggregation(UUID shopId, UUID orgId, LocalDate snapshotDate,
            boolean allowVerifiedLossProfitNegative) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(orgId, "orgId");
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");

        if (dailySnapshotRepository.existsForShopAndDate(shopId, snapshotDate)
                || runStore.isCompleted(shopId, snapshotDate)) {
            return AnalyticsAggregationResult.SKIPPED_ALREADY_EXISTS;
        }

        AnalyticsJobLockStore.LockToken token = lockStore.tryAcquire(shopId, snapshotDate);
        if (token == null) {
            return AnalyticsAggregationResult.SKIPPED_LOCKED;
        }

        try {
            final AnalyticsAggregationResult[] result = new AnalyticsAggregationResult[] {
                    AnalyticsAggregationResult.COMPLETED };
            transactionRunner.runInTransaction(() -> {
                if (dailySnapshotRepository.existsForShopAndDate(shopId, snapshotDate)
                        || runStore.isCompleted(shopId, snapshotDate)) {
                    result[0] = AnalyticsAggregationResult.SKIPPED_ALREADY_EXISTS;
                    return;
                }

                logStore.log(shopId, snapshotDate, AnalyticsJobLogStore.LogLevel.INFO, "Aggregation started",
                        LocalDateTime.now(clock).atOffset(ZoneOffset.UTC).toLocalDateTime());

                List<OrderFact> orders = orderReadRepository.findConfirmedOrdersForShopAndDate(shopId, snapshotDate);
                Set<UUID> orderIds = new HashSet<>();
                for (OrderFact o : orders) {
                    orderIds.add(o.orderId());
                }

                List<OrderItemFact> items = orderIds.isEmpty() ? List.of()
                        : orderItemReadRepository.findItemsForOrders(shopId, new ArrayList<>(orderIds));
                Map<UUID, Integer> stockByVariant = new HashMap<>();
                for (StockAtMidnightFact stock : inventoryReadRepository.findStockAtMidnight(shopId, snapshotDate)) {
                    stockByVariant.put(stock.variantId(), stock.stockQuantity());
                }

                Aggregates aggregates = computeAggregates(shopId, orgId, snapshotDate, items, stockByVariant,
                        orders.size(), allowVerifiedLossProfitNegative);
                persistSnapshots(aggregates);
                runStore.markCompleted(shopId, snapshotDate);
                logStore.log(shopId, snapshotDate, AnalyticsJobLogStore.LogLevel.INFO, "Aggregation completed",
                        LocalDateTime.now(clock).atOffset(ZoneOffset.UTC).toLocalDateTime());
            });
            return result[0];
        } catch (RuntimeException ex) {
            logStore.log(shopId, snapshotDate, AnalyticsJobLogStore.LogLevel.ERROR,
                    "Aggregation failed: " + ex.getMessage(),
                    LocalDateTime.now(clock).atOffset(ZoneOffset.UTC).toLocalDateTime());
            throw ex;
        } finally {
            lockStore.release(token);
        }
    }

    public void validateSnapshotUniqueness(UUID shopId, LocalDate snapshotDate) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(snapshotDate, "snapshotDate");
        if (dailySnapshotRepository.existsForShopAndDate(shopId, snapshotDate)
                || runStore.isCompleted(shopId, snapshotDate)) {
            throw new IllegalStateException("Duplicate daily snapshot prohibited");
        }
    }

    public void persistSnapshots(Aggregates aggregates) {
        requireNotNull(aggregates, "aggregates");
        productSnapshotRepository.saveAll(aggregates.productSnapshots);
        categorySnapshotRepository.saveAll(aggregates.categorySnapshots);
        dailySnapshotRepository.save(aggregates.dailySnapshot);
    }

    private Aggregates computeAggregates(UUID shopId, UUID orgId, LocalDate snapshotDate, List<OrderItemFact> items,
            Map<UUID, Integer> stockByVariant, int orderCount, boolean allowVerifiedLossProfitNegative) {


        Map<ProductKey, ProductAccumulator> productAcc = new HashMap<>();
        for (OrderItemFact item : items) {
            ProductKey key = new ProductKey(item.productId(), item.variantId(), item.sellerId(), item.categoryId());
            ProductAccumulator acc = productAcc.computeIfAbsent(key, k -> new ProductAccumulator());
            acc.qtySold += item.quantity();
            acc.soldPriceSum = acc.soldPriceSum.add(AnalyticsMoney.normalizeNonNegative(item.soldPrice(), "soldPrice"));
            acc.costPriceSum = acc.costPriceSum
                    .add(AnalyticsMoney.normalizeNonNegative(item.baseCostPrice(), "baseCostPrice"));
            acc.lines += 1;
            acc.verifiedLoss = acc.verifiedLoss || item.verifiedLoss();
        }

        List<DailyProductSnapshot> productSnapshots = new ArrayList<>();
        Map<UUID, CategoryAccumulator> categoryAcc = new HashMap<>();

        for (Map.Entry<ProductKey, ProductAccumulator> entry : productAcc.entrySet()) {
            ProductKey key = entry.getKey();
            ProductAccumulator acc = entry.getValue();
            BigDecimal unitSalePrice = acc.lines == 0 ? BigDecimal.ZERO
                    : acc.soldPriceSum.divide(new BigDecimal(acc.lines), 2, RoundingMode.HALF_UP);
            BigDecimal baseCostPrice = acc.lines == 0 ? BigDecimal.ZERO
                    : acc.costPriceSum.divide(new BigDecimal(acc.lines), 2, RoundingMode.HALF_UP);

            int stockAtMidnight = stockByVariant.getOrDefault(key.variantId, 0);
            DailyProductSnapshot snapshot = new DailyProductSnapshot(UUID.randomUUID(), snapshotDate, shopId,
                    key.productId, key.variantId, key.sellerId, key.categoryId, acc.qtySold, baseCostPrice, unitSalePrice,
                    stockAtMidnight);
            productSnapshots.add(snapshot);

            BigDecimal gross = unitSalePrice.multiply(new BigDecimal(acc.qtySold)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal profit = unitSalePrice.subtract(baseCostPrice).multiply(new BigDecimal(acc.qtySold)).setScale(2,
                    RoundingMode.HALF_UP);

            if (gross.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Negative revenue prohibited");
            }
            if (profit.compareTo(BigDecimal.ZERO) < 0 && !(allowVerifiedLossProfitNegative || acc.verifiedLoss)) {
                throw new IllegalStateException("Negative profit requires verified loss");
            }

            CategoryAccumulator cat = categoryAcc.computeIfAbsent(key.categoryId, id -> new CategoryAccumulator());
            cat.gross = cat.gross.add(gross);
            cat.profit = cat.profit.add(profit);
            cat.itemsSold += acc.qtySold;
        }

        List<DailyCategorySnapshot> categorySnapshots = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalProfit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (Map.Entry<UUID, CategoryAccumulator> entry : categoryAcc.entrySet()) {
            UUID categoryId = entry.getKey();
            CategoryAccumulator acc = entry.getValue();
            BigDecimal gross = AnalyticsMoney.normalizeNonNegative(acc.gross, "catGrossRevenue");
            BigDecimal profit = AnalyticsMoney.normalizeSigned(acc.profit, "catNetProfit");
            DailyCategorySnapshot snap = new DailyCategorySnapshot(UUID.randomUUID(), snapshotDate, shopId, categoryId,
                    gross, profit, acc.itemsSold);
            categorySnapshots.add(snap);
            totalGross = totalGross.add(gross);
            totalProfit = totalProfit.add(profit);
        }

        DailySnapshot dailySnapshot = new DailySnapshot(UUID.randomUUID(), snapshotDate, shopId, orgId,
                AnalyticsMoney.normalizeNonNegative(totalGross, "totalGross"),
                AnalyticsMoney.normalizeSigned(totalProfit, "totalProfit"), orderCount);

        return new Aggregates(productSnapshots, categorySnapshots, dailySnapshot);
    }

    private static <T> T requireStatic(T value, String fieldName) {
        if (value == null) throw new com.velora.app.common.DomainException(fieldName + " must not be null");
        return value;
    }

    public static final class Aggregates {
        private final List<DailyProductSnapshot> productSnapshots;
        private final List<DailyCategorySnapshot> categorySnapshots;
        private final DailySnapshot dailySnapshot;

        public Aggregates(List<DailyProductSnapshot> productSnapshots, List<DailyCategorySnapshot> categorySnapshots,
                DailySnapshot dailySnapshot) {
            this.productSnapshots = requireStatic(productSnapshots, "productSnapshots");
            this.categorySnapshots = requireStatic(categorySnapshots, "categorySnapshots");
            this.dailySnapshot = requireStatic(dailySnapshot, "dailySnapshot");
        }
    }

    private static final class ProductKey {
        private final UUID productId;
        private final UUID variantId;
        private final UUID sellerId;
        private final UUID categoryId;

        private ProductKey(UUID productId, UUID variantId, UUID sellerId, UUID categoryId) {
            this.productId = productId;
            this.variantId = variantId;
            this.sellerId = sellerId;
            this.categoryId = categoryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ProductKey)) {
                return false;
            }
            ProductKey that = (ProductKey) o;
            return productId.equals(that.productId) && variantId.equals(that.variantId)
                    && sellerId.equals(that.sellerId)
                    && categoryId.equals(that.categoryId);
        }

        @Override
        public int hashCode() {
            int result = productId.hashCode();
            result = 31 * result + variantId.hashCode();
            result = 31 * result + sellerId.hashCode();
            result = 31 * result + categoryId.hashCode();
            return result;
        }
    }

    private static final class ProductAccumulator {
        private int qtySold;
        private BigDecimal soldPriceSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal costPriceSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private int lines;
        private boolean verifiedLoss;
    }

    private static final class CategoryAccumulator {
        private BigDecimal gross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal profit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private int itemsSold;
    }
}
