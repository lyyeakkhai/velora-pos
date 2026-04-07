package com.velora.app.modules.store_managementModule.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.modules.authModule.domain.Membership;
import com.velora.app.modules.authModule.Repository.MembershipRepository;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.store_managementModule.domain.Address;
import com.velora.app.modules.store_managementModule.domain.Shop;
import com.velora.app.modules.store_managementModule.domain.ShopRepository;
import com.velora.app.modules.store_managementModule.domain.ShopStatus;
import com.velora.app.modules.store_managementModule.domain.StoreManagementService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application-layer service for shop registration and lifecycle management.
 *
 * <p>
 * Extends {@link AbstractDomainService} to reuse {@code requireRole} and
 * {@code requireNotNull} guard methods. Delegates domain logic to
 * {@link StoreManagementService}.
 *
 * <p>
 * Requirements: 16.1, 16.4
 */
public class StoreService extends AbstractDomainService implements IStoreService {

    private final StoreManagementService storeManagementService;
    private final ShopRepository shopRepository;
    private final MembershipRepository membershipRepository;

    public StoreService(
            ShopRepository shopRepository,
            MembershipRepository membershipRepository) {
        this.shopRepository = shopRepository;
        this.membershipRepository = membershipRepository;
        this.storeManagementService = new StoreManagementService(shopRepository);
    }

    /**
     * Registers a new shop for the given owner.
     *
     * @param ownerId         the UUID of the shop owner
     * @param slug            the unique URL slug for the shop
     * @param legalName       the shop's legal business name (optional at
     *                        registration)
     * @param taxId           the shop's tax identifier (optional at registration)
     * @param physicalAddress the shop's physical address
     * @return the persisted {@link Shop}
     * @throws DomainException if the slug is already taken
     */
    @Override
    public Shop registerShop(UUID ownerId, String slug, String legalName, String taxId, Address physicalAddress) {
        requireNotNull(ownerId, "ownerId");
        requireNotNull(slug, "slug");
        requireNotNull(physicalAddress, "physicalAddress");

        return storeManagementService.registerShop(ownerId, slug, legalName, taxId, physicalAddress);
    }

    /**
     * Transitions a shop to VERIFIED status.
     *
     * <p>
     * Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param shopId    the UUID of the shop to verify
     * @param actorRole the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role or the shop is
     *                         not found
     */
    @Override
    public Shop verifyShop(UUID shopId, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        // VERIFIED is not a distinct status in ShopStatus — verification transitions
        // the shop to ACTIVE
        return storeManagementService.updateShopStatus(shopId, ShopStatus.ACTIVE, actorRole);
    }

    /**
     * Transitions a shop to ACTIVE status.
     *
     * <p>
     * Requires the actor to hold the {@code OWNER} or {@code SUPER_ADMIN} role.
     * The shop must have a legal name and tax ID set before activation.
     *
     * @param shopId    the UUID of the shop to activate
     * @param actorRole the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role, the shop is not
     *                         found,
     *                         or the shop is missing required legal identity fields
     */
    @Override
    public Shop activateShop(UUID shopId, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        return storeManagementService.updateShopStatus(shopId, ShopStatus.ACTIVE, actorRole);
    }

    /**
     * Suspends a shop.
     *
     * <p>
     * Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param shopId    the UUID of the shop to suspend
     * @param actorRole the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role or the shop is
     *                         not found
     */
    @Override
    public Shop suspendShop(UUID shopId, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        return storeManagementService.updateShopStatus(shopId, ShopStatus.SUSPENDED, actorRole);
    }

    /**
     * Bans a shop permanently.
     *
     * <p>
     * Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param shopId    the UUID of the shop to ban
     * @param actorRole the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role or the shop is
     *                         not found
     */
    @Override
    public Shop banShop(UUID shopId, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        return storeManagementService.updateShopStatus(shopId, ShopStatus.BANNED, actorRole);
    }

    /**
     * Lifts a ban on a shop, transitioning it back to SUSPENDED status.
     *
     * <p>
     * Requires the actor to hold the {@code SUPER_ADMIN} role.
     * Only a SUPER_ADMIN can override a BANNED status.
     *
     * @param shopId    the UUID of the shop to unban
     * @param actorRole the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role or the shop is
     *                         not found
     */
    @Override
    public Shop unbanShop(UUID shopId, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        return storeManagementService.updateShopStatus(shopId, ShopStatus.SUSPENDED, actorRole);
    }

    /**
     * Updates the shop's physical address.
     *
     * <p>
     * Requires the actor to hold the {@code OWNER} or {@code SUPER_ADMIN} role.
     *
     * @param shopId     the UUID of the shop to update
     * @param newAddress the new physical address
     * @param actorRole  the role of the actor performing the action
     * @return the updated {@link Shop}
     * @throws DomainException if the actor lacks the required role or the shop is
     *                         not found
     */
    @Override
    public Shop updateAddress(UUID shopId, Address newAddress, Role.RoleName actorRole) {
        requireNotNull(shopId, "shopId");
        requireNotNull(newAddress, "newAddress");
        requireNotNull(actorRole, "actorRole");
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new DomainException("Shop not found: " + shopId));

        shop.updateAddress(newAddress);
        return shopRepository.save(shop);
    }

    /**
     * Calculates the shop's payout from a gross amount after platform fee
     * deduction.
     *
     * @param shopId      the UUID of the shop
     * @param grossAmount the gross sale amount before fee deduction
     * @return the net payout amount after the platform fee is subtracted
     * @throws DomainException if the shop is not found or the gross amount is
     *                         invalid
     */
    @Override
    public BigDecimal calculatePayout(UUID shopId, BigDecimal grossAmount) {
        requireNotNull(shopId, "shopId");
        requireNotNull(grossAmount, "grossAmount");

        return storeManagementService.payoutCalculation(shopId, grossAmount);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Resolves the highest-privilege role for the given actor from their
     * memberships.
     * Returns {@code SELLER} as the default when no membership exists.
     */
    private Role.RoleName resolveActorRole(UUID actorId) {
        return membershipRepository.findByUserId(actorId).stream()
                .map(m -> resolveRoleNameFromId(m.getRoleId()))
                .reduce(Role.RoleName.SELLER, StoreService::highestPrivilege);
    }

    /**
     * Placeholder — returns SELLER until a RoleRepository is wired in.
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
            case OWNER -> 1;
            case MANAGER -> 2;
            case SELLER -> 3;
        };
    }
}
