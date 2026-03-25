package com.velora.app.infrastructure.db;

import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.plan_subscriptionModule.domain.PlatformRegistry;
import com.velora.app.modules.plan_subscriptionModule.domain.PlatformRegistryRepository;

/**
 * PostgreSQL implementation of PlatformRegistryRepository.
 * Requirements: 14.2
 */
public class PostgresPlatformRegistryRepository implements PlatformRegistryRepository {

    @Override
    public PlatformRegistry save(PlatformRegistry registry) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<PlatformRegistry> findById(UUID registryId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<PlatformRegistry> findByOwnerId(UUID ownerId) {
        // TODO: implement JDBC select by ownerId
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
