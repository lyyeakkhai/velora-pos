package com.velora.app.modules.store_managementModule.domain;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain service orchestrating registration, status transitions, and payouts.
 */
public class StoreManagementService {

    private final ShopRepository shopRepository;

    public StoreManagementService(ShopRepository shopRepository) {
        ValidationUtils.validateNotBlank(shopRepository, "shopRepository");
        this.shopRepository = shopRepository;
    }

    public Shop registerShop(UUID ownerId, String slug, String legalName, String taxId, Address physicalAddress) {
        ValidationUtils.validateUUID(ownerId, "ownerId");
        ValidationUtils.validateSlug(slug, "slug");
        ValidationUtils.validateNotBlank(physicalAddress, "physicalAddress");

        if (shopRepository.existsBySlug(slug)) {
            throw new IllegalStateException("slug must be unique");
        }

        Shop shop = new Shop(UUID.randomUUID(), ownerId, slug, physicalAddress);
        if (legalName != null) {
            shop.setLegalName(legalName);
        }
        if (taxId != null) {
            shop.setTaxId(taxId);
        }

        shopRepository.save(shop);
        return shop;
    }

    public Shop updateShopStatus(UUID shopId, ShopStatus newStatus, Role.RoleName actorRole) {
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateNotBlank(newStatus, "newStatus");
        ValidationUtils.validateNotBlank(actorRole, "actorRole");

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalStateException("shop not found"));

        boolean adminOverride = actorRole == Role.RoleName.SUPER_ADMIN;
        shop.transitionStatus(newStatus, adminOverride);
        shopRepository.save(shop);
        return shop;
    }

    /**
     * Computes shop payout from a gross amount using the shop's platform fee rate.
     */
    public BigDecimal payoutCalculation(UUID shopId, BigDecimal grossAmount) {
        ValidationUtils.validateUUID(shopId, "shopId");
        BigDecimal gross = ValidationUtils.normalizeMoney(grossAmount, "grossAmount");

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalStateException("shop not found"));

        BigDecimal feeRate = shop.getShopSettings().getPlatformFeeRatePercent();
        BigDecimal fee = gross.multiply(feeRate).divide(new BigDecimal("100.00"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal payout = gross.subtract(fee);
        return payout.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
