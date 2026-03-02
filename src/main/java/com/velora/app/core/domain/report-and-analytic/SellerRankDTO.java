package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

public record SellerRankDTO(UUID sellerId,BigDecimal grossRevenue,BigDecimal netProfit,int itemsSold){public SellerRankDTO{ValidationUtils.validateUUID(sellerId,"sellerId");AnalyticsMoney.normalizeNonNegative(grossRevenue,"grossRevenue");AnalyticsMoney.normalizeSigned(netProfit,"netProfit");if(itemsSold<0){throw new IllegalArgumentException("itemsSold must be >= 0");}}}
