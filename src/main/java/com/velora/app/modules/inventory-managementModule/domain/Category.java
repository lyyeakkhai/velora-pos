package com.velora.app.modules.inventory.domain;

import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Product category scoped by shop.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class Category extends AbstractAuditableEntity {

    private UUID shopId;
    private String name;

    public Category(UUID shopId, String name) {
        super(UUID.randomUUID());
        setShopId(shopId);
        setName(name);
    }

    public UUID getShopId() {
        return shopId;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        setName(newName);
        touch();
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        String normalized = name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        this.name = normalized;
    }

    @Override
    public String toString() {
        return "Category{id=" + getId() +
                ", shopId=" + shopId +
                ", name='" + name + '\'' +
                '}';
    }
}

