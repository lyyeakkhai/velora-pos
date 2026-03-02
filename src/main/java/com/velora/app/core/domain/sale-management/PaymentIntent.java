package com.velora.app.core.domain.salemanagement;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Temporary payment intent that contains an immutable cart snapshot.
 */
public class PaymentIntent {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    public record CartItemSnapshot(UUID productId, int quantity, BigDecimal soldPrice) {
        public CartItemSnapshot {
            ValidationUtils.validateUUID(productId, "productId");
            ValidationUtils.validatePositiveInteger(quantity, "quantity");
            soldPrice = ValidationUtils.normalizeMoney(soldPrice, "soldPrice");
        }
    }

    private UUID intentId;
    private String bankRefId;
    private UUID shopId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private String cartSnapshot;
    private PaymentIntentStatus status;
    private LocalDateTime createdAt;
    private final List<CartItemSnapshot> snapshotItems;

    /**
     * Creates a new payment intent (CREATED) with an immutable snapshot.
     */
    public PaymentIntent(String bankRefId, UUID shopId, UUID customerId, List<CartItemSnapshot> snapshotItems,
            String cartSnapshot) {
        setIntentId(UUID.randomUUID());
        setBankRefId(bankRefId);
        setShopId(shopId);
        setCustomerId(customerId);
        this.snapshotItems = freezeSnapshot(snapshotItems);
        setCartSnapshot(cartSnapshot);
        setCreatedAt(LocalDateTime.now());
        setStatus(PaymentIntentStatus.CREATED);
        setTotalAmount(sumSnapshot(this.snapshotItems));
    }

    public UUID getIntentId() {
        return intentId;
    }

    public String getBankRefId() {
        return bankRefId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCartSnapshot() {
        return cartSnapshot;
    }

    public PaymentIntentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<CartItemSnapshot> getSnapshotItems() {
        return snapshotItems;
    }

    /**
     * Confirms this intent (CREATED -> CONFIRMED).
     */
    public void confirm() {
        if (status != PaymentIntentStatus.CREATED) {
            throw new IllegalStateException("Illegal intent transition from " + status);
        }
        if (!isValid()) {
            throw new IllegalStateException("Intent is not valid");
        }
        setStatus(PaymentIntentStatus.CONFIRMED);
    }

    /**
     * Expires this intent (CREATED -> EXPIRED).
     */
    public void expire() {
        if (status != PaymentIntentStatus.CREATED) {
            throw new IllegalStateException("Illegal intent transition from " + status);
        }
        setStatus(PaymentIntentStatus.EXPIRED);
    }

    /**
     * Returns true if the intent is CREATED and within its TTL window.
     */
    public boolean isValid() {
        if (status != PaymentIntentStatus.CREATED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return createdAt.plus(DEFAULT_TTL).isAfter(now);
    }

    private static List<CartItemSnapshot> freezeSnapshot(List<CartItemSnapshot> items) {
        ValidationUtils.validateNotBlank(items, "snapshotItems");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("snapshotItems cannot be empty");
        }
        List<CartItemSnapshot> copy = new ArrayList<>(items.size());
        for (CartItemSnapshot item : items) {
            ValidationUtils.validateNotBlank(item, "snapshotItem");
            copy.add(item);
        }
        return Collections.unmodifiableList(copy);
    }

    private static BigDecimal sumSnapshot(List<CartItemSnapshot> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItemSnapshot item : items) {
            BigDecimal line = item.soldPrice().multiply(BigDecimal.valueOf(item.quantity()));
            sum = sum.add(ValidationUtils.normalizeMoney(line, "lineSubtotal"));
        }
        return ValidationUtils.normalizeMoney(sum, "totalAmount");
    }

    private void setIntentId(UUID intentId) {
        ValidationUtils.validateUUID(intentId, "intentId");
        this.intentId = intentId;
    }

    private void setBankRefId(String bankRefId) {
        ValidationUtils.validateNotBlank(bankRefId, "bankRefId");
        this.bankRefId = bankRefId.toString().trim();
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setCustomerId(UUID customerId) {
        ValidationUtils.validateUUID(customerId, "customerId");
        this.customerId = customerId;
    }

    private void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = ValidationUtils.normalizeMoney(totalAmount, "totalAmount");
    }

    private void setCartSnapshot(String cartSnapshot) {
        if (cartSnapshot == null) {
            this.cartSnapshot = null;
            return;
        }
        ValidationUtils.validateNotBlank(cartSnapshot, "cartSnapshot");
        this.cartSnapshot = cartSnapshot;
    }

    private void setStatus(PaymentIntentStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
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
        if (!(o instanceof PaymentIntent)) {
            return false;
        }
        PaymentIntent that = (PaymentIntent) o;
        return Objects.equals(intentId, that.intentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(intentId);
    }

    @Override
    public String toString() {
        return "PaymentIntent{" +
                "intentId=" + intentId +
                ", bankRefId='" + bankRefId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
