package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Category business service.
 */
public class CategoryService {

    private final CategoryStore categoryStore;

    public CategoryService(CategoryStore categoryStore) {
        this.categoryStore = require(categoryStore, "categoryStore");
    }

    public Category createCategory(Role.RoleName actorRole, UUID shopId, String name) {
        RolePolicy.requireCatalogWrite(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(name, "name");
        if (categoryStore.existsByShopIdAndName(shopId, name.trim())) {
            throw new IllegalStateException("Category name must be unique per shop");
        }
        return categoryStore.save(new Category(shopId, name));
    }

    public void validateOwnership(Role.RoleName actorRole) {
        RolePolicy.requireOwner(actorRole);
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}
