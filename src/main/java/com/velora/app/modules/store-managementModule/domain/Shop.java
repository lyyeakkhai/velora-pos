package com.velora.app.core.domain.storemanagement;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Aggregate root for store/vendor shop lifecycle.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class Shop extends AbstractAuditableEntity {

    private final UUID ownerId;
    private String legalName;
    private String taxId;
    private String slug;
    private ShopStatus status;
    private Address physicalAddress;
    private ShopAccount shopAccount;
    private ShopSettings shopSettings;

    public Shop(UUID id, UUID ownerId, String slug, Address physicalAddress) {
        super(id);
        ValidationUtils.validateUUID(ownerId, "ownerId");
        setSlug(slug);
        setPhysicalAddress(physicalAddress);
        this.ownerId = ownerId;
        this.status = ShopStatus.PENDING;
        this.shopSettings = ShopSettings.defaultSettings();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = normalizeOptionalTrimmed(legalName, "legalName");
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = normalizeOptionalTrimmed(taxId, "taxId");
        if (this.taxId != null && this.taxId.length() < 3) {
            throw new IllegalArgumentException("taxId is too short");
        }
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        ValidationUtils.validateSlug(slug, "slug");
        this.slug = slug.trim();
    }

    public ShopStatus getStatus() {
        return status;
    }

    public void transitionStatus(ShopStatus newStatus, boolean adminOverride) {
        ValidationUtils.validateNotBlank(newStatus, "status");
        if (status == ShopStatus.BANNED && newStatus != ShopStatus.BANNED && !adminOverride) {
            throw new IllegalStateException("BANNED shops cannot revert without admin approval");
        }
        if (newStatus == ShopStatus.ACTIVE) {
            requireLegalIdentityForActivation();
        }
        this.status = newStatus;
        touch();
    }

    private void requireLegalIdentityForActivation() {
        if (legalName == null || legalName.isBlank()) {
            throw new IllegalStateException("Shop cannot be ACTIVE without legal_name");
        }
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalStateException("Shop cannot be ACTIVE without tax_id");
        }
    }

    public Address getPhysicalAddress() {
        return physicalAddress;
    }

    public void updateAddress(Address newAddress) {
        ValidationUtils.validateNotBlank(newAddress, "physicalAddress");
        this.physicalAddress = newAddress;
        touch();
    }

    public void setPhysicalAddress(Address physicalAddress) {
        ValidationUtils.validateNotBlank(physicalAddress, "physicalAddress");
        this.physicalAddress = physicalAddress;
    }

    public ShopAccount getShopAccount() {
        return shopAccount;
    }

    public void setShopAccount(ShopAccount shopAccount) {
        ValidationUtils.validateNotBlank(shopAccount, "shopAccount");
        if (!shopAccount.getShopId().equals(getId())) {
            throw new IllegalArgumentException("shopAccount.shopId must match shopId");
        }
        this.shopAccount = shopAccount;
    }

    public ShopSettings getShopSettings() {
        return shopSettings;
    }

    public void setShopSettings(ShopSettings shopSettings) {
        ValidationUtils.validateNotBlank(shopSettings, "shopSettings");
        this.shopSettings = shopSettings;
    }

    private static String normalizeOptionalTrimmed(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
