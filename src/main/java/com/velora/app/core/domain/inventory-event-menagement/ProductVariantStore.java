package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for product variants.
 */
public interface ProductVariantStore {
    boolean existsBySku(String sku);

    ProductVariant save(ProductVariant variant);

    List<ProductVariant> saveAll(List<ProductVariant> variants);

    Optional<ProductVariant> findById(UUID variantId);

    List<ProductVariant> findByProductId(UUID productId);

    Optional<ProductVariant> findBySku(String sku);
}
