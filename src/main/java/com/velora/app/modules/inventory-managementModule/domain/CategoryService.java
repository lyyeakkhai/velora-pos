package com.velora.app.modules.inventory.domain;

import java.util.UUID;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Category business service.
 */
public class CategoryService {

    private final CategoryStore categoryStore;
    private final RolePolicy policy;

    public CategoryService(CategoryStore categoryStore) {
        this.categoryStore = require(categoryStore, "categoryStore");
        this.policy = new RolePolicy();
    }

    public Category createCategory(Role.RoleName actorRole, UUID shopId, String name) {
        policy.requireCatalogWrite(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(name, "name");
        if (categoryStore.existsByShopIdAndName(shopId, name.trim())) {
            throw new IllegalStateException("Category name must be unique per shop");
        }
        return categoryStore.save(new Category(shopId, name));
    }

    public void validateOwnership(Role.RoleName actorRole) {
        policy.requireOwner(actorRole);
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}

