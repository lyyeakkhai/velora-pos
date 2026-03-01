package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.Shop;
import com.velora.app.core.repository.ShopRepository;
import java.util.Optional;

public class PostgresShopRepository implements ShopRepository {

    public PostgresShopRepository() {
        // connection wiring would go here (DatabaseConfig)
    }

    @Override
    public Optional<Shop> findById(String id) {
        // TODO: implement JDBC access
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void save(Shop shop) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
