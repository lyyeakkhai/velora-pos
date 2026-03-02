package com.velora.app.core.domain.inventoryeventmanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.salemanagement.TransactionRunner;

public class InventoryEventTesting {

    @Test
    public void product_validCreation_normalizesAndEnforcesProfit() {
        Product product = new Product(UUID.randomUUID(), UUID.randomUUID(), "  Green Tea  ", "green-tea",
                new BigDecimal("10"), new BigDecimal("2"));
        assertNotNull(product.getProductId());
        assertEquals("Green Tea", product.getName());
        assertEquals(new BigDecimal("10.00"), product.getBasePrice());
        assertEquals(new BigDecimal("2.00"), product.getCostPrice());
    }

    @Test(expected = IllegalArgumentException.class)
    public void product_costAboveBase_throws() {
        new Product(UUID.randomUUID(), UUID.randomUUID(), "Tea", "tea", new BigDecimal("5"), new BigDecimal("6"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void variant_invalidSku_throws() {
        new ProductVariant(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "bad sku", 0, UUID.randomUUID(),
                null, null);
    }

    @Test
    public void eventProduct_stateMachine() {
        EventProduct ep = new EventProduct(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                0,
                EventProductStatus.SCHEDULED);
        ep.activate();
        assertEquals(EventProductStatus.ACTIVE, ep.getStatus());

        ep.end();
        assertEquals(EventProductStatus.ENDED, ep.getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void eventProduct_cannotActivateFromActive_throws() {
        EventProduct ep = new EventProduct(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                0,
                EventProductStatus.ACTIVE);
        ep.activate();
    }

    @Test
    public void discountService_profitProtectionAndFinalPrice() {
        InMemoryEventTypeStore eventStore = new InMemoryEventTypeStore();
        InMemoryEventProductStore epStore = new InMemoryEventProductStore();
        DiscountService discountService = new DiscountService(eventStore, epStore);

        UUID shopId = UUID.randomUUID();
        EventType event = discountService.createEvent(Role.RoleName.OWNER, shopId, "Promo", new BigDecimal("10"),
                DiscountType.PERCENTAGE, true, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1),
                new BigDecimal("0"), 10);

        BigDecimal finalPrice = discountService.calculateFinalPrice(new BigDecimal("100"), event);
        assertEquals(new BigDecimal("90.00"), finalPrice);

        // OK: profit safe
        discountService.validateDiscount(Role.RoleName.OWNER, event, new BigDecimal("100"), new BigDecimal("20"), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void discountService_rejectsBelowCost() {
        InMemoryEventTypeStore eventStore = new InMemoryEventTypeStore();
        DiscountService discountService = new DiscountService(eventStore, new InMemoryEventProductStore());

        UUID shopId = UUID.randomUUID();
        EventType event = discountService.createEvent(Role.RoleName.OWNER, shopId, "Promo", new BigDecimal("90"),
                DiscountType.FIXED, true, LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1),
                new BigDecimal("0"), 10);

        // sale 100 - 90 = 10 which is <= cost 10 => reject
        discountService.validateDiscount(Role.RoleName.OWNER, event, new BigDecimal("100"), new BigDecimal("10"), 0);
    }

    @Test
    public void productService_createProductAtomic_createsProductAndVariants() {
        InMemoryCategoryStore categoryStore = new InMemoryCategoryStore();
        InMemoryProductStore productStore = new InMemoryProductStore();
        InMemoryVariantStore variantStore = new InMemoryVariantStore();
        TransactionRunner tx = work -> work.run();

        ProductService service = new ProductService(tx, categoryStore, productStore, variantStore);

        UUID shopId = UUID.randomUUID();
        Category category = categoryStore.save(new Category(shopId, "Beverages"));

        List<ProductService.VariantDraft> drafts = List.of(
                new ProductService.VariantDraft("SKU-ABC_1", 10, UUID.randomUUID(), "M", "Green"),
                new ProductService.VariantDraft("SKU-ABC_2", 0, UUID.randomUUID(), "L", "Green"));

        Product created = service.createProductAtomic(Role.RoleName.MANAGER, shopId, category.getCategoryId(), "Tea",
                "tea", new BigDecimal("10"), new BigDecimal("2"), drafts);

        assertNotNull(created.getProductId());
        assertEquals(2, variantStore.saved.size());
        assertEquals(created.getProductId(), variantStore.saved.get(0).getProductId());
    }

    @Test(expected = IllegalStateException.class)
    public void productService_sellerCannotWrite() {
        ProductService service = new ProductService(work -> work.run(), new InMemoryCategoryStore(),
                new InMemoryProductStore(), new InMemoryVariantStore());
        service.createProductAtomic(Role.RoleName.SELLER, UUID.randomUUID(), UUID.randomUUID(), "Tea", "tea",
                new BigDecimal("10"), new BigDecimal("2"),
                List.of(new ProductService.VariantDraft("SKU-1", 1, UUID.randomUUID(), null, null)));
    }

    private static class InMemoryCategoryStore implements CategoryStore {
        private final Map<UUID, Category> byId = new HashMap<>();

        @Override
        public boolean existsByShopIdAndName(UUID shopId, String name) {
            for (Category c : byId.values()) {
                if (c.getShopId().equals(shopId) && c.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Category save(Category category) {
            byId.put(category.getCategoryId(), category);
            return category;
        }

        @Override
        public Optional<Category> findById(UUID categoryId) {
            return Optional.ofNullable(byId.get(categoryId));
        }
    }

    private static class InMemoryProductStore implements ProductStore {
        private final Map<UUID, Product> byId = new HashMap<>();

        @Override
        public boolean existsByShopIdAndName(UUID shopId, String name) {
            for (Product p : byId.values()) {
                if (p.getShopId().equals(shopId) && p.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean existsBySlug(String slug) {
            for (Product p : byId.values()) {
                if (p.getSlug().equals(slug)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Product save(Product product) {
            byId.put(product.getProductId(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(UUID productId) {
            return Optional.ofNullable(byId.get(productId));
        }
    }

    private static class InMemoryVariantStore implements ProductVariantStore {
        private final Map<String, UUID> bySku = new HashMap<>();
        private final Map<UUID, ProductVariant> byId = new HashMap<>();
        private final List<ProductVariant> saved = new ArrayList<>();

        @Override
        public boolean existsBySku(String sku) {
            return bySku.containsKey(sku);
        }

        @Override
        public List<ProductVariant> saveAll(List<ProductVariant> variants) {
            for (ProductVariant v : variants) {
                byId.put(v.getVariantId(), v);
                bySku.put(v.getSku(), v.getVariantId());
                saved.add(v);
            }
            return variants;
        }

        @Override
        public Optional<ProductVariant> findById(UUID variantId) {
            return Optional.ofNullable(byId.get(variantId));
        }
    }

    private static class InMemoryEventTypeStore implements EventTypeStore {
        private final Map<UUID, EventType> byId = new HashMap<>();

        @Override
        public EventType save(EventType eventType) {
            byId.put(eventType.getEventId(), eventType);
            return eventType;
        }

        @Override
        public Optional<EventType> findById(UUID eventId) {
            return Optional.ofNullable(byId.get(eventId));
        }
    }

    private static class InMemoryEventProductStore implements EventProductStore {
        private final Map<UUID, EventProduct> byId = new HashMap<>();
        private final Map<String, Boolean> byEventProduct = new HashMap<>();

        @Override
        public boolean existsByEventIdAndProductId(UUID eventId, UUID productId) {
            return byEventProduct.containsKey(eventId + ":" + productId);
        }

        @Override
        public EventProduct save(EventProduct eventProduct) {
            byId.put(eventProduct.getEventProductId(), eventProduct);
            byEventProduct.put(eventProduct.getEventId() + ":" + eventProduct.getProductId(), Boolean.TRUE);
            return eventProduct;
        }

        @Override
        public Optional<EventProduct> findById(UUID eventProductId) {
            return Optional.ofNullable(byId.get(eventProductId));
        }
    }
}
