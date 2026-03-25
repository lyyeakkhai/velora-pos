package com.velora.app.modules.inventory_managementModule.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.inventory.domain.Category;
import com.velora.app.modules.inventory.domain.CategoryService;
import com.velora.app.modules.inventory.domain.CategoryStore;
import com.velora.app.modules.inventory.domain.DiscountService;
import com.velora.app.modules.inventory.domain.DiscountType;
import com.velora.app.modules.event_managementModule.domain.EventProduct;
import com.velora.app.modules.event_managementModule.domain.EventProductStatus;
import com.velora.app.modules.event_managementModule.domain.EventProductStore;
import com.velora.app.modules.event_managementModule.domain.EventType;
import com.velora.app.modules.event_managementModule.domain.EventTypeStore;
import com.velora.app.modules.inventory.domain.Product;
import com.velora.app.modules.inventory.domain.ProductService;
import com.velora.app.modules.inventory.domain.ProductStore;
import com.velora.app.modules.inventory.domain.ProductVariant;
import com.velora.app.modules.inventory.domain.ProductVariantStore;
import com.velora.app.modules.sale_managementModule.domain.TransactionRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for product, variant, category, and event
 * management.
 *
 * <p>
 * Extends {@link AbstractDomainService} to reuse {@code requireRole} and
 * {@code requireNotNull} guard methods. Delegates domain logic to
 * {@link ProductService}, {@link DiscountService}, and {@link CategoryService}.
 *
 * <p>
 * Requirements: 16.5
 */
public class InventoryManagementService extends AbstractDomainService implements IInventoryManagementService {

    private final ProductService productService;
    private final DiscountService discountService;
    private final CategoryService categoryService;

    public InventoryManagementService(
            TransactionRunner transactionRunner,
            CategoryStore categoryStore,
            ProductStore productStore,
            ProductVariantStore productVariantStore,
            EventTypeStore eventTypeStore,
            EventProductStore eventProductStore) {
        this.productService = new ProductService(transactionRunner, categoryStore, productStore, productVariantStore);
        this.discountService = new DiscountService(eventTypeStore, eventProductStore);
        this.categoryService = new CategoryService(categoryStore);
    }

    /**
     * Atomically creates a product and its initial variants.
     *
     * @param actorRole  the role of the actor performing the action
     * @param shopId     the UUID of the shop
     * @param categoryId the UUID of the category
     * @param name       the product name
     * @param slug       the unique URL slug
     * @param basePrice  the base selling price
     * @param costPrice  the cost price
     * @param variants   the initial variant drafts
     * @return the persisted {@link Product}
     */
    @Override
    public Product createProductAtomic(Role.RoleName actorRole, UUID shopId, UUID categoryId, String name, String slug,
            BigDecimal basePrice, BigDecimal costPrice, List<ProductService.VariantDraft> variants) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(categoryId, "categoryId");
        requireNotNull(name, "name");
        requireNotNull(slug, "slug");
        requireNotNull(basePrice, "basePrice");
        requireNotNull(costPrice, "costPrice");
        requireNotNull(variants, "variants");

        return productService.createProductAtomic(actorRole, shopId, categoryId, name, slug, basePrice, costPrice,
                variants);
    }

    /**
     * Updates an existing product's fields.
     *
     * @param actorRole  the role of the actor performing the action
     * @param product    the product to update
     * @param name       the new name (nullable — no change if null)
     * @param slug       the new slug (nullable — no change if null)
     * @param basePrice  the new base price (nullable — no change if null)
     * @param costPrice  the new cost price (nullable — no change if null)
     * @param categoryId the new category UUID (nullable — no change if null)
     * @return the updated {@link Product}
     */
    @Override
    public Product updateProduct(Role.RoleName actorRole, Product product, String name, String slug,
            BigDecimal basePrice, BigDecimal costPrice, UUID categoryId) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(product, "product");

        return productService.updateProduct(actorRole, product, name, slug, basePrice, costPrice, categoryId);
    }

    /**
     * Bulk-inserts additional variants for an existing product.
     *
     * @param actorRole the role of the actor performing the action
     * @param product   the product to add variants to
     * @param drafts    the variant drafts to insert
     * @return the list of persisted {@link ProductVariant} instances
     */
    @Override
    public List<ProductVariant> bulkInsertVariants(Role.RoleName actorRole, Product product,
            List<ProductService.VariantDraft> drafts) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(product, "product");
        requireNotNull(drafts, "drafts");

        return productService.bulkInsertVariants(actorRole, product, drafts);
    }

    /**
     * Creates a new discount event for a shop.
     *
     * @param actorRole     the role of the actor performing the action
     * @param shopId        the UUID of the shop
     * @param name          the event name
     * @param discountValue the discount value
     * @param discountType  the type of discount (PERCENTAGE or FIXED)
     * @param available     whether the event is available
     * @param startDate     the event start date
     * @param endDate       the event end date
     * @param minAmount     the minimum spend required
     * @param usageLimit    the maximum number of uses
     * @return the persisted {@link EventType}
     */
    @Override
    public EventType createEvent(Role.RoleName actorRole, UUID shopId, String name, BigDecimal discountValue,
            DiscountType discountType, boolean available, LocalDateTime startDate, LocalDateTime endDate,
            BigDecimal minAmount, Integer usageLimit) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(name, "name");
        requireNotNull(discountValue, "discountValue");
        requireNotNull(discountType, "discountType");
        requireNotNull(startDate, "startDate");
        requireNotNull(endDate, "endDate");

        return discountService.createEvent(actorRole, shopId, name, discountValue, discountType, available, startDate,
                endDate, minAmount, usageLimit);
    }

    /**
     * Attaches a product to a discount event.
     *
     * @param actorRole     the role of the actor performing the action
     * @param event         the event to attach the product to
     * @param product       the product to attach
     * @param sortOrder     the display sort order
     * @param initialStatus the initial status of the event-product link
     * @return the persisted {@link EventProduct}
     */
    @Override
    public EventProduct attachProductToEvent(Role.RoleName actorRole, EventType event, Product product, int sortOrder,
            EventProductStatus initialStatus) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(event, "event");
        requireNotNull(product, "product");
        requireNotNull(initialStatus, "initialStatus");

        return discountService.attachProductToEvent(actorRole, event, product, sortOrder, initialStatus);
    }

    /**
     * Calculates the final price after applying the event discount.
     *
     * @param salePrice the original sale price
     * @param event     the discount event to apply
     * @return the final price after discount
     */
    @Override
    public BigDecimal calculateFinalPrice(BigDecimal salePrice, EventType event) {
        requireNotNull(salePrice, "salePrice");
        requireNotNull(event, "event");

        return discountService.calculateFinalPrice(salePrice, event);
    }

    /**
     * Creates a new product category for a shop.
     *
     * @param actorRole the role of the actor performing the action
     * @param shopId    the UUID of the shop
     * @param name      the category name
     * @return the persisted {@link Category}
     */
    @Override
    public Category createCategory(Role.RoleName actorRole, UUID shopId, String name) {
        requireNotNull(actorRole, "actorRole");
        requireNotNull(shopId, "shopId");
        requireNotNull(name, "name");

        return categoryService.createCategory(actorRole, shopId, name);
    }
}
