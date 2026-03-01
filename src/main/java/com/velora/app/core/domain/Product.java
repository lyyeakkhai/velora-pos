package com.velora.app.core.domain;
import com.velora.app.common.DomainException;

public class Product {
    private final String id;
    private String name;
    private double price;

    public Product(String id, String name, double price) {
        if (price < 0)
            throw new DomainException("Price cannot be negative");
        this.id = id;
        this.name = name;
        this.price = price;
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
