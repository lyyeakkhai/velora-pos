package com.velora.app.modules.authModule.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.authModule.domain.Membership;

/**
 * Domain repository interface for Membership persistence operations.
 *
 * Pure domain interface — no framework or infrastructure dependencies.
 * Implementations are provided by the infrastructure layer.
 *
 * @author Velora Development Team
 * @version 1.0
 * @since 1.0
 */
public interface MembershipRepository {

    /**
     * Persists a membership and returns the saved instance.
     *
     * @param membership The membership to save (cannot be null)
     * @return The saved membership
     */
    Membership save(Membership membership);

    /**
     * Finds all memberships associated with a given user.
     *
     * @param userId The user UUID
     * @return A list of memberships for that user (may be empty)
     */
    List<Membership> findByUserId(UUID userId);

    /**
     * Finds all memberships associated with a given shop.
     *
     * @param shopId The shop UUID
     * @return A list of memberships for that shop (may be empty)
     */
    List<Membership> findByShopId(UUID shopId);

    /**
     * Finds the membership for a specific user within a specific shop.
     *
     * @param userId The user UUID
     * @param shopId The shop UUID
     * @return An Optional containing the membership, or empty if not found
     */
    Optional<Membership> findByUserAndShop(UUID userId, UUID shopId);

    /**
     * Finds a membership by its unique identifier.
     *
     * @param membershipId The membership UUID
     * @return An Optional containing the membership, or empty if not found
     */
    Optional<Membership> findById(UUID membershipId);

    /**
     * Removes a membership by its unique identifier.
     *
     * @param membershipId The membership UUID to delete
     */
    void deleteById(UUID membershipId);
}
