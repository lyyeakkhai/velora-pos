package com.velora.app.core.domain.inventoryeventmanagement;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Variant of a product (e.g., size/color) with stock control and SKU.
 */
public class ProductVariant {

    private UUID variantId;
    private UUID productId;
    private String size;
    private String color;
    private Integer stockQuantity;
    private String sku;
    private UUID imageId;
    private UUID shopId;
    private UUID categoryId;
    private LocalDateTime createdAt;

    public ProductVariant(UUID productId, UUID shopId, UUID categoryId, String sku, Integer stockQuantity, UUID imageId,
            String size, String color) {
        setVariantId(UUID.randomUUID());
        setProductId(productId);
        setShopId(shopId);
        setCategoryId(categoryId);
        setSku(sku);
        setStockQuantity(stockQuantity);
        setImageId(imageId);
        setSize(size);
        setColor(color);
        setCreatedAt(LocalDateTime.now());
    }

    public UUID getVariantId() {
        return variantId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void adjustStock(int delta) {
        int next = stockQuantity + delta;
        if (next < 0) {
            throw new IllegalStateException("stockQuantity cannot go negative");
        }
        setStockQuantity(next);
    }

    private void setVariantId(UUID variantId) {
        ValidationUtils.validateUUID(variantId, "variantId");
        this.variantId = variantId;
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

    private void setCreatedAt(LocalDateTime createdAt) {
        ValidationUtils.validateNotBlank(createdAt, "createdAt");
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductVariant)) {
            return false;
        }
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantId);
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "variantId=" + variantId +
                ", productId=" + productId +
                ", sku='" + sku + '\'' +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
