package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Read model of an order item used for aggregation.
 */
public record OrderItemFact(UUID orderId,UUID productId,UUID variantId,UUID sellerId,UUID categoryId,int quantity,BigDecimal soldPrice,BigDecimal baseCostPrice,boolean verifiedLoss){public OrderItemFact{ValidationUtils.validateUUID(orderId,"orderId");ValidationUtils.validateUUID(productId,"productId");ValidationUtils.validateUUID(variantId,"variantId");ValidationUtils.validateUUID(sellerId,"sellerId");ValidationUtils.validateUUID(categoryId,"categoryId");if(quantity<0){throw new IllegalArgumentException("quantity must be >= 0");}ValidationUtils.normalizeMoney(soldPrice,"soldPrice");ValidationUtils.normalizeMoney(baseCostPrice,"baseCostPrice");}}
