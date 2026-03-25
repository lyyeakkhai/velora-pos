package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.velora.app.common.AbstractSnapshotAggregator;

/**
 * Aggregates order items and inventory data into {@link DailyProductSnapshot} records.
 *
 * <p>Extends {@link AbstractSnapshotAggregator} for the template-method run/idempotency
 * guarantee and implements {@link SnapshotAggregator} for the named-aggregator contract.
 *
 * Requirements: 20.2
 */
public class ProductSnapshotAggregator
        extends AbstractSnapshotAggregator<List<DailyProductSnapshot>>
        implements SnapshotAggregator<List<DailyProductSnapshot>> {

    private final ConfirmedOrderReadRepository orderReadRepository;
    private final OrderItemReadRepository orderItemReadRepository;
    private final InventoryAnalyticsReadRepository inventoryReadRepository;
    private final DailyProductSnapshotRepository productSnapshotRepository;
    private final boolean allowVerifiedLossProfitNegative;

    public ProductSnapshotAggregator(
            ConfirmedOrderReadRepository orderReadRepository,
            OrderItemReadRepository orderItemReadRepository,
            InventoryAnalyticsReadRepository inventoryReadRepository,
            DailyProductSnapshotRepository productSnapshotRepository,
            boolean allowVerifiedLossProfitNegative) {
        this.orderReadRepository = orderReadRepository;
        this.orderItemReadRepository = orderItemReadRepository;
        this.inventoryReadRepository = inventoryReadRepository;
        this.productSnapshotRepository = productSnapshotRepository;
        this.allowVerifiedLossProfitNegative = allowVerifiedLossProfitNegative;
    }

    @Override
    public String getAggregatorName() {
        return "ProductSnapshotAggregator";
    }

    @Override
    public boolean alreadyExists(UUID shopId, LocalDate date) {
        return productSnapshotRepository.existsForShopAndDate(shopId, date);
    }

    @Override
    public List<DailyProductSnapshot> aggregate(UUID shopId, LocalDate date) {
        List<OrderFact> orders = orderReadRepository.findConfirmedOrdersForShopAndDate(shopId, date);
        Set<UUID> orderIds = new HashSet<>();
        for (OrderFact o : orders) {
            orderIds.add(o.orderId());
        }

        List<OrderItemFact> items = orderIds.isEmpty()
                ? List.of()
                : orderItemReadRepository.findItemsForOrders(shopId, new ArrayList<>(orderIds));

        Map<UUID, Integer> stockByVariant = new HashMap<>();
        for (StockAtMidnightFact stock : inventoryReadRepository.findStockAtMidnight(shopId, date)) {
            stockByVariant.put(stock.variantId(), stock.stockQuantity());
        }

        Map<ProductKey, ProductAccumulator> productAcc = new HashMap<>();
        for (OrderItemFact item : items) {
            ProductKey key = new ProductKey(item.productId(), item.variantId(), item.sellerId(), item.categoryId());
            ProductAccumulator acc = productAcc.computeIfAbsent(key, k -> new ProductAccumulator());
            acc.qtySold += item.quantity();
            acc.soldPriceSum = acc.soldPriceSum.add(AnalyticsMoney.normalizeNonNegative(item.soldPrice(), "soldPrice"));
            acc.costPriceSum = acc.costPriceSum.add(AnalyticsMoney.normalizeNonNegative(item.baseCostPrice(), "baseCostPrice"));
            acc.lines += 1;
            acc.verifiedLoss = acc.verifiedLoss || item.verifiedLoss();
        }

        List<DailyProductSnapshot> snapshots = new ArrayList<>();
        for (Map.Entry<ProductKey, ProductAccumulator> entry : productAcc.entrySet()) {
            ProductKey key = entry.getKey();
            ProductAccumulator acc = entry.getValue();

            BigDecimal unitSalePrice = acc.lines == 0 ? BigDecimal.ZERO
                    : acc.soldPriceSum.divide(new BigDecimal(acc.lines), 2, RoundingMode.HALF_UP);
            BigDecimal baseCostPrice = acc.lines == 0 ? BigDecimal.ZERO
                    : acc.costPriceSum.divide(new BigDecimal(acc.lines), 2, RoundingMode.HALF_UP);

            BigDecimal gross = unitSalePrice.multiply(new BigDecimal(acc.qtySold)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal profit = unitSalePrice.subtract(baseCostPrice).multiply(new BigDecimal(acc.qtySold))
                    .setScale(2, RoundingMode.HALF_UP);

            if (gross.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Negative revenue prohibited");
            }
            if (profit.compareTo(BigDecimal.ZERO) < 0 && !(allowVerifiedLossProfitNegative || acc.verifiedLoss)) {
                throw new IllegalStateException("Negative profit requires verified loss");
            }

            int stockAtMidnight = stockByVariant.getOrDefault(key.variantId, 0);
            snapshots.add(new DailyProductSnapshot(UUID.randomUUID(), date, shopId,
                    key.productId, key.variantId, key.sellerId, key.categoryId,
                    acc.qtySold, baseCostPrice, unitSalePrice, stockAtMidnight));
        }

        return snapshots;
    }

    @Override
    public void persist(List<DailyProductSnapshot> snapshots) {
        productSnapshotRepository.saveAll(snapshots);
    }

    // ---- inner helpers ----

    private static final class ProductKey {
        final UUID productId;
        final UUID variantId;
        final UUID sellerId;
        final UUID categoryId;

        ProductKey(UUID productId, UUID variantId, UUID sellerId, UUID categoryId) {
            this.productId = productId;
            this.variantId = variantId;
            this.sellerId = sellerId;
            this.categoryId = categoryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductKey)) return false;
            ProductKey that = (ProductKey) o;
            return productId.equals(that.productId) && variantId.equals(that.variantId)
                    && sellerId.equals(that.sellerId) && categoryId.equals(that.categoryId);
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
        int qtySold;
        BigDecimal soldPriceSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal costPriceSum = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int lines;
        boolean verifiedLoss;
    }
}

