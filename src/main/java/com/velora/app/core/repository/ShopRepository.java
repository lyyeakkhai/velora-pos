package com.velora.app.core.repository;

import com.velora.app.core.domain.Shop;
import java.util.Optional;

public interface ShopRepository {
    Optional<Shop> findById(String id);

    void save(Shop shop);
}
