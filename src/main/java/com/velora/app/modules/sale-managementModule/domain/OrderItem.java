package com.velora.app.modules.sale_managementModule.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Immutable snapshot item within an order.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class OrderItem extends AbstractAuditableEntity {

    private UUID orderId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal soldPrice;
    private BigDecimal subtotal;

    /**
     * Creates an order item with mandatory fields. id is generated internally.
     */
    public OrderItem(UUID orderId, UUID productId, Integer quantity, BigDecimal soldPrice) {
        super(UUID.randomUUID());
        setOrderId(orderId);
        setProductId(productId);
        setQuantity(quantity);
        setSoldPrice(soldPrice);
        setSubtotal(calculateSubtotal());
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSoldPrice() {
        return soldPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /**
     * Calculates subtotal = soldPrice * quantity.
     */
    public BigDecimal calculateSubtotal() {
        ValidationUtils.validatePositiveInteger(quantity, "quantity");
        BigDecimal normalizedPrice = ValidationUtils.normalizeMoney(soldPrice, "soldPrice");
        return ValidationUtils.normalizeMoney(normalizedPrice.multiply(BigDecimal.valueOf(quantity)), "subtotal");
    }

    private void setOrderId(UUID orderId) {
        ValidationUtils.validateUUID(orderId, "orderId");
        this.orderId = orderId;
    }

    private void setProductId(UUID productId) {
        ValidationUtils.validateUUID(productId, "productId");
        this.productId = productId;
    }

    private void setQuantity(Integer quantity) {
        ValidationUtils.validatePositiveInteger(quantity, "quantity");
        this.quantity = quantity;
    }

    private void setSoldPrice(BigDecimal soldPrice) {
        this.soldPrice = ValidationUtils.normalizeMoney(soldPrice, "soldPrice");
    }

    private void setSubtotal(BigDecimal subtotal) {
        this.subtotal = ValidationUtils.normalizeMoney(subtotal, "subtotal");
    }

    @Override
    public String toString() {
        return "OrderItem{id=" + getId() +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", soldPrice=" + soldPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
