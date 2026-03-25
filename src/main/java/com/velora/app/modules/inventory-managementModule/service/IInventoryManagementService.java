package com.velora.app.modules.inventory_managementModule.service;

import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.inventory.domain.Category;
import com.velora.app.modules.inventory.domain.DiscountType;
import com.velora.app.modules.event_managementModule.domain.EventProduct;
import com.velora.app.modules.event_managementModule.domain.EventProductStatus;
import com.velora.app.modules.event_managementModule.domain.EventType;
import com.velora.app.modules.inventory.domain.Product;
import com.velora.app.modules.inventory.domain.ProductService;
import com.velora.app.modules.inventory.domain.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for product, variant, category, and event
 * management.
 *
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5
 */
public interface IInventoryManagementService {

        Product createProductAtomic(Role.RoleName actorRole, UUID shopId, UUID categoryId, String name, String slug,
                        BigDecimal basePrice, BigDecimal costPrice, List<ProductService.VariantDraft> variants);

        Product updateProduct(Role.RoleName actorRole, Product product, String name, String slug,
                        BigDecimal basePrice, BigDecimal costPrice, UUID categoryId);

        List<ProductVariant> bulkInsertVariants(Role.RoleName actorRole, Product product,
                        List<ProductService.VariantDraft> drafts);

        EventType createEvent(Role.RoleName actorRole, UUID shopId, String name, BigDecimal discountValue,
                        DiscountType discountType, boolean available, LocalDateTime startDate, LocalDateTime endDate,
                        BigDecimal minAmount, Integer usageLimit);

        EventProduct attachProductToEvent(Role.RoleName actorRole, EventType event, Product product, int sortOrder,
                        EventProductStatus initialStatus);

        BigDecimal calculateFinalPrice(BigDecimal salePrice, EventType event);

        Category createCategory(Role.RoleName actorRole, UUID shopId, String name);
}
