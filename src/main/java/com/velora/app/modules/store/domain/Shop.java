package com.velora.app.modules.store.domain;

import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Represents a vendor shop in the Velora platform.
 * 
 * <p>
 * The Shop is the aggregate root for the store management domain.
 * It manages shop lifecycle, verification status, and legal compliance.
 * 
 * <p>
 * State transitions:
 * <ul>
 * <li>PENDING → ACTIVE (requires legalName + taxId)</li>
 * <li>ACTIVE → SUSPENDED</li>
 * <li>SUSPENDED → ACTIVE</li>
 * <li>ANY → BANNED (admin action)</li>
 * <li>BANNED → ACTIVE (admin override only)</li>
 * </ul>
 * 
 * <p>
 * Business rule: A shop cannot be ACTIVE without both legalName and taxId.
 */
public class Shop extends AbstractAuditableEntity {

    /**
     * Shop status lifecycle states
     */
    public enum Status {
        PENDING, // Initial state after registration
        ACTIVE, // Verified and operational
        SUSPENDED, // Temporarily disabled
        BANNED // Permanently banned (terminal without admin override)
    }

    private UUID ownerId;
    private String legalName;
    private String taxId;
    private String slug;
    private Status status;
    private Address address;

    /**
     * Creates a new Shop in PENDING status.
     *
     * @param shopId  The unique shop identifier
     * @param ownerId The owner's user ID
     * @param slug    URL-safe shop identifier (unique, lowercase)
     * @param address The shop's physical address
     */
    public Shop(UUID shopId, UUID ownerId, String slug, Address address) {
        super(shopId);
        ValidationUtils.validateUUID(ownerId, "ownerId");
        ValidationUtils.validateSlug(slug, "slug");
        ValidationUtils.validateNotBlank(address, "address");

        this.ownerId = ownerId;
        this.slug = slug;
        this.address = address;
        this.status = Status.PENDING;
        this.legalName = null;
        this.taxId = null;
    }

    /**
     * Gets the owner's user ID.
     */
    public UUID getOwnerId() {
        return ownerId;
    }

    /**
     * Gets the legal business name.
     */
    public String getLegalName() {
        return legalName;
    }

    /**
     * Gets the tax identification number.
     */
    public String getTaxId() {
        return taxId;
    }

    /**
     * Gets the URL-safe slug.
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Gets the current status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the physical address.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Checks if the shop is verified (has legalName and taxId).
     */
    public boolean isVerified() {
        return legalName != null && !legalName.isBlank()
                && taxId != null && !taxId.isBlank();
    }

    /**
     * Checks if the shop is currently active.
     */
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    /**
     * Checks if the shop is banned.
     */
    public boolean isBanned() {
        return status == Status.BANNED;
    }

    /**
     * Sets the legal name and tax ID for verification.
     * This is required before the shop can be activated.
     *
     * @param legalName The legal business name (required)
     * @param taxId     The tax identification number (required)
     * @throws IllegalArgumentException if either parameter is null or blank
     */
    public void verify(String legalName, String taxId) {
        ValidationUtils.validateNotBlank(legalName, "legalName");
        ValidationUtils.validateNotBlank(taxId, "taxId");

        this.legalName = legalName.trim();
        this.taxId = taxId.trim();
        touch();
    }

    /**
     * Transitions the shop to ACTIVE status.
     * Requires verification (legalName + taxId) unless adminOverride is true.
     *
     * @param adminOverride If true, bypasses verification requirement (for admin
     *                      recovery)
     * @throws IllegalStateException if shop is not verified and adminOverride is
     *                               false
     */
    public void activate(boolean adminOverride) {
        if (!adminOverride && !isVerified()) {
            throw new IllegalStateException(
                    "Shop must be verified (legalName + taxId) before activation");
        }
        if (status == Status.BANNED) {
            throw new IllegalStateException("Banned shops require admin override to activate");
        }
        this.status = Status.ACTIVE;
        touch();
    }

    /**
     * Transitions the shop to SUSPENDED status.
     * A suspended shop can be reactivated.
     */
    public void suspend() {
        if (status == Status.PENDING) {
            throw new IllegalStateException("Cannot suspend a pending shop");
        }
        if (status == Status.BANNED) {
            throw new IllegalStateException("Shop is already banned");
        }
        this.status = Status.SUSPENDED;
        touch();
    }

    /**
     * Transitions the shop to BANNED status (admin action).
     * This is a terminal state - reactivation requires admin override.
     *
     * @param reason The reason for the ban (optional)
     */
    public void ban(String reason) {
        this.status = Status.BANNED;
        touch();
    }

    /**
     * Reactivates a banned shop (admin override only).
     * This is the only way to restore a banned shop.
     */
    public void reactivate() {
        if (status != Status.BANNED) {
            throw new IllegalStateException("Only banned shops can be reactivated");
        }
        this.status = Status.ACTIVE;
        touch();
    }

    /**
     * Updates the shop's physical address.
     *
     * @param newAddress The new address
     */
    public void updateAddress(Address newAddress) {
        ValidationUtils.validateNotBlank(newAddress, "newAddress");
        this.address = newAddress;
        touch();
    }

    /**
     * Validates state transition is legal.
     *
     * @param targetStatus  The target status
     * @param adminOverride Whether admin override is applied
     * @return true if transition is valid
     */
    public boolean canTransitionTo(Status targetStatus, boolean adminOverride) {
        if (this.status == targetStatus) {
            return false; // Already in this state
        }

        return switch (this.status) {
            case PENDING -> targetStatus == Status.ACTIVE;
            case ACTIVE -> targetStatus == Status.SUSPENDED || targetStatus == Status.BANNED;
            case SUSPENDED -> targetStatus == Status.ACTIVE || targetStatus == Status.BANNED;
            case BANNED -> targetStatus == Status.ACTIVE && adminOverride;
        };
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + getId() +
                ", slug='" + slug + '\'' +
                ", status=" + status +
                ", verified=" + isVerified() +
                '}';
    }
}
