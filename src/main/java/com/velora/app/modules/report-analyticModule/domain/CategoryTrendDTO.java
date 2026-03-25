package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

public record CategoryTrendDTO(UUID categoryId,BigDecimal grossRevenue,BigDecimal netProfit,int itemsSold){public CategoryTrendDTO{ValidationUtils.validateUUID(categoryId,"categoryId");AnalyticsMoney.normalizeNonNegative(grossRevenue,"grossRevenue");AnalyticsMoney.normalizeSigned(netProfit,"netProfit");if(itemsSold<0){throw new IllegalArgumentException("itemsSold must be >= 0");}}}
