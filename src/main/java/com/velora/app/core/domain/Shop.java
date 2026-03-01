package com.velora.app.core.domain;

import com.velora.app.common.DomainException;

public class Shop {
    private final String id;
    private String name;

    public Shop(String id, String name) {
        if (id == null || id.isBlank())
            throw new DomainException("Shop id required");
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
