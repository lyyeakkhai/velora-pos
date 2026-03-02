package com.velora.app.core.domain.inventoryeventmanagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Product with pricing and shop/category scope.
 */
public class Product {

    private UUID productId;
    private String name;
    private String slug;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private UUID categoryId;
    private UUID shopId;
    private LocalDateTime createdAt;
    private boolean disabled;

    public Product(UUID shopId, UUID categoryId, String name, String slug, BigDecimal basePrice, BigDecimal costPrice) {
        setProductId(UUID.randomUUID());
        setShopId(shopId);
        setCategoryId(categoryId);
        setName(name);
        setSlug(slug);
        setBasePrice(basePrice);
        setCostPrice(costPrice);
        ValidationUtils.validateCostNotAboveBase(this.costPrice, this.basePrice, "costPrice", "basePrice");
        setCreatedAt(LocalDateTime.now());
        setDisabled(false);
    }

    public UUID getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void updateProduct(String name, String slug, BigDecimal basePrice, BigDecimal costPrice, UUID categoryId) {
        requireNotDisabled();
        if (name != null) {
            setName(name);
        }
        if (slug != null) {
            setSlug(slug);
        }
        if (basePrice != null) {
            setBasePrice(basePrice);
        }
        if (costPrice != null) {
            setCostPrice(costPrice);
        }
        if (categoryId != null) {
            setCategoryId(categoryId);
        }
        ValidationUtils.validateCostNotAboveBase(this.costPrice, this.basePrice, "costPrice", "basePrice");
    }

    public void disable() {
        setDisabled(true);
    }

    private void requireNotDisabled() {
        if (disabled) {
            throw new IllegalStateException("Product is disabled");
        }
    }

    private void setProductId(UUID productId) {
        ValidationUtils.validateUUID(productId, "productId");
        this.productId = productId;
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        String normalized = name.toString().trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        this.name = normalized;
    }

    private void setSlug(String slug) {
        ValidationUtils.validateSlug(slug, "slug");
        this.slug = slug;
    }

    private void setBasePrice(BigDecimal basePrice) {
        this.basePrice = ValidationUtils.normalizePositiveMoney(basePrice, "basePrice");
    }

    private void setCostPrice(BigDecimal costPrice) {
        this.costPrice = ValidationUtils.normalizePositiveMoney(costPrice, "costPrice");
    }

    private void setCategoryId(UUID categoryId) {
        ValidationUtils.validateUUID(categoryId, "categoryId");
        this.categoryId = categoryId;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setCreatedAt(LocalDateTime createdAt) {
        ValidationUtils.validateNotBlank(createdAt, "createdAt");
        this.createdAt = createdAt;
    }

    private void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", shopId=" + shopId +
                ", categoryId=" + categoryId +
                ", disabled=" + disabled +
                '}';
    }
}
