package com.velora.app.modules.admin.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Membership;
import com.velora.app.core.domain.auth.MembershipRepository;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.auth.User;
import com.velora.app.core.domain.auth.UserAuthRepository;
import com.velora.app.core.domain.auth.UserRepository;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshot;
import com.velora.app.core.domain.payment.PlatformRevenueSnapshotRepository;
import com.velora.app.core.domain.storemanagement.Shop;
import com.velora.app.core.domain.storemanagement.ShopRepository;
import com.velora.app.core.domain.storemanagement.ShopStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for platform administration operations.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireRole} and
 * {@code requireNotNull} guard methods. All mutating operations require the
 * actor to hold the {@code SUPER_ADMIN} role.
 *
 * <p>Requirements: 16.1
 */
public class AdminService extends AbstractDomainService implements IAdminService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final MembershipRepository membershipRepository;
    private final ShopRepository shopRepository;
    private final PlatformRevenueSnapshotRepository revenueSnapshotRepository;

    public AdminService(
            UserRepository userRepository,
            UserAuthRepository userAuthRepository,
            MembershipRepository membershipRepository,
            ShopRepository shopRepository,
            PlatformRevenueSnapshotRepository revenueSnapshotRepository) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.membershipRepository = membershipRepository;
        this.shopRepository = shopRepository;
        this.revenueSnapshotRepository = revenueSnapshotRepository;
    }

    /**
     * Bans a user account by suspending it.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId      the ID of the admin performing the action
     * @param targetUserId the ID of the user to ban
     * @param reason       the reason for the ban (must not be null)
     * @return the updated {@link User}
     * @throws DomainException if the actor is not SUPER_ADMIN or the user is not found
     */
    @Override
    public User banUser(UUID actorId, UUID targetUserId, String reason) {
        requireNotNull(actorId, "actorId");
        requireNotNull(targetUserId, "targetUserId");
        requireNotNull(reason, "reason");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new DomainException("User not found: " + targetUserId));

        user.suspend();
        return userRepository.save(user);
    }

    /**
     * Revokes all active sessions for a user by invalidating their auth records.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId      the ID of the admin performing the action
     * @param targetUserId the ID of the user whose sessions to revoke
     * @throws DomainException if the actor is not SUPER_ADMIN or the user is not found
     */
    @Override
    public void revokeUserSessions(UUID actorId, UUID targetUserId) {
        requireNotNull(actorId, "actorId");
        requireNotNull(targetUserId, "targetUserId");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        userRepository.findById(targetUserId)
                .orElseThrow(() -> new DomainException("User not found: " + targetUserId));

        // Session revocation intent is recorded by re-saving the UserAuth record.
        // The infrastructure layer is responsible for invalidating active tokens/sessions
        // when it detects the record has been touched.
        userAuthRepository.findByUserId(targetUserId)
                .ifPresent(userAuthRepository::save);
    }

    /**
     * Bans a shop by transitioning its status to {@code BANNED}.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId the ID of the admin performing the action
     * @param shopId  the ID of the shop to ban
     * @param reason  the reason for the ban (must not be null)
     * @return the updated {@link Shop}
     * @throws DomainException if the actor is not SUPER_ADMIN or the shop is not found
     */
    @Override
    public Shop banShop(UUID actorId, UUID shopId, String reason) {
        requireNotNull(actorId, "actorId");
        requireNotNull(shopId, "shopId");
        requireNotNull(reason, "reason");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new DomainException("Shop not found: " + shopId));

        shop.transitionStatus(ShopStatus.BANNED, true);
        return shopRepository.save(shop);
    }

    /**
     * Lifts a ban on a shop by transitioning its status to {@code SUSPENDED}.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId the ID of the admin performing the action
     * @param shopId  the ID of the shop to unban
     * @return the updated {@link Shop}
     * @throws DomainException if the actor is not SUPER_ADMIN or the shop is not found
     */
    @Override
    public Shop unbanShop(UUID actorId, UUID shopId) {
        requireNotNull(actorId, "actorId");
        requireNotNull(shopId, "shopId");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new DomainException("Shop not found: " + shopId));

        shop.transitionStatus(ShopStatus.SUSPENDED, true);
        return shopRepository.save(shop);
    }

    /**
     * Returns platform revenue snapshots within the given date range (inclusive).
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId        the ID of the admin performing the action
     * @param startInclusive the start date of the range
     * @param endInclusive   the end date of the range
     * @return list of {@link PlatformRevenueSnapshot} found in the range; never null
     * @throws DomainException if startInclusive is after endInclusive
     */
    @Override
    public List<PlatformRevenueSnapshot> viewRevenueSnapshots(UUID actorId, LocalDate startInclusive,
            LocalDate endInclusive) {
        requireNotNull(actorId, "actorId");
        requireNotNull(startInclusive, "startInclusive");
        requireNotNull(endInclusive, "endInclusive");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        if (startInclusive.isAfter(endInclusive)) {
            throw new DomainException("startInclusive must not be after endInclusive");
        }

        List<PlatformRevenueSnapshot> results = new ArrayList<>();
        LocalDate cursor = startInclusive;
        while (!cursor.isAfter(endInclusive)) {
            revenueSnapshotRepository.findByDate(cursor).ifPresent(results::add);
            cursor = cursor.plusDays(1);
        }
        return results;
    }

    /**
     * Changes the role for a membership.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId      the ID of the admin performing the action
     * @param membershipId the ID of the membership to update
     * @param newRole      the new role to assign
     * @throws DomainException if the actor is not SUPER_ADMIN or the membership is not found
     */
    @Override
    public void changePermissions(UUID actorId, UUID membershipId, Role.RoleName newRole) {
        requireNotNull(actorId, "actorId");
        requireNotNull(membershipId, "membershipId");
        requireNotNull(newRole, "newRole");

        requireRole(resolveActorRole(actorId), Role.RoleName.SUPER_ADMIN);

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new DomainException("Membership not found: " + membershipId));

        // Role resolution: in a full implementation a RoleRepository would look up the UUID
        // by RoleName. Here we generate a stable placeholder UUID from the role name ordinal.
        UUID newRoleId = UUID.nameUUIDFromBytes(newRole.name().getBytes());
        membership.setRoleId(newRoleId);
        membershipRepository.save(membership);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Resolves the highest-privilege role for the given actor from their memberships.
     * Returns {@code SELLER} as the default when no membership exists.
     */
    private Role.RoleName resolveActorRole(UUID actorId) {
        return membershipRepository.findByUserId(actorId).stream()
                .map(m -> resolveRoleNameFromId(m.getRoleId()))
                .reduce(Role.RoleName.SELLER, AdminService::highestPrivilege);
    }

    /**
     * Placeholder — returns {@code SELLER} until a RoleRepository is wired in.
     */
    private Role.RoleName resolveRoleNameFromId(UUID roleId) {
        // TODO: inject RoleRepository and look up by roleId
        return Role.RoleName.SELLER;
    }

    private static Role.RoleName highestPrivilege(Role.RoleName a, Role.RoleName b) {
        return ordinal(a) <= ordinal(b) ? a : b;
    }

    private static int ordinal(Role.RoleName r) {
        return switch (r) {
            case SUPER_ADMIN -> 0;
            case OWNER       -> 1;
            case MANAGER     -> 2;
            case SELLER      -> 3;
        };
    }
}
