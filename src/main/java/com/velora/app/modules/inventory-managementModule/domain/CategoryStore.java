package com.velora.app.modules.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for categories.
 */
public interface CategoryStore {
    boolean existsByShopIdAndName(UUID shopId, String name);

    Category save(Category category);

    Optional<Category> findById(UUID categoryId);

    List<Category> findByShopId(UUID shopId);
}

