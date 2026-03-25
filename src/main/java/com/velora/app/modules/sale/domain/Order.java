package com.velora.app.modules.sale.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a permanent sale order in the Velora platform.
 * 
 * <p>
 * Order is the aggregate root for the sale management domain.
 * It manages order lifecycle, item tracking, and total verification.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>PENDING → PAID (payment confirmed)</li>
 * <li>PENDING → CANCELLED (timeout or user cancellation)</li>
 * <li>PAID → CANCELLED (admin refund)</li>
 * </ul>
 * 
 * <p>
 * Business rules:
 * <ul>
 * <li>totalPrice must equal sum of item subtotals</li>
 * <li>Once PAID, order cannot be reverted</li>
 * <li>Items are immutable once created</li>
 * </ul>
 */
public class Order extends AbstractAuditableEntity {

    /**
     * Order status lifecycle states
     */
    public enum Status {
        PENDING, // Awaiting payment
        PAID, // Payment confirmed
        CANCELLED // Order cancelled (terminal for PENDING, allows refund for PAID)
    }

    private final UUID shopId;
    private final UUID customerId;
    private Status status;
    private BigDecimal totalPrice;
    private final List<OrderItem> items;
    private Receipt receipt;
    private Delivery delivery;
    private LocalDateTime paidAt;

    /**
     * Creates a new Order in PENDING status.
     *
     * @param orderId    The unique order identifier
     * @param shopId     The shop's identifier
     * @param customerId The customer's identifier
     * @param items      The order items (immutable after creation)
     */
    public Order(UUID orderId, UUID shopId, UUID customerId, List<OrderItem> items) {
        super(orderId);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(customerId, "customerId");
        ValidationUtils.validateNotBlank(items, "items");

        this.shopId = shopId;
        this.customerId = customerId;
        this.status = Status.PENDING;
        this.items = new ArrayList<>(items);
        this.totalPrice = calculateTotalFromItems();
        this.receipt = null;
        this.delivery = null;
        this.paidAt = null;

        verifyTotal();
    }

    /**
     * Gets the shop identifier.
     */
    public UUID getShopId() {
        return shopId;
    }

    /**
     * Gets the customer identifier.
     */
    public UUID getCustomerId() {
        return customerId;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the total price.
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Gets an unmodifiable list of order items.
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the receipt if exists.
     */
    public Receipt getReceipt() {
        return receipt;
    }

    /**
     * Gets the delivery if exists.
     */
    public Delivery getDelivery() {
        return delivery;
    }

    /**
     * Gets the payment timestamp.
     */
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    /**
     * Checks if the order is pending.
     */
    public boolean isPending() {
        return status == Status.PENDING;
    }

    /**
     * Checks if the order is paid.
     */
    public boolean isPaid() {
        return status == Status.PAID;
    }

    /**
     * Checks if the order is cancelled.
     */
    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    /**
     * Checks if the order is finalized (paid and cannot be reverted).
     */
    public boolean isFinalized() {
        return status == Status.PAID;
    }

    /**
     * Marks the order as paid. Sets paidAt timestamp.
     *
     * @throws IllegalStateException if order is not PENDING
     */
    public void markPaid() {
        if (status != Status.PENDING) {
            throw new IllegalStateException(
                    "Order can only be marked paid from PENDING status. Current: " + status);
        }
        this.status = Status.PAID;
        this.paidAt = LocalDateTime.now();
        touch();
    }

    /**
     * Cancels the order. Allowed from PENDING or PAID status.
     *
     * @throws IllegalStateException if order is already CANCELLED
     */
    public void cancel() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = Status.CANCELLED;
        touch();
    }

    /**
     * Attaches a receipt to the order.
     *
     * @param receipt The receipt to attach
     * @throws IllegalStateException if order is not PAID
     */
    public void attachReceipt(Receipt receipt) {
        if (status != Status.PAID) {
            throw new IllegalStateException("Receipt can only be attached to PAID orders");
        }
        this.receipt = receipt;
        touch();
    }

    /**
     * Sets delivery information for the order.
     *
     * @param delivery The delivery to attach
     * @throws IllegalStateException if order is not PAID
     */
    public void setDelivery(Delivery delivery) {
        if (status != Status.PAID) {
            throw new IllegalStateException("Delivery can only be set for PAID orders");
        }
        this.delivery = delivery;
        touch();
    }

    /**
     * Verifies that totalPrice equals the sum of item subtotals.
     *
     * @throws IllegalStateException if totals don't match
     */
    public void verifyTotal() {
        BigDecimal calculatedTotal = calculateTotalFromItems();
        if (totalPrice.compareTo(calculatedTotal) != 0) {
            throw new IllegalStateException(
                    "Order total mismatch. Expected: " + calculatedTotal + ", Found: " + totalPrice);
        }
    }

    /**
     * Calculates total from items.
     */
    private BigDecimal calculateTotalFromItems() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + getId() +
                ", shopId=" + shopId +
                ", customerId=" + customerId +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", itemCount=" + items.size() +
                '}';
    }
}
