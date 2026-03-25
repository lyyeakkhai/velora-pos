package com.velora.app.modules.inventory.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.velora.app.common.DomainException;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.event_managementModule.domain.EventProduct;
import com.velora.app.modules.event_managementModule.domain.EventProductStatus;
import com.velora.app.modules.event_managementModule.domain.EventProductStore;
import com.velora.app.modules.event_managementModule.domain.EventType;
import com.velora.app.modules.event_managementModule.domain.EventTypeStore;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Discount and promotion service.
 * Uses the {@link DiscountStrategy} pattern to delegate calculation to the
 * appropriate strategy based on {@link DiscountType}.
 */
public class DiscountService {

    private final EventTypeStore eventTypeStore;
    private final EventProductStore eventProductStore;
    private final RolePolicy policy;
    private final Map<DiscountType, DiscountStrategy> strategies;

    public DiscountService(EventTypeStore eventTypeStore, EventProductStore eventProductStore) {
        this.eventTypeStore = require(eventTypeStore, "eventTypeStore");
        this.eventProductStore = require(eventProductStore, "eventProductStore");
        this.policy = new RolePolicy();
        this.strategies = new EnumMap<>(DiscountType.class);
        this.strategies.put(DiscountType.PERCENTAGE, new PercentageDiscountStrategy());
        this.strategies.put(DiscountType.FIXED, new FixedDiscountStrategy());
    }

    public EventType createEvent(Role.RoleName actorRole, UUID shopId, String name, BigDecimal discountValue,
            DiscountType discountType, boolean available, LocalDateTime startDate, LocalDateTime endDate,
            BigDecimal minAmount, Integer usageLimit) {
        policy.requireOwner(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        EventType event = new EventType(shopId, name, discountValue, discountType, available, startDate, endDate,
                minAmount, usageLimit);
        return eventTypeStore.save(event);
    }

    public EventProduct attachProductToEvent(Role.RoleName actorRole, EventType event, Product product, int sortOrder,
            EventProductStatus initialStatus) {
        policy.requireCatalogWrite(actorRole);
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
        policy.requireOwner(actorRole);
        ValidationUtils.validateNotBlank(event, "event");
        BigDecimal sale = ValidationUtils.normalizeMoney(salePrice, "salePrice");
        BigDecimal cost = ValidationUtils.normalizeMoney(costPrice, "costPrice");
        ValidationUtils.validateNonNegativeInteger(currentUsageCount, "currentUsageCount");

        if (!event.isAvailable()) {
            throw new DomainException("Event is not available");
        }
        if (!event.isInDateRange(LocalDateTime.now())) {
            throw new DomainException("Event is outside active date range");
        }
        if (sale.compareTo(event.getMinAmount()) < 0) {
            throw new DomainException("Minimum spend not met");
        }
        if (currentUsageCount >= event.getUsageLimit()) {
            throw new DomainException("Usage limit exceeded");
        }

        BigDecimal finalPrice = calculateFinalPrice(sale, event);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Final price cannot be negative");
        }
        if (finalPrice.compareTo(cost) <= 0) {
            throw new DomainException("Discount would violate profit protection");
        }
    }

    /**
     * Calculates final price after applying the event discount.
     * Looks up the {@link DiscountStrategy} by {@link DiscountType}, calls
     * {@code validate()} then {@code apply()}, and delegates profit margin
     * protection to
     * {@link com.velora.app.common.AbstractDiscountCalculator#validateProfitMargin}.
     */
    public BigDecimal calculateFinalPrice(BigDecimal salePrice, EventType event) {
        ValidationUtils.validateNotBlank(event, "event");
        BigDecimal sale = ValidationUtils.normalizeMoney(salePrice, "salePrice");

        DiscountStrategy strategy = strategies.get(event.getDiscountType());
        if (strategy == null) {
            throw new DomainException("No discount strategy registered for type: " + event.getDiscountType());
        }

        BigDecimal discountValue = event.getDiscountValue();

        // For fixed discounts, also validate against the base price
        if (strategy instanceof FixedDiscountStrategy fixedStrategy) {
            fixedStrategy.validateAgainstBasePrice(discountValue, sale);
        } else {
            strategy.validate(discountValue);
        }

        BigDecimal finalPrice = strategy.apply(sale, discountValue);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Final price cannot be negative");
        }
        return ValidationUtils.normalizeMoney(finalPrice, "finalPrice");
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}
