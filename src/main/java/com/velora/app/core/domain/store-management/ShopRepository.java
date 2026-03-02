package com.velora.app.core.domain.storemanagement;

import java.util.Optional;
import java.util.UUID;

public interface ShopRepository {
    void save(Shop shop);

    Optional<Shop> findById(UUID shopId);

    boolean existsBySlug(String slug);
}
