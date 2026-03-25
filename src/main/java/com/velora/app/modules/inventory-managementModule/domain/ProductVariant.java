package com.velora.app.modules.inventory.domain;

import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Variant of a product (e.g., size/color) with stock control and SKU.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class ProductVariant extends AbstractAuditableEntity {

    private UUID productId;
    private String size;
    private String color;
    private Integer stockQuantity;
    private String sku;
    private UUID imageId;
    private UUID shopId;
    private UUID categoryId;

    public ProductVariant(UUID productId, UUID shopId, UUID categoryId, String sku, Integer stockQuantity,
            UUID imageId, String size, String color) {
        super(UUID.randomUUID());
        setProductId(productId);
        setShopId(shopId);
        setCategoryId(categoryId);
        setSku(sku);
        setStockQuantity(stockQuantity);
        setImageId(imageId);
        setSize(size);
        setColor(color);
    }

    public UUID getProductId() {
        return productId;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public String getSku() {
        return sku;
    }

    public UUID getImageId() {
        return imageId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void adjustStock(int delta) {
        int next = stockQuantity + delta;
        if (next < 0) {
            throw new IllegalStateException("stockQuantity cannot go negative");
        }
        setStockQuantity(next);
        touch();
    }

    private void setProductId(UUID productId) {
        ValidationUtils.validateUUID(productId, "productId");
        this.productId = productId;
    }

    private void setSize(String size) {
        if (size == null) {
            this.size = null;
            return;
        }
        ValidationUtils.validateNotBlank(size, "size");
        this.size = size.trim();
    }

    private void setColor(String color) {
        if (color == null) {
            this.color = null;
            return;
        }
        ValidationUtils.validateNotBlank(color, "color");
        this.color = color.trim();
    }

    private void setStockQuantity(Integer stockQuantity) {
        ValidationUtils.validateNonNegativeInteger(stockQuantity, "stockQuantity");
        this.stockQuantity = stockQuantity;
    }

    private void setSku(String sku) {
        ValidationUtils.validateSku(sku, "sku");
        this.sku = sku.trim();
    }

    private void setImageId(UUID imageId) {
        ValidationUtils.validateUUID(imageId, "imageId");
        this.imageId = imageId;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setCategoryId(UUID categoryId) {
        ValidationUtils.validateUUID(categoryId, "categoryId");
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "ProductVariant{id=" + getId() +
                ", productId=" + productId +
                ", sku='" + sku + '\'' +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}

