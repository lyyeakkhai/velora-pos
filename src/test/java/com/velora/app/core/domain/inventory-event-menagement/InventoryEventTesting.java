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

import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.salemanagement.TransactionRunner;

public class InventoryEventTesting {

    @Test
    public void product_validCreation_normalizesAndEnforcesProfit() {
        Product product = new Product(UUID.randomUUID(), UUID.randomUUID(), "  Green Tea  ", "green-tea",
                new BigDecimal("10"), new BigDecimal("2"));
        assertNotNull(product.getId());
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

    @Test(expected = com.velora.app.common.DomainException.class)
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

        Product created = service.createProductAtomic(Role.RoleName.MANAGER, shopId, category.getId(), "Tea",
                "tea", new BigDecimal("10"), new BigDecimal("2"), drafts);

        assertNotNull(created.getId());
        assertEquals(2, variantStore.saved.size());
        assertEquals(created.getId(), variantStore.saved.get(0).getProductId());
    }

    @Test(expected = DomainException.class)
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
            byId.put(category.getId(), category);
            return category;
        }

        @Override
        public Optional<Category> findById(UUID categoryId) {
            return Optional.ofNullable(byId.get(categoryId));
        }

        @Override
        public List<Category> findByShopId(UUID shopId) {
            List<Category> result = new ArrayList<>();
            for (Category c : byId.values()) {
                if (c.getShopId().equals(shopId)) result.add(c);
            }
            return result;
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
            byId.put(product.getId(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(UUID productId) {
            return Optional.ofNullable(byId.get(productId));
        }

        @Override
        public List<Product> findByShopId(UUID shopId) {
            List<Product> result = new ArrayList<>();
            for (Product p : byId.values()) {
                if (p.getShopId().equals(shopId)) result.add(p);
            }
            return result;
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
        public ProductVariant save(ProductVariant variant) {
            byId.put(variant.getId(), variant);
            bySku.put(variant.getSku(), variant.getId());
            saved.add(variant);
            return variant;
        }

        @Override
        public List<ProductVariant> saveAll(List<ProductVariant> variants) {
            for (ProductVariant v : variants) {
                byId.put(v.getId(), v);
                bySku.put(v.getSku(), v.getId());
                saved.add(v);
            }
            return variants;
        }

        @Override
        public Optional<ProductVariant> findById(UUID variantId) {
            return Optional.ofNullable(byId.get(variantId));
        }

        @Override
        public List<ProductVariant> findByProductId(UUID productId) {
            List<ProductVariant> result = new ArrayList<>();
            for (ProductVariant v : byId.values()) {
                if (v.getProductId().equals(productId)) result.add(v);
            }
            return result;
        }

        @Override
        public Optional<ProductVariant> findBySku(String sku) {
            UUID id = bySku.get(sku);
            return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
        }
    }

    private static class InMemoryEventTypeStore implements EventTypeStore {
        private final Map<UUID, EventType> byId = new HashMap<>();

        @Override
        public EventType save(EventType eventType) {
            byId.put(eventType.getId(), eventType);
            return eventType;
        }

        @Override
        public Optional<EventType> findById(UUID eventId) {
            return Optional.ofNullable(byId.get(eventId));
        }

        @Override
        public List<EventType> findByShopId(UUID shopId) {
            List<EventType> result = new ArrayList<>();
            for (EventType e : byId.values()) {
                if (e.getShopId().equals(shopId)) result.add(e);
            }
            return result;
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
            byId.put(eventProduct.getId(), eventProduct);
            byEventProduct.put(eventProduct.getEventId() + ":" + eventProduct.getProductId(), Boolean.TRUE);
            return eventProduct;
        }

        @Override
        public Optional<EventProduct> findById(UUID eventProductId) {
            return Optional.ofNullable(byId.get(eventProductId));
        }

        @Override
        public List<EventProduct> findByEventId(UUID eventId) {
            List<EventProduct> result = new ArrayList<>();
            for (EventProduct ep : byId.values()) {
                if (ep.getEventId().equals(eventId)) result.add(ep);
            }
            return result;
        }

        @Override
        public List<EventProduct> findByProductId(UUID productId) {
            List<EventProduct> result = new ArrayList<>();
            for (EventProduct ep : byId.values()) {
                if (ep.getProductId().equals(productId)) result.add(ep);
            }
            return result;
        }
    }
}
