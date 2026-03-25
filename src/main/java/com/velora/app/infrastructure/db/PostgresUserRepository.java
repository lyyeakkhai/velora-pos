package com.velora.app.infrastructure.db;

import com.velora.app.modules.authModule.domain.User;
import com.velora.app.modules.authModule.Repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of UserRepository.
 * Requirements: 14.1
 */
public class PostgresUserRepository implements UserRepository {

    @Override
    public User save(User user) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<User> findById(UUID id) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        // TODO: implement JDBC select by username
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsByUsername(String username) {
        // TODO: implement JDBC existence check
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
