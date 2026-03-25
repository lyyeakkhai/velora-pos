package com.velora.app.modules.admin.service;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.authModule.domain.User;
import com.velora.app.modules.store_managementModule.domain.Shop;
import com.velora.app.modules.paymentModule.domain.PlatformRevenueSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for platform administration operations.
 *
 * <p>
 * Requirement: 16.1
 */
public interface IAdminService {

    /**
     * Bans a user account. Requires SUPER_ADMIN.
     */
    User banUser(UUID actorId, UUID targetUserId, String reason);

    /**
     * Revokes all active sessions for a user. Requires SUPER_ADMIN.
     */
    void revokeUserSessions(UUID actorId, UUID targetUserId);

    /**
     * Bans a shop. Requires SUPER_ADMIN.
     */
    Shop banShop(UUID actorId, UUID shopId, String reason);

    /**
     * Lifts a ban on a shop. Requires SUPER_ADMIN.
     */
    Shop unbanShop(UUID actorId, UUID shopId);

    /**
     * Returns platform revenue snapshots within the given date range.
     */
    List<PlatformRevenueSnapshot> viewRevenueSnapshots(UUID actorId, LocalDate startInclusive,
            LocalDate endInclusive);

    /**
     * Changes the role/permissions for a membership. Requires SUPER_ADMIN.
     */
    void changePermissions(UUID actorId, UUID membershipId, Role.RoleName newRole);
}
