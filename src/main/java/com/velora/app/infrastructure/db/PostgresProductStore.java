package com.velora.app.infrastructure.db;

import com.velora.app.modules_inventory.domain.Product;
import com.velora.app.modules_inventory.domain.ProductStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of ProductStore.
 * Requirements: 14.4
 */
public class PostgresProductStore implements ProductStore {

    @Override
    public Product save(Product product) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Product> findByShopId(UUID shopId) {
        // TODO: implement JDBC select by shopId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsByShopIdAndName(UUID shopId, String name) {
        // TODO: implement JDBC existence check
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsBySlug(String slug) {
        // TODO: implement JDBC existence check
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
