package com.velora.app.modules.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Product with pricing and shop/category scope.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class Product extends AbstractAuditableEntity {

    private String name;
    private String slug;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private UUID categoryId;
    private UUID shopId;
    private boolean disabled;

    public Product(UUID shopId, UUID categoryId, String name, String slug, BigDecimal basePrice, BigDecimal costPrice) {
        super(UUID.randomUUID());
        setShopId(shopId);
        setCategoryId(categoryId);
        setName(name);
        setSlug(slug);
        setBasePrice(basePrice);
        setCostPrice(costPrice);
        ValidationUtils.validateCostNotAboveBase(this.costPrice, this.basePrice, "costPrice", "basePrice");
        this.disabled = false;
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
        touch();
    }

    public void disable() {
        this.disabled = true;
        touch();
    }

    private void requireNotDisabled() {
        if (disabled) {
            throw new IllegalStateException("Product is disabled");
        }
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        String normalized = name.trim().replaceAll("\\s+", " ");
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

    @Override
    public String toString() {
        return "Product{id=" + getId() +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", shopId=" + shopId +
                ", categoryId=" + categoryId +
                ", disabled=" + disabled +
                '}';
    }
}
