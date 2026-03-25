package com.velora.app.modules.report_analyticModule.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.modules.sale_managementModule.domain.TransactionRunner;
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

                // Determine order count for DailySnapshotAggregator
                List<OrderFact> orders = orderReadRepository.findConfirmedOrdersForShopAndDate(shopId, snapshotDate);

                // Build aggregators in dependency order: product → category → daily
                List<SnapshotAggregator<?>> aggregators = new ArrayList<>();
                aggregators.add(new ProductSnapshotAggregator(
                        orderReadRepository, orderItemReadRepository, inventoryReadRepository,
                        productSnapshotRepository, allowVerifiedLossProfitNegative));
                aggregators.add(new CategorySnapshotAggregator(
                        productSnapshotRepository, categorySnapshotRepository));
                aggregators.add(new DailySnapshotAggregator(
                        categorySnapshotRepository, dailySnapshotRepository, orgId, orders.size()));

                for (SnapshotAggregator<?> aggregator : aggregators) {
                    aggregator.run(shopId, snapshotDate);
                }

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
}
