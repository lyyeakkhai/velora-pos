package com.velora.app.modules.sale.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.common.AbstractEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a line item in an order.
 * 
 * <p>
 * OrderItem is immutable once created - prices are locked at the time of order.
 * This ensures cart snapshot protection.
 */
public class OrderItem extends AbstractEntity {

    private final UUID productId;
    private final UUID variantId;
    private final String productName;
    private final String variantDescription;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    /**
     * Creates a new OrderItem with calculated subtotal.
     *
     * @param orderItemId        The unique order item identifier
     * @param productId          The product identifier
     * @param variantId          The variant identifier (may be null for non-variant
     *                           products)
     * @param productName        The product name at time of order (snapshot)
     * @param variantDescription The variant description (may be null)
     * @param quantity           The quantity ordered (must be > 0)
     * @param unitPrice          The price per unit at time of order (must be >= 0)
     */
    public OrderItem(UUID orderItemId, UUID productId, UUID variantId,
            String productName, String variantDescription,
            int quantity, BigDecimal unitPrice) {
        super(orderItemId);
        ValidationUtils.validateUUID(productId, "productId");
        ValidationUtils.validateNotBlank(productName, "productName");
        ValidationUtils.validatePositiveInteger(quantity, "quantity");
        ValidationUtils.validateNotBlank(unitPrice, "unitPrice");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }

        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantDescription = variantDescription;
        this.quantity = quantity;
        this.unitPrice = ValidationUtils.normalizeMoney(unitPrice, "unitPrice");
        this.subtotal = calculateSubtotal();
    }

    /**
     * Gets the product identifier.
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * Gets the variant identifier.
     */
    public UUID getVariantId() {
        return variantId;
    }

    /**
     * Gets the product name (snapshot).
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Gets the variant description (snapshot).
     */
    public String getVariantDescription() {
        return variantDescription;
    }

    /**
     * Gets the quantity ordered.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the unit price (snapshot).
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Gets the calculated subtotal (quantity * unitPrice).
     */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /**
     * Calculates subtotal from quantity and unit price.
     */
    private BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + getId() +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
