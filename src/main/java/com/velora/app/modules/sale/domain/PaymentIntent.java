package com.velora.app.modules.sale.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a temporary payment intent for checkout.
 * 
 * <p>
 * PaymentIntent is a temporary record that tracks the pre-payment state.
 * It is deleted after order finalization or expiration.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>CREATED → CONFIRMED (payment received)</li>
 * <li>CREATED → EXPIRED (timeout)</li>
 * </ul>
 */
public class PaymentIntent extends AbstractAuditableEntity {

    /**
     * Payment intent status lifecycle states
     */
    public enum Status {
        CREATED, // Awaiting payment
        CONFIRMED, // Payment received
        EXPIRED // Timeout reached
    }

    private final UUID shopId;
    private final UUID customerId;
    private final String bankRefId;
    private final BigDecimal totalAmount;
    private final String cartSnapshot; // JSON representation of cart items
    private Status status;
    private LocalDateTime expiresAt;
    private UUID linkedOrderId;

    /**
     * Creates a new PaymentIntent in CREATED status.
     *
     * @param intentId     The unique intent identifier
     * @param shopId       The shop identifier
     * @param customerId   The customer identifier
     * @param bankRefId    Unique bank reference ID (for idempotency)
     * @param totalAmount  The total payment amount
     * @param cartSnapshot JSON snapshot of cart items
     * @param expiresAt    When this intent expires
     */
    public PaymentIntent(UUID intentId, UUID shopId, UUID customerId,
            String bankRefId, BigDecimal totalAmount,
            String cartSnapshot, LocalDateTime expiresAt) {
        super(intentId);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(customerId, "customerId");
        ValidationUtils.validateNotBlank(bankRefId, "bankRefId");
        ValidationUtils.validateNotBlank(totalAmount, "totalAmount");
        ValidationUtils.validateNotBlank(cartSnapshot, "cartSnapshot");
        ValidationUtils.validateNotBlank(expiresAt, "expiresAt");

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("totalAmount must be >= 0");
        }

        this.shopId = shopId;
        this.customerId = customerId;
        this.bankRefId = bankRefId;
        this.totalAmount = ValidationUtils.normalizeMoney(totalAmount, "totalAmount");
        this.cartSnapshot = cartSnapshot;
        this.status = Status.CREATED;
        this.expiresAt = expiresAt;
        this.linkedOrderId = null;
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
     * Gets the bank reference ID.
     */
    public String getBankRefId() {
        return bankRefId;
    }

    /**
     * Gets the total amount.
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Gets the cart snapshot (JSON).
     */
    public String getCartSnapshot() {
        return cartSnapshot;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the expiration timestamp.
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the linked order ID after confirmation.
     */
    public UUID getLinkedOrderId() {
        return linkedOrderId;
    }

    /**
     * Checks if the intent is still valid (not expired or confirmed).
     */
    public boolean isValid() {
        return status == Status.CREATED && !isExpired();
    }

    /**
     * Checks if the intent has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the intent is confirmed.
     */
    public boolean isConfirmed() {
        return status == Status.CONFIRMED;
    }

    /**
     * Confirms payment and links to an order.
     *
     * @param orderId The created order identifier
     * @throws IllegalStateException if not in CREATED status
     */
    public void confirm(UUID orderId) {
        if (status != Status.CREATED) {
            throw new IllegalStateException(
                    "Payment intent can only be confirmed from CREATED status. Current: " + status);
        }
        ValidationUtils.validateUUID(orderId, "orderId");

        this.status = Status.CONFIRMED;
        this.linkedOrderId = orderId;
        touch();
    }

    /**
     * Marks the intent as expired.
     *
     * @throws IllegalStateException if already confirmed
     */
    public void expire() {
        if (status == Status.CONFIRMED) {
            throw new IllegalStateException("Cannot expire a confirmed payment intent");
        }
        this.status = Status.EXPIRED;
        touch();
    }

    /**
     * Validates and expires if necessary.
     * Should be called periodically by a scheduled job.
     *
     * @return true if expired, false otherwise
     */
    public boolean checkAndExpire() {
        if (status == Status.CREATED && isExpired()) {
            expire();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "PaymentIntent{" +
                "id=" + getId() +
                ", shopId=" + shopId +
                ", bankRefId='" + bankRefId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
