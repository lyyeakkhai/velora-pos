package com.velora.app.common;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class AbstractAuditableEntity extends AbstractEntity {

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected AbstractAuditableEntity(UUID id) {
        super(id);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
