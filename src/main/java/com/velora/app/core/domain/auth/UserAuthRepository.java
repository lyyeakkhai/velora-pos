package com.velora.app.core.domain.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for UserAuth persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public interface UserAuthRepository {

    /**
     * Persists a UserAuth record and returns the saved instance.
     *
     * @param userAuth The UserAuth to save (cannot be null)
     * @return The saved UserAuth
     */
    UserAuth save(UserAuth userAuth);

    /**
     * Finds a UserAuth record by email address.
     *
     * @param email The email address to search for
     * @return An Optional containing the UserAuth, or empty if not found
     */
    Optional<UserAuth> findByEmail(String email);

    /**
     * Finds a UserAuth record by the associated user ID.
     *
     * @param userId The user UUID
     * @return An Optional containing the UserAuth, or empty if not found
     */
    Optional<UserAuth> findByUserId(UUID userId);

    /**
     * Checks whether a UserAuth record with the given email already exists.
     *
     * @param email The email address to check
     * @return true if a record with that email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
