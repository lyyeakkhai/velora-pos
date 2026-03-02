package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for categories.
 */
public interface CategoryStore {
    boolean existsByShopIdAndName(UUID shopId, String name);

    Category save(Category category);

    Optional<Category> findById(UUID categoryId);
}
