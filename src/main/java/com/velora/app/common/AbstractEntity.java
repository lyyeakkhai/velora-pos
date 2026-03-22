package com.velora.app.common;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractEntity {

    private final UUID id;

    protected AbstractEntity(UUID id) {
        if (id == null) throw new DomainException("id must not be null");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEntity)) return false;
        AbstractEntity that = (AbstractEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
