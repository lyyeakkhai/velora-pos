package com.velora.app.core.service;

import com.velora.app.core.domain.Product;
import com.velora.app.core.domain.Shop;
import com.velora.app.core.repository.ShopRepository;

public class SaleService {
    private final ShopRepository shopRepo;

    public SaleService(ShopRepository shopRepo) {
        this.shopRepo = shopRepo;
    }

    public void processSale(Shop shop, Product product, int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");
        // Business rules would go here (discounts, inventory checks, etc.)
    }
}
