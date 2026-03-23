package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.plan_subscription.ShopAccount;
import com.velora.app.core.domain.plan_subscription.ShopAccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of ShopAccountRepository.
 * Requirements: 14.2
 */
public class PostgresShopAccountRepository implements ShopAccountRepository {

    @Override
    public ShopAccount save(ShopAccount account) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<ShopAccount> findByShopId(UUID shopId) {
        // TODO: implement JDBC select by shopId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ShopAccount> findAllActive() {
        // TODO: implement JDBC select where status = ACTIVE
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
