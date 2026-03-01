package com.velora.app.core.domain;
import com.velora.app.common.DomainException;

public class Product {
    private final String id;
    private String name;
    private double price;

    public Product(String id, String name, double price) {
        if (id == null || id.isBlank())
            throw new DomainException("Product id required");
        if (name == null || name.isBlank())
            throw new DomainException("Product name required");
        if (price < 0)
            throw new DomainException("Price cannot be negative");
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new DomainException("Product name required");
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0)
            throw new DomainException("Price cannot be negative");
        this.price = price;
    }
}
