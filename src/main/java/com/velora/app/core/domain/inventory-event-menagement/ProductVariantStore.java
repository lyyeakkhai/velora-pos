package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for product variants.
 */
public interface ProductVariantStore {
    boolean existsBySku(String sku);

    List<ProductVariant> saveAll(List<ProductVariant> variants);

    Optional<ProductVariant> findById(UUID variantId);
}
