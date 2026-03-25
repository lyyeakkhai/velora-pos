package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

public record SellerPerformanceDTO(UUID sellerId,DateRange range,BigDecimal grossRevenue,BigDecimal netProfit,int itemsSold){public SellerPerformanceDTO{ValidationUtils.validateUUID(sellerId,"sellerId");ValidationUtils.validateNotBlank(range,"range");AnalyticsMoney.normalizeNonNegative(grossRevenue,"grossRevenue");AnalyticsMoney.normalizeSigned(netProfit,"netProfit");if(itemsSold<0){throw new IllegalArgumentException("itemsSold must be >= 0");}}}
