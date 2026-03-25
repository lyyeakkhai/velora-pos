package com.velora.app.modules.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for products.
 */
public interface ProductStore {
    boolean existsByShopIdAndName(UUID shopId, String name);

    boolean existsBySlug(String slug);

    Product save(Product product);

    Optional<Product> findById(UUID productId);

    List<Product> findByShopId(UUID shopId);
}

