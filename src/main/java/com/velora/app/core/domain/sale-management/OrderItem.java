package com.velora.app.core.domain.salemanagement;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Immutable snapshot item within an order.
 */
public class OrderItem {

    private UUID orderItemId;
    private UUID orderId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal soldPrice;
    private BigDecimal subtotal;

    /**
     * Creates an order item with mandatory fields. orderItemId is generated.
     */
    public OrderItem(UUID orderId, UUID productId, Integer quantity, BigDecimal soldPrice) {
        setOrderItemId(UUID.randomUUID());
        setOrderId(orderId);
        setProductId(productId);
        setQuantity(quantity);
        setSoldPrice(soldPrice);
        setSubtotal(calculateSubtotal());
    }

    public UUID getOrderItemId() {
        return orderItemId;
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

    private void setOrderItemId(UUID orderItemId) {
        ValidationUtils.validateUUID(orderItemId, "orderItemId");
        this.orderItemId = orderItemId;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderItem)) {
            return false;
        }
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", soldPrice=" + soldPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
