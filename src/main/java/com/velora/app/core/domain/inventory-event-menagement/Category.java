package com.velora.app.core.domain.inventoryeventmanagement;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Product category scoped by shop.
 */
public class Category {

    private UUID categoryId;
    private UUID shopId;
    private String name;
    private LocalDateTime createdAt;

    public Category(UUID shopId, String name) {
        setCategoryId(UUID.randomUUID());
        setShopId(shopId);
        setName(name);
        setCreatedAt(LocalDateTime.now());
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void rename(String newName) {
        setName(newName);
    }

    private void setCategoryId(UUID categoryId) {
        ValidationUtils.validateUUID(categoryId, "categoryId");
        this.categoryId = categoryId;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        String normalized = name.toString().trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        this.name = normalized;
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
        if (!(o instanceof Category)) {
            return false;
        }
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", shopId=" + shopId +
                ", name='" + name + '\'' +
                '}';
    }
}
