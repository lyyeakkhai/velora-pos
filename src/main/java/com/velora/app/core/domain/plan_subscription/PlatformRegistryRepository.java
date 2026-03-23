package com.velora.app.core.domain.plan_subscription;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for PlatformRegistry persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * Requirements: 14.2
 */
public interface PlatformRegistryRepository {

    /**
     * Persists a platform registry entry and returns the saved instance.
     *
     * @param registry The registry to save (cannot be null)
     * @return The saved registry
     */
    PlatformRegistry save(PlatformRegistry registry);

    /**
     * Finds a platform registry by its unique identifier.
     *
     * @param registryId The registry UUID
     * @return An Optional containing the registry, or empty if not found
     */
    Optional<PlatformRegistry> findById(UUID registryId);

    /**
     * Finds the platform registry belonging to a given owner (user or shop).
     *
     * @param ownerId The owner UUID
     * @return An Optional containing the registry, or empty if not found
     */
    Optional<PlatformRegistry> findByOwnerId(UUID ownerId);
}
