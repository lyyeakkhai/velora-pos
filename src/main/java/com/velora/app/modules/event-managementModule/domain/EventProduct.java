package com.velora.app.core.domain.inventoryeventmanagement;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Junction record attaching a product to a discount event.
 *
 * Extends AbstractAuditableEntity to inherit UUID-based identity,
 * equals/hashCode, and createdAt/updatedAt audit timestamps.
 */
public class EventProduct extends AbstractAuditableEntity {

    private Integer sortOrder;
    private EventProductStatus status;
    private LocalDateTime deletedAt;
    private UUID shopId;
    private UUID productId;
    private UUID categoryId;
    private UUID eventId;

    public EventProduct(UUID shopId, UUID productId, UUID categoryId, UUID eventId, Integer sortOrder,
            EventProductStatus initialStatus) {
        super(UUID.randomUUID());
        setShopId(shopId);
        setProductId(productId);
        setCategoryId(categoryId);
        setEventId(eventId);
        setSortOrder(sortOrder);
        setStatus(initialStatus);
        this.deletedAt = null;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public EventProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public UUID getShopId() {
        return shopId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void activate() {
        if (status == EventProductStatus.ENDED) {
            throw new IllegalStateException("ENDED is terminal");
        }
        if (status != EventProductStatus.SCHEDULED) {
            throw new IllegalStateException("Illegal transition from " + status);
        }
        setStatus(EventProductStatus.ACTIVE);
        touch();
    }

    public void end() {
        if (status == EventProductStatus.ENDED) {
            return;
        }
        if (status == EventProductStatus.SCHEDULED || status == EventProductStatus.ACTIVE) {
            setStatus(EventProductStatus.ENDED);
            touch();
            return;
        }
        throw new IllegalStateException("Illegal transition from " + status);
    }

    public void softDelete() {
        if (deletedAt != null) {
            return;
        }
        this.deletedAt = LocalDateTime.now();
        touch();
    }

    public void updateSortOrder(int sortOrder) {
        ValidationUtils.validateNonNegativeInteger(sortOrder, "sortOrder");
        this.sortOrder = sortOrder;
        touch();
    }

    private void setSortOrder(Integer sortOrder) {
        ValidationUtils.validateNonNegativeInteger(sortOrder, "sortOrder");
        this.sortOrder = sortOrder;
    }

    private void setStatus(EventProductStatus status) {
        ValidationUtils.validateNotBlank(status, "status");
        this.status = status;
    }

    private void setShopId(UUID shopId) {
        ValidationUtils.validateUUID(shopId, "shopId");
        this.shopId = shopId;
    }

    private void setProductId(UUID productId) {
        ValidationUtils.validateUUID(productId, "productId");
        this.productId = productId;
    }

    private void setCategoryId(UUID categoryId) {
        ValidationUtils.validateUUID(categoryId, "categoryId");
        this.categoryId = categoryId;
    }

    private void setEventId(UUID eventId) {
        ValidationUtils.validateUUID(eventId, "eventId");
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "EventProduct{id=" + getId() +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                '}';
    }
}
