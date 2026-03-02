package com.velora.app.core.domain.salemanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Permanent order record.
 */
public class Order {

    private UUID orderId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private UUID shopId;
    private UUID customerId;
    private LocalDateTime createdAt;
    private final List<OrderItem> items;

    /**
     * Creates an order with mandatory fields.
     * <p>
     * orderId is supplied by the caller and is immutable.
     */
    public Order(UUID orderId, UUID shopId, UUID customerId, List<OrderItem> items) {
        setOrderId(orderId);
        setShopId(shopId);
        setCustomerId(customerId);
        setCreatedAt(LocalDateTime.now());
        setStatus(OrderStatus.PENDING);
        this.items = freezeItems(orderId, items);
        setTotalPrice(sumItems(this.items));
        verifyTotal();
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Marks this order paid.
     */
    public void markPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Illegal order transition from " + status);
        }
        verifyTotal();
        setStatus(OrderStatus.PAID);
    }

    /**
     * Cancels this order.
     */
    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        if (status == OrderStatus.PENDING || status == OrderStatus.PAID) {
            setStatus(OrderStatus.CANCELLED);
            return;
        }
        throw new IllegalStateException("Illegal order transition from " + status);
    }

    /**
     * Verifies that totalPrice equals the sum of item subtotals.
     */
    public void verifyTotal() {
        BigDecimal expected = sumItems(items);
        if (expected.compareTo(totalPrice) != 0) {
            throw new IllegalStateException("totalPrice mismatch: expected " + expected + " but was " + totalPrice);
        }
    }

    /**
     * Returns true if order is finalized (PAID or CANCELLED).
     */
    public boolean isFinalized() {
        return status == OrderStatus.PAID || status == OrderStatus.CANCELLED;
    }

    private static List<OrderItem> freezeItems(UUID orderId, List<OrderItem> items) {
        ValidationUtils.validateUUID(orderId, "orderId");
        ValidationUtils.validateNotBlank(items, "items");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items cannot be empty");
        }
        List<OrderItem> copy = new ArrayList<>(items.size());
        for (OrderItem item : items) {
            ValidationUtils.validateNotBlank(item, "orderItem");
            if (!orderId.equals(item.getOrderId())) {
                throw new IllegalArgumentException("orderItem.orderId must match orderId");
            }
            copy.add(item);
        }
        return Collections.unmodifiableList(copy);
    }

    private static BigDecimal sumItems(List<OrderItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : items) {
            sum = sum.add(ValidationUtils.normalizeMoney(item.getSubtotal(), "subtotal"));
        }
        return ValidationUtils.normalizeMoney(sum, "totalPrice");
    }

    private void setOrderId(UUID orderId) {
        ValidationUtils.validateUUID(orderId, "orderId");
        this.orderId = orderId;
    }

    private void setStatus(OrderStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = ValidationUtils.normalizeMoney(totalPrice, "totalPrice");
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setCustomerId(UUID customerId) {
        ValidationUtils.validateUUID(customerId, "customerId");
        this.customerId = customerId;
    }

    private void setCreatedAt(LocalDateTime createdAt) {
        ValidationUtils.validateNotBlank(createdAt, "createdAt");
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", shopId=" + shopId +
                ", customerId=" + customerId +
                ", createdAt=" + createdAt +
                '}';
    }
}
