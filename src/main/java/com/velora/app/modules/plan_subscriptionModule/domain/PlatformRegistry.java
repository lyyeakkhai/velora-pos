package com.velora.app.modules.plan_subscriptionModule.domain;

import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Master switch for platform access.
 */
public class PlatformRegistry {

    private UUID registryId;
    private UUID ownerId;
    private TargetType targetType;
    private RegistryStatus status;
    private String banReason;
    private UUID transactionId;

    /**
     * Creates a registry entry with mandatory fields. registryId is generated.
     */
    public PlatformRegistry(UUID ownerId, TargetType targetType, RegistryStatus status) {
        setRegistryId(UUID.randomUUID());
        setOwnerId(ownerId);
        setTargetType(targetType);
        setStatus(status);
        setBanReason(null);
        setTransactionId(null);
    }

    public UUID getRegistryId() {
        return registryId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public RegistryStatus getStatus() {
        return status;
    }

    public String getBanReason() {
        return banReason;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * Activates the registry.
     *
     * @throws IllegalStateException if the registry is banned or already active
     */
    public void activate() {
        if (status == RegistryStatus.BANNED) {
            throw new IllegalStateException("Registry is banned and cannot be activated");
        }
        if (status == RegistryStatus.ACTIVE) {
            throw new IllegalStateException("Registry is already active");
        }
        setStatus(RegistryStatus.ACTIVE);
        setBanReason(null);
    }

    /**
     * Bans the registry permanently.
     */
    public void ban(String reason) {
        if (status == RegistryStatus.BANNED) {
            throw new IllegalStateException("Registry is already banned");
        }
        ValidationUtils.validateNotBlank(reason, "banReason");
        setStatus(RegistryStatus.BANNED);
        setBanReason(reason);
    }

    /**
     * Deactivates the registry.
     */
    public void deactivate() {
        if (status == RegistryStatus.BANNED) {
            throw new IllegalStateException("Registry is banned and cannot be deactivated");
        }
        if (status == RegistryStatus.INACTIVE) {
            throw new IllegalStateException("Registry is already inactive");
        }
        setStatus(RegistryStatus.INACTIVE);
    }

    /**
     * Verifies that platform access is allowed.
     *
     * @throws IllegalStateException when access is not allowed
     */
    public void verifyAccess() {
        if (status == RegistryStatus.ACTIVE) {
            return;
        }
        if (status == RegistryStatus.BANNED) {
            throw new IllegalStateException("Access denied: banned. " + (banReason == null ? "" : banReason));
        }
        throw new IllegalStateException("Access denied: registry status is " + status);
    }

    private void setRegistryId(UUID registryId) {
        ValidationUtils.validateUUID(registryId, "registryId");
        this.registryId = registryId;
    }

    private void setOwnerId(UUID ownerId) {
        ValidationUtils.validateUUID(ownerId, "ownerId");
        this.ownerId = ownerId;
    }

    private void setTargetType(TargetType targetType) {
        ValidationUtils.validateNotBlank(targetType, "targetType");
        this.targetType = targetType;
    }

    private void setStatus(RegistryStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setBanReason(String banReason) {
        if (banReason == null) {
            this.banReason = null;
            return;
        }
        ValidationUtils.validateNotBlank(banReason, "banReason");
        this.banReason = banReason;
    }

    /**
     * Optional payment transaction reference.
     */
    public void setTransactionId(UUID transactionId) {
        if (transactionId == null) {
            this.transactionId = null;
            return;
        }
        ValidationUtils.validateUUID(transactionId, "transactionId");
        this.transactionId = transactionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlatformRegistry)) {
            return false;
        }
        PlatformRegistry that = (PlatformRegistry) o;
        return Objects.equals(registryId, that.registryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryId);
    }

    @Override
    public String toString() {
        return "PlatformRegistry{" +
                "registryId=" + registryId +
                ", ownerId=" + ownerId +
                ", targetType=" + targetType +
                ", status=" + status +
                '}';
    }
}
