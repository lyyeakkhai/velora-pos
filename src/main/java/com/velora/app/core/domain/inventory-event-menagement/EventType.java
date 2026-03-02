package com.velora.app.core.domain.inventoryeventmanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Discount rule / promotional event definition.
 */
public class EventType {

    private UUID eventId;
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
        setEventId(UUID.randomUUID());
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

    public UUID getEventId() {
        return eventId;
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

    private void setEventId(UUID eventId) {
        ValidationUtils.validateUUID(eventId, "eventId");
        this.eventId = eventId;
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        this.name = name.toString().trim();
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventType)) {
            return false;
        }
        EventType eventType = (EventType) o;
        return Objects.equals(eventId, eventType.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "EventType{" +
                "eventId=" + eventId +
                ", name='" + name + '\'' +
                ", discountType=" + discountType +
                ", available=" + available +
                '}';
    }
}
