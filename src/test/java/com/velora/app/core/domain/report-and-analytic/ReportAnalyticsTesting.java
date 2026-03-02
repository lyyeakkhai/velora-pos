package com.velora.app.core.domain.reportandanalytic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.salemanagement.TransactionRunner;

public class ReportAnalyticsTesting {

    @Test
    public void aggregation_createsSnapshots_andReportsCorrectTotals() {
        UUID shopId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 3, 1);

        UUID order1 = UUID.randomUUID();
        UUID order2 = UUID.randomUUID();
        UUID productA = UUID.randomUUID();
        UUID variantA = UUID.randomUUID();
        UUID productB = UUID.randomUUID();
        UUID variantB = UUID.randomUUID();
        UUID seller1 = UUID.randomUUID();
        UUID seller2 = UUID.randomUUID();
        UUID cat1 = UUID.randomUUID();
        UUID cat2 = UUID.randomUUID();

        InMemoryConfirmedOrderRepo orderRepo = new InMemoryConfirmedOrderRepo(
                List.of(new OrderFact(order1, shopId, orgId, LocalDateTime.now(ZoneOffset.UTC)),
                        new OrderFact(order2, shopId, orgId, LocalDateTime.now(ZoneOffset.UTC))));

        InMemoryOrderItemRepo itemRepo = new InMemoryOrderItemRepo(
                List.of(new OrderItemFact(order1, productA, variantA, seller1, cat1, 2, new BigDecimal("10"),
                                new BigDecimal("4"), false),
                        new OrderItemFact(order2, productA, variantA, seller1, cat1, 1, new BigDecimal("12"),
                                new BigDecimal("4"), false),
                        new OrderItemFact(order2, productB, variantB, seller2, cat2, 3, new BigDecimal("5"),
                                new BigDecimal("2"), false)));

        InMemoryInventoryRepo invRepo = new InMemoryInventoryRepo(
                List.of(new StockAtMidnightFact(variantA, 7), new StockAtMidnightFact(variantB, 2)));

        InMemoryDailyProductSnapshotRepo productSnaps = new InMemoryDailyProductSnapshotRepo();
        InMemoryDailyCategorySnapshotRepo categorySnaps = new InMemoryDailyCategorySnapshotRepo();
        InMemoryDailySnapshotRepo dailySnaps = new InMemoryDailySnapshotRepo();

        InMemoryLockStore lockStore = new InMemoryLockStore();
        InMemoryRunStore runStore = new InMemoryRunStore();
        InMemoryLogStore logStore = new InMemoryLogStore();

        TransactionRunner tx = work -> work.run();
        Clock clock = Clock.fixed(Instant.parse("2026-03-02T00:10:00Z"), ZoneOffset.UTC);

        AnalyticsAggregationService svc = new AnalyticsAggregationService(tx, lockStore, runStore, logStore, orderRepo,
                itemRepo, invRepo, productSnaps, categorySnaps, dailySnaps, clock);

        AnalyticsAggregationResult result = svc.runDailyAggregation(shopId, orgId, date, false);
        assertEquals(AnalyticsAggregationResult.COMPLETED, result);

        assertEquals(2, productSnaps.saved.size());
        DailyProductSnapshot snapA = productSnaps.findByVariant(variantA);
        assertNotNull(snapA);
        assertEquals(3, snapA.getQtySold());
        assertEquals(new BigDecimal("11.00"), snapA.getUnitSalePrice());
        assertEquals(new BigDecimal("4.00"), snapA.getBaseCostPrice());
        assertEquals(7, snapA.getStockAtMidnight());

        DailySnapshot daily = dailySnaps.findByShopAndDate(shopId, date).orElseThrow();
        assertEquals(new BigDecimal("48.00"), daily.getTotalGross());
        assertEquals(new BigDecimal("30.00"), daily.getTotalProfit());
        assertEquals(2, daily.getOrderCount());

