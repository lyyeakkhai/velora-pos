package com.velora.app.modules.event_managementModule.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.modules.inventory.domain.DiscountType;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Discount rule / promotional event definition.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class EventType extends AbstractAuditableEntity {

    private String name;
    private BigDecimal discountValue;
    private DiscountType discountType;
    private boolean available;
    private UUID shopId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minAmount;
    private Integer usageLimit;

    public EventType(UUID shopId, String name, BigDecimal discountValue, DiscountType discountType, boolean available,
            LocalDateTime startDate, LocalDateTime endDate, BigDecimal minAmount, Integer usageLimit) {
        super(UUID.randomUUID());
        setShopId(shopId);
        setName(name);
        setDiscountType(discountType);
        setDiscountValue(discountValue);
        setAvailable(available);
        setStartDate(startDate);
        setEndDate(endDate);
        ValidationUtils.validateStartBeforeEnd(this.startDate, this.endDate, "startDate", "endDate");
        setMinAmount(minAmount);
        setUsageLimit(usageLimit);
        validateDiscountBounds();
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public boolean isAvailable() {
        return available;
    }

    public UUID getShopId() {
        return shopId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public boolean isInDateRange(LocalDateTime now) {
        ValidationUtils.validateNotBlank(now, "now");
        return !now.isBefore(startDate) && now.isBefore(endDate);
    }

    public void setAvailable(boolean available) {
        this.available = available;
        touch();
    }

    public BigDecimal calculateDiscountAmount(BigDecimal salePrice) {
        BigDecimal price = ValidationUtils.normalizeMoney(salePrice, "salePrice");
        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal pct = ValidationUtils.normalizePercentage(discountValue, "discountValue");
            BigDecimal amount = price.multiply(pct).divide(new BigDecimal("100"));
            return ValidationUtils.normalizeMoney(amount, "discountAmount");
        }
        // FIXED
        return ValidationUtils.normalizeMoney(discountValue, "discountValue");
    }

    private void validateDiscountBounds() {
        if (discountType == DiscountType.PERCENTAGE) {
            ValidationUtils.normalizePercentage(discountValue, "discountValue");
        }
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        this.name = name.trim();
    }

    private void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = ValidationUtils.normalizeMoney(discountValue, "discountValue");
    }

    private void setDiscountType(DiscountType discountType) {
        ValidationUtils.validateNotBlank(discountType, "discountType");
        this.discountType = discountType;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setStartDate(LocalDateTime startDate) {
        ValidationUtils.validateNotBlank(startDate, "startDate");
        this.startDate = startDate;
    }

    private void setEndDate(LocalDateTime endDate) {
        ValidationUtils.validateNotBlank(endDate, "endDate");
        this.endDate = endDate;
    }

    private void setMinAmount(BigDecimal minAmount) {
        this.minAmount = ValidationUtils.normalizeMoney(minAmount, "minAmount");
    }

    private void setUsageLimit(Integer usageLimit) {
        ValidationUtils.validateNonNegativeInteger(usageLimit, "usageLimit");
        this.usageLimit = usageLimit;
    }

    @Override
    public String toString() {
        return "EventType{id=" + getId() +
                ", name='" + name + '\'' +
                ", discountType=" + discountType +
                ", available=" + available +
                '}';
    }
}
