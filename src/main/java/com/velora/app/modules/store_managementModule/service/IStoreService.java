package com.velora.app.modules.store_managementModule.service;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.store_managementModule.domain.Address;
import com.velora.app.modules.store_managementModule.domain.Shop;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application-layer contract for shop registration and lifecycle management.
 *
 * <p>
 * Requirement: 16.1, 16.4
 */
public interface IStoreService {

    /**
     * Registers a new shop for the given owner.
     */
    Shop registerShop(UUID ownerId, String slug, String legalName, String taxId, Address physicalAddress);

    /**
     * Transitions a shop to VERIFIED status.
     */
    Shop verifyShop(UUID shopId, Role.RoleName actorRole);

    /**
     * Transitions a shop to ACTIVE status.
     */
    Shop activateShop(UUID shopId, Role.RoleName actorRole);

    /**
     * Suspends a shop.
     */
    Shop suspendShop(UUID shopId, Role.RoleName actorRole);

    /**
     * Bans a shop permanently.
     */
    Shop banShop(UUID shopId, Role.RoleName actorRole);

    /**
     * Lifts a ban on a shop.
     */
    Shop unbanShop(UUID shopId, Role.RoleName actorRole);

    /**
     * Updates the shop's physical address.
     */
    Shop updateAddress(UUID shopId, Address newAddress, Role.RoleName actorRole);

    /**
     * Calculates the shop's payout from a gross amount after platform fee
     * deduction.
     */
    BigDecimal calculatePayout(UUID shopId, BigDecimal grossAmount);
}
