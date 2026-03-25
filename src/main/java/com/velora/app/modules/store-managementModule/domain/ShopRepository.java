package com.velora.app.core.domain.storemanagement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Shop persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public interface ShopRepository {

    /**
     * Persists a shop and returns the saved instance.
     *
     * @param shop The shop to save (cannot be null)
     * @return The saved shop
     */
    Shop save(Shop shop);

    /**
     * Finds a shop by its unique identifier.
     *
     * @param id The shop UUID
     * @return An Optional containing the shop, or empty if not found
     */
    Optional<Shop> findById(UUID id);

    /**
     * Finds a shop by its URL slug.
     *
     * @param slug The slug to search for
     * @return An Optional containing the shop, or empty if not found
     */
    Optional<Shop> findBySlug(String slug);

    /**
     * Finds all shops owned by a given user.
     *
     * @param ownerId The owner's user UUID
     * @return A list of shops for that owner (may be empty)
     */
    List<Shop> findByOwnerId(UUID ownerId);

    /**
     * Checks whether a shop with the given slug already exists.
     *
     * @param slug The slug to check
     * @return true if a shop with that slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
}
