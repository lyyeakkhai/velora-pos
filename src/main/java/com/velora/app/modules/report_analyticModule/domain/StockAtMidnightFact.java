package com.velora.app.modules.report_analyticModule.domain;

import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Read model of inventory stock at midnight used by aggregation.
 */
public record StockAtMidnightFact(UUID variantId,int stockQuantity){public StockAtMidnightFact{ValidationUtils.validateUUID(variantId,"variantId");if(stockQuantity<0){throw new IllegalArgumentException("stockQuantity must be >= 0");}}}

