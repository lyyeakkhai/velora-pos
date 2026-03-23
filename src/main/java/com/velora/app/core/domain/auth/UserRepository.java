package com.velora.app.core.domain.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for User persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public interface UserRepository {

    /**
     * Persists a user and returns the saved instance.
     *
     * @param user The user to save (cannot be null)
     * @return The saved user
     */
    User save(User user);

    /**
     * Finds a user by their unique identifier.
     *
     * @param id The user UUID
     * @return An Optional containing the user, or empty if not found
     */
    Optional<User> findById(UUID id);

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for
     * @return An Optional containing the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username The username to check
     * @return true if a user with that username exists, false otherwise
     */
    boolean existsByUsername(String username);
}
