package com.velora.app.core.service;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.inventoryeventmanagement.Category;
import com.velora.app.core.domain.inventoryeventmanagement.DiscountType;
import com.velora.app.core.domain.inventoryeventmanagement.EventProduct;
import com.velora.app.core.domain.inventoryeventmanagement.EventProductStatus;
import com.velora.app.core.domain.inventoryeventmanagement.EventType;
import com.velora.app.core.domain.inventoryeventmanagement.Product;
import com.velora.app.core.domain.inventoryeventmanagement.ProductService;
import com.velora.app.core.domain.inventoryeventmanagement.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for product, variant, category, and event management.
 *
 * <p>Requirement: 16.1, 16.5
 */
public interface IInventoryManagementService {

    /**
     * Atomically creates a product and its initial variants.
     */
    Product createProductAtomic(Role.RoleName actorRole, UUID shopId, UUID categoryId, String name, String slug,
            BigDecimal basePrice, BigDecimal costPrice, List<ProductService.VariantDraft> variants);

    /**
     * Updates an existing product's fields.
     */
    Product updateProduct(Role.RoleName actorRole, Product product, String name, String slug,
            BigDecimal basePrice, BigDecimal costPrice, UUID categoryId);

    /**
     * Bulk-inserts additional variants for an existing product.
     */
    List<ProductVariant> bulkInsertVariants(Role.RoleName actorRole, Product product,
            List<ProductService.VariantDraft> drafts);

    /**
     * Creates a new discount event for a shop.
     */
    EventType createEvent(Role.RoleName actorRole, UUID shopId, String name, BigDecimal discountValue,
            DiscountType discountType, boolean available, LocalDateTime startDate, LocalDateTime endDate,
            BigDecimal minAmount, Integer usageLimit);

    /**
     * Attaches a product to a discount event.
     */
    EventProduct attachProductToEvent(Role.RoleName actorRole, EventType event, Product product, int sortOrder,
            EventProductStatus initialStatus);

    /**
     * Calculates the final price after applying the event discount.
     */
    BigDecimal calculateFinalPrice(BigDecimal salePrice, EventType event);

    /**
     * Creates a new product category for a shop.
     */
    Category createCategory(Role.RoleName actorRole, UUID shopId, String name);
}