        ReportingService reportingService = new ReportingService(dailySnaps);
        DailyReportDTO report = reportingService.getDailyReport(Role.RoleName.MANAGER, shopId, date);
        assertEquals(new BigDecimal("48.00"), report.totalGross());
        assertEquals(new BigDecimal("30.00"), report.totalProfit());
    }

    @Test
    public void aggregation_isIdempotent_skipsWhenSnapshotExists() {
        UUID shopId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 3, 1);

        InMemoryDailySnapshotRepo dailySnaps = new InMemoryDailySnapshotRepo();
        dailySnaps.save(new DailySnapshot(UUID.randomUUID(), date, orgId, shopId, new BigDecimal("0"),
                new BigDecimal("0"), 0, LocalDateTime.now(ZoneOffset.UTC)));

        AnalyticsAggregationService svc = new AnalyticsAggregationService(work -> work.run(), new InMemoryLockStore(),
                new InMemoryRunStore(), new InMemoryLogStore(), new InMemoryConfirmedOrderRepo(List.of()),
                new InMemoryOrderItemRepo(List.of()), new InMemoryInventoryRepo(List.of()),
                new InMemoryDailyProductSnapshotRepo(), new InMemoryDailyCategorySnapshotRepo(), dailySnaps,
                Clock.systemUTC());

        assertEquals(AnalyticsAggregationResult.SKIPPED_ALREADY_EXISTS, svc.runDailyAggregation(shopId, orgId, date, false));
    }

    @Test(expected = IllegalStateException.class)
    public void reporting_deniesSellerShopReports() {
        ReportingService svc = new ReportingService(new InMemoryDailySnapshotRepo());
        svc.getDailyReport(Role.RoleName.SELLER, UUID.randomUUID(), LocalDate.of(2026, 3, 1));
    }

    @Test
    public void sellerAnalytics_allowsSellerSelf_only() {
        UUID shopId = UUID.randomUUID();
        UUID seller = UUID.randomUUID();
        UUID otherSeller = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 2, 24);
        LocalDate end = LocalDate.of(2026, 3, 1);

        InMemoryDailyProductSnapshotRepo productSnaps = new InMemoryDailyProductSnapshotRepo();
        productSnaps.saveAll(List.of(new DailyProductSnapshot(UUID.randomUUID(), end, UUID.randomUUID(), UUID.randomUUID(),
                seller, UUID.randomUUID(), shopId, 1, new BigDecimal("2"), new BigDecimal("5"), 10,
                LocalDateTime.now(ZoneOffset.UTC))));

        SellerAnalyticsService svc = new SellerAnalyticsService(productSnaps);
        SellerPerformanceDTO perf = svc.getSellerPerformance(Role.RoleName.SELLER, seller, seller, shopId, start, end);
        assertEquals(seller, perf.sellerId());

        try {
            svc.getSellerPerformance(Role.RoleName.SELLER, seller, otherSeller, shopId, start, end);
            throw new AssertionError("Expected exception");
        } catch (IllegalStateException expected) {
            // ok
        }
    }

    @Test(expected = IllegalStateException.class)
    public void aggregation_rejectsNegativeProfit_withoutVerifiedLoss() {
        UUID shopId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 3, 1);

        UUID order1 = UUID.randomUUID();
        UUID productA = UUID.randomUUID();
        UUID variantA = UUID.randomUUID();
        UUID seller1 = UUID.randomUUID();
        UUID cat1 = UUID.randomUUID();

        InMemoryConfirmedOrderRepo orderRepo = new InMemoryConfirmedOrderRepo(
                List.of(new OrderFact(order1, shopId, orgId, LocalDateTime.now(ZoneOffset.UTC))));
        InMemoryOrderItemRepo itemRepo = new InMemoryOrderItemRepo(
                List.of(new OrderItemFact(order1, productA, variantA, seller1, cat1, 1, new BigDecimal("5"),
                        new BigDecimal("10"), false)));

        AnalyticsAggregationService svc = new AnalyticsAggregationService(work -> work.run(), new InMemoryLockStore(),
                new InMemoryRunStore(), new InMemoryLogStore(), orderRepo, itemRepo, new InMemoryInventoryRepo(List.of()),
                new InMemoryDailyProductSnapshotRepo(), new InMemoryDailyCategorySnapshotRepo(), new InMemoryDailySnapshotRepo(),
                Clock.systemUTC());

        svc.runDailyAggregation(shopId, orgId, date, false);
    }

    @Test
    public void forecast_detectRevenueDrop_allowsNegativeChangePercent() {
        UUID shopId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        InMemoryDailySnapshotRepo dailyRepo = new InMemoryDailySnapshotRepo();
        dailyRepo.save(new DailySnapshot(UUID.randomUUID(), LocalDate.of(2026, 2, 20), orgId, shopId,
                new BigDecimal("100"), new BigDecimal("20"), 1, LocalDateTime.now(ZoneOffset.UTC)));
        dailyRepo.save(new DailySnapshot(UUID.randomUUID(), LocalDate.of(2026, 2, 21), orgId, shopId,
                new BigDecimal("100"), new BigDecimal("20"), 1, LocalDateTime.now(ZoneOffset.UTC)));
        dailyRepo.save(new DailySnapshot(UUID.randomUUID(), LocalDate.of(2026, 2, 27), orgId, shopId,
                new BigDecimal("50"), new BigDecimal("10"), 1, LocalDateTime.now(ZoneOffset.UTC)));

        ForecastService forecast = new ForecastService(new InMemoryDailyProductSnapshotRepo(), dailyRepo);
        AnalyticsInsightDTO insight = forecast.detectRevenueDrop(Role.RoleName.MANAGER, shopId,
                new DateRange(LocalDate.of(2026, 2, 27), LocalDate.of(2026, 2, 27)),
                new DateRange(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21)));

        // previous 200, current 50 => -75%
        assertEquals(new BigDecimal("-75.00"), insight.changePercent());
        assertEquals(RiskLevel.HIGH, insight.riskLevel());
    }

    private static final class InMemoryDailyProductSnapshotRepo implements DailyProductSnapshotRepository {
        private final List<DailyProductSnapshot> saved = new ArrayList<>();

        @Override
        public boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate) {
            for (DailyProductSnapshot s : saved) {
                if (s.getShopId().equals(shopId) && s.getSnapshotDate().equals(snapshotDate)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DailyProductSnapshot> saveAll(List<DailyProductSnapshot> snapshots) {
            saved.addAll(snapshots);
            return snapshots;
        }

        @Override
        public List<DailyProductSnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive,
                LocalDate endInclusive) {
            List<DailyProductSnapshot> out = new ArrayList<>();
            for (DailyProductSnapshot s : saved) {
                if (!s.getShopId().equals(shopId)) {
                    continue;
                }
                if (s.getSnapshotDate().isBefore(startInclusive) || s.getSnapshotDate().isAfter(endInclusive)) {
                    continue;
                }
                out.add(s);
            }
            return out;
        }

        public DailyProductSnapshot findByVariant(UUID variantId) {
            for (DailyProductSnapshot s : saved) {
                if (s.getVariantId().equals(variantId)) {
                    return s;
                }
            }
            return null;
        }
    }

    private static final class InMemoryDailyCategorySnapshotRepo implements DailyCategorySnapshotRepository {
        private final List<DailyCategorySnapshot> saved = new ArrayList<>();

        @Override
        public boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate) {
            for (DailyCategorySnapshot s : saved) {
                if (s.getShopId().equals(shopId) && s.getSnapshotDate().equals(snapshotDate)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DailyCategorySnapshot> saveAll(List<DailyCategorySnapshot> snapshots) {
            saved.addAll(snapshots);
            return snapshots;
        }

        @Override
        public List<DailyCategorySnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive,
                LocalDate endInclusive) {
            List<DailyCategorySnapshot> out = new ArrayList<>();
            for (DailyCategorySnapshot s : saved) {
                if (!s.getShopId().equals(shopId)) {
                    continue;
                }
                if (s.getSnapshotDate().isBefore(startInclusive) || s.getSnapshotDate().isAfter(endInclusive)) {
                    continue;
                }
                out.add(s);
            }
            return out;
        }
    }

    private static final class InMemoryDailySnapshotRepo implements DailySnapshotRepository {
        private final Map<String, DailySnapshot> byShopDate = new HashMap<>();

        @Override
        public boolean existsForShopAndDate(UUID shopId, LocalDate snapshotDate) {
            return byShopDate.containsKey(shopId + ":" + snapshotDate);
        }

        @Override
        public DailySnapshot save(DailySnapshot snapshot) {
            byShopDate.put(snapshot.getShopId() + ":" + snapshot.getSnapshotDate(), snapshot);
            return snapshot;
        }

        @Override
        public Optional<DailySnapshot> findByShopAndDate(UUID shopId, LocalDate snapshotDate) {
            return Optional.ofNullable(byShopDate.get(shopId + ":" + snapshotDate));
        }

        @Override
        public List<DailySnapshot> findByShopAndDateRange(UUID shopId, LocalDate startInclusive, LocalDate endInclusive) {
            List<DailySnapshot> out = new ArrayList<>();
            for (DailySnapshot s : byShopDate.values()) {
                if (!s.getShopId().equals(shopId)) {
                    continue;
                }
                if (s.getSnapshotDate().isBefore(startInclusive) || s.getSnapshotDate().isAfter(endInclusive)) {
                    continue;
                }
                out.add(s);
            }
            return out;
        }
    }

    private static final class InMemoryConfirmedOrderRepo implements ConfirmedOrderReadRepository {
        private final List<OrderFact> orders;

        private InMemoryConfirmedOrderRepo(List<OrderFact> orders) {
            this.orders = orders;
        }

        @Override
        public List<OrderFact> findConfirmedOrdersForShopAndDate(UUID shopId, LocalDate utcDate) {
            List<OrderFact> out = new ArrayList<>();
            for (OrderFact o : orders) {
                if (o.shopId().equals(shopId)) {
                    out.add(o);
                }
            }
            return out;
        }
    }

    private static final class InMemoryOrderItemRepo implements OrderItemReadRepository {
        private final List<OrderItemFact> items;

        private InMemoryOrderItemRepo(List<OrderItemFact> items) {
            this.items = items;
        }

        @Override
        public List<OrderItemFact> findItemsForOrders(UUID shopId, List<UUID> orderIds) {
            Set<UUID> set = new HashSet<>(orderIds);
            List<OrderItemFact> out = new ArrayList<>();
            for (OrderItemFact i : items) {
                if (set.contains(i.orderId())) {
                    out.add(i);
                }
            }
            return out;
        }
    }

    private static final class InMemoryInventoryRepo implements InventoryAnalyticsReadRepository {
        private final List<StockAtMidnightFact> stocks;

        private InMemoryInventoryRepo(List<StockAtMidnightFact> stocks) {
            this.stocks = stocks;
        }

        @Override
        public List<StockAtMidnightFact> findStockAtMidnight(UUID shopId, LocalDate snapshotDate) {
            return stocks;
        }
    }

    private static final class InMemoryLockStore implements AnalyticsJobLockStore {
        private final Set<String> locks = new HashSet<>();

        @Override
        public LockToken tryAcquire(UUID shopId, LocalDate snapshotDate) {
            String key = shopId + ":" + snapshotDate;
            if (locks.contains(key)) {
                return null;
            }
            locks.add(key);
            return new LockToken(shopId, snapshotDate, UUID.randomUUID().toString());
        }

        @Override
        public void release(LockToken token) {
            locks.remove(token.shopId() + ":" + token.snapshotDate());
        }
    }

    private static final class InMemoryRunStore implements AnalyticsJobRunStore {
        private final Set<String> completed = new HashSet<>();

        @Override
        public boolean isCompleted(UUID shopId, LocalDate snapshotDate) {
            return completed.contains(shopId + ":" + snapshotDate);
        }

        @Override
        public void markCompleted(UUID shopId, LocalDate snapshotDate) {
            completed.add(shopId + ":" + snapshotDate);
        }
    }

    private static final class InMemoryLogStore implements AnalyticsJobLogStore {
        @Override
        public void log(UUID shopId, LocalDate snapshotDate, LogLevel level, String message, LocalDateTime atUtc) {
            // no-op for tests
        }
    }
}
