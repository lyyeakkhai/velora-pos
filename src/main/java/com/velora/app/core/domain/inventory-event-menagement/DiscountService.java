package com.velora.app.core.domain.inventoryeventmanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Discount and promotion service.
 */
public class DiscountService {

    private final EventTypeStore eventTypeStore;
    private final EventProductStore eventProductStore;

    public DiscountService(EventTypeStore eventTypeStore, EventProductStore eventProductStore) {
        this.eventTypeStore = require(eventTypeStore, "eventTypeStore");
        this.eventProductStore = require(eventProductStore, "eventProductStore");
    }

    public EventType createEvent(Role.RoleName actorRole, UUID shopId, String name, BigDecimal discountValue,
            DiscountType discountType, boolean available, LocalDateTime startDate, LocalDateTime endDate,
            BigDecimal minAmount, Integer usageLimit) {
        RolePolicy.requireOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        EventType event = new EventType(shopId, name, discountValue, discountType, available, startDate, endDate,
                minAmount, usageLimit);
        return eventTypeStore.save(event);
    }

    public EventProduct attachProductToEvent(Role.RoleName actorRole, EventType event, Product product, int sortOrder,
            EventProductStatus initialStatus) {
        RolePolicy.requireCatalogWrite(actorRole);
        ValidationUtils.validateNotBlank(event, "event");
        ValidationUtils.validateNotBlank(product, "product");
        if (!event.getShopId().equals(product.getShopId())) {
            throw new IllegalStateException("Cross-shop attachment rejected");
        }
        if (eventProductStore.existsByEventIdAndProductId(event.getId(), product.getId())) {
            throw new IllegalStateException("Product already attached to event");
        }
        EventProduct ep = new EventProduct(product.getShopId(), product.getId(), product.getCategoryId(),
                event.getId(), sortOrder, initialStatus);
        return eventProductStore.save(ep);
    }

    /**
     * Validates discount usage, availability, date range, minimum spend, and
     * profit.
     */
    public void validateDiscount(Role.RoleName actorRole, EventType event, BigDecimal salePrice, BigDecimal costPrice,
            int currentUsageCount) {
        RolePolicy.requireOwner(actorRole);
        ValidationUtils.validateNotBlank(event, "event");
        BigDecimal sale = ValidationUtils.normalizeMoney(salePrice, "salePrice");
        BigDecimal cost = ValidationUtils.normalizeMoney(costPrice, "costPrice");
        ValidationUtils.validateNonNegativeInteger(currentUsageCount, "currentUsageCount");

        if (!event.isAvailable()) {
            throw new IllegalStateException("Event is not available");
        }
        if (!event.isInDateRange(LocalDateTime.now())) {
            throw new IllegalStateException("Event is outside active date range");
        }
        if (sale.compareTo(event.getMinAmount()) < 0) {
            throw new IllegalStateException("Minimum spend not met");
        }
        if (currentUsageCount >= event.getUsageLimit()) {
            throw new IllegalStateException("Usage limit exceeded");
        }

        BigDecimal finalPrice = calculateFinalPrice(sale, event);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Final price cannot be negative");
        }
        if (finalPrice.compareTo(cost) <= 0) {
            throw new IllegalStateException("Discount would violate profit protection");
        }
    }

    /**
     * Calculates final price after applying the event discount.
     */
    public BigDecimal calculateFinalPrice(BigDecimal salePrice, EventType event) {
        ValidationUtils.validateNotBlank(event, "event");
        BigDecimal sale = ValidationUtils.normalizeMoney(salePrice, "salePrice");
        BigDecimal discount = event.calculateDiscountAmount(sale);
        BigDecimal finalPrice = sale.subtract(discount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Final price cannot be negative");
        }
        return ValidationUtils.normalizeMoney(finalPrice, "finalPrice");
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}
