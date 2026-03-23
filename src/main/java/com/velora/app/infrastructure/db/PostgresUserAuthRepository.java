package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.auth.UserAuth;
import com.velora.app.core.domain.auth.UserAuthRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of UserAuthRepository.
 * Requirements: 14.1
 */
public class PostgresUserAuthRepository implements UserAuthRepository {

    @Override
    public UserAuth save(UserAuth userAuth) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<UserAuth> findByEmail(String email) {
        // TODO: implement JDBC select by email
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<UserAuth> findByUserId(UUID userId) {
        // TODO: implement JDBC select by userId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsByEmail(String email) {
        // TODO: implement JDBC existence check
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
