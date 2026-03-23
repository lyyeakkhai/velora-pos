package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.plan_subscription.UserAccount;
import com.velora.app.core.domain.plan_subscription.UserAccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of UserAccountRepository.
 * Requirements: 14.2
 */
public class PostgresUserAccountRepository implements UserAccountRepository {

    @Override
    public UserAccount save(UserAccount account) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<UserAccount> findByUserId(UUID userId) {
        // TODO: implement JDBC select by userId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<UserAccount> findAllActive() {
        // TODO: implement JDBC select where status = ACTIVE
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
