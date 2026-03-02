package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Seller performance analytics derived from daily product snapshots only.
 */
public class SellerAnalyticsService {

    private final DailyProductSnapshotRepository productSnapshotRepository;

    public SellerAnalyticsService(DailyProductSnapshotRepository productSnapshotRepository) {
        this.productSnapshotRepository = require(productSnapshotRepository, "productSnapshotRepository");
    }

    public List<SellerRankDTO> rankSellers(Role.RoleName actorRole, UUID shopId, LocalDate startInclusive,
            LocalDate endInclusive) {
        AnalyticsAccessPolicy.requireManagerOrOwner(actorRole);
        DateRange range = new DateRange(startInclusive, endInclusive);
        ValidationUtils.validateUUID(shopId, "shopId");

        List<DailyProductSnapshot> snaps = productSnapshotRepository.findByShopAndDateRange(shopId, range.startInclusive(),
                range.endInclusive());
        Map<UUID, Acc> bySeller = new HashMap<>();
        for (DailyProductSnapshot s : snaps) {
            Acc acc = bySeller.computeIfAbsent(s.getSellerId(), id -> new Acc());
            BigDecimal gross = s.getUnitSalePrice().multiply(new BigDecimal(s.getQtySold())).setScale(2,
                    RoundingMode.HALF_UP);
            BigDecimal profit = s.getUnitSalePrice().subtract(s.getBaseCostPrice()).multiply(new BigDecimal(s.getQtySold()))
                    .setScale(2, RoundingMode.HALF_UP);
            acc.gross = acc.gross.add(gross);
            acc.profit = acc.profit.add(profit);
            acc.itemsSold += s.getQtySold();
        }

        List<SellerRankDTO> ranks = new ArrayList<>();
        for (Map.Entry<UUID, Acc> e : bySeller.entrySet()) {
            ranks.add(new SellerRankDTO(e.getKey(), e.getValue().gross, e.getValue().profit, e.getValue().itemsSold));
        }
        ranks.sort(Comparator.comparing(SellerRankDTO::grossRevenue).reversed());
        return ranks;
    }

    public SellerPerformanceDTO getSellerPerformance(Role.RoleName actorRole, UUID actorSellerId, UUID requestedSellerId,
            UUID shopId, LocalDate startInclusive, LocalDate endInclusive) {
        AnalyticsAccessPolicy.requireSellerSelfOrElevated(actorRole, actorSellerId, requestedSellerId);
        DateRange range = new DateRange(startInclusive, endInclusive);
        ValidationUtils.validateUUID(shopId, "shopId");

        List<DailyProductSnapshot> snaps = productSnapshotRepository.findByShopAndDateRange(shopId, range.startInclusive(),
                range.endInclusive());
        BigDecimal gross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal profit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int itemsSold = 0;

        for (DailyProductSnapshot s : snaps) {
            if (!s.getSellerId().equals(requestedSellerId)) {
                continue;
            }
            gross = gross.add(s.getUnitSalePrice().multiply(new BigDecimal(s.getQtySold())).setScale(2, RoundingMode.HALF_UP));
            profit = profit.add(s.getUnitSalePrice().subtract(s.getBaseCostPrice()).multiply(new BigDecimal(s.getQtySold()))
                    .setScale(2, RoundingMode.HALF_UP));
            itemsSold += s.getQtySold();
        }
        return new SellerPerformanceDTO(requestedSellerId, range, gross, profit, itemsSold);
    }

    private static final class Acc {
        private BigDecimal gross = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal profit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private int itemsSold;
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}
