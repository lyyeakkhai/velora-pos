package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

public record OutOfStockPredictionDTO(UUID variantId,int stockAtMidnight,BigDecimal avgDailyQtySold,BigDecimal estimatedDaysUntilOutOfStock,RiskLevel riskLevel,String recommendation){public OutOfStockPredictionDTO{ValidationUtils.validateUUID(variantId,"variantId");if(stockAtMidnight<0){throw new IllegalArgumentException("stockAtMidnight must be >= 0");}AnalyticsMoney.normalizeNonNegative(avgDailyQtySold,"avgDailyQtySold");AnalyticsMoney.normalizeNonNegative(estimatedDaysUntilOutOfStock,"estimatedDaysUntilOutOfStock");ValidationUtils.validateNotBlank(riskLevel,"riskLevel");ValidationUtils.validateNotBlank(recommendation,"recommendation");}}

