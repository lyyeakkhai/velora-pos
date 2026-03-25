package com.velora.app.infrastructure.db;

import com.velora.app.modules.store_managementModule.domain.Shop;
import com.velora.app.modules.store_managementModule.domain.ShopRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of ShopRepository.
 * Requirements: 14.3
 */
public class PostgresShopRepository implements ShopRepository {

    @Override
    public Shop save(Shop shop) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Shop> findById(UUID id) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Shop> findBySlug(String slug) {
        // TODO: implement JDBC select by slug
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Shop> findByOwnerId(UUID ownerId) {
        // TODO: implement JDBC select by ownerId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsBySlug(String slug) {
        // TODO: implement JDBC existence check
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
