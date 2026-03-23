package com.velora.app.core.domain.storemanagement;

import com.velora.app.core.domain.auth.Role;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.*;

public class StoreManagementTesting {

    private static class InMemoryShopRepository implements ShopRepository {
        private final Map<UUID, Shop> byId = new HashMap<>();

        @Override
        public Shop save(Shop shop) {
            byId.put(shop.getId(), shop);
            return shop;
        }

        @Override
        public Optional<Shop> findById(UUID shopId) {
            return Optional.ofNullable(byId.get(shopId));
        }

        @Override
        public Optional<Shop> findBySlug(String slug) {
            return byId.values().stream().filter(s -> s.getSlug().equals(slug)).findFirst();
        }

        @Override
        public java.util.List<Shop> findByOwnerId(UUID ownerId) {
            java.util.List<Shop> result = new java.util.ArrayList<>();
            for (Shop shop : byId.values()) {
                if (shop.getOwnerId().equals(ownerId)) result.add(shop);
            }
            return result;
        }

        @Override
        public boolean existsBySlug(String slug) {
            for (Shop shop : byId.values()) {
                if (shop.getSlug().equals(slug)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Test
    public void registerShop_duplicateSlug_throws() {
        InMemoryShopRepository repo = new InMemoryShopRepository();
        StoreManagementService service = new StoreManagementService(repo);
        Address address = new Address("Street 1", "Phnom Penh", "Dangkao", "Phnom Penh");
        UUID ownerId = UUID.randomUUID();

        service.registerShop(ownerId, "my-shop", null, null, address);
        try {
            service.registerShop(ownerId, "my-shop", null, null, address);
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("unique"));
        }
    }

    @Test
    public void activateShop_requiresLegalAndTaxId() {
        InMemoryShopRepository repo = new InMemoryShopRepository();
        StoreManagementService service = new StoreManagementService(repo);
        Address address = new Address("Street 1", "Phnom Penh", "Dangkao", "Phnom Penh");
        Shop shop = service.registerShop(UUID.randomUUID(), "legal-shop", null, null, address);

        try {
            service.updateShopStatus(shop.getId(), ShopStatus.ACTIVE, Role.RoleName.OWNER);
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("active"));
        }

        shop.setLegalName("Velora Co., Ltd");
        shop.setTaxId("TAX-123456");
        repo.save(shop);

        Shop updated = service.updateShopStatus(shop.getId(), ShopStatus.ACTIVE, Role.RoleName.OWNER);
        assertEquals(ShopStatus.ACTIVE, updated.getStatus());
    }

    @Test
    public void bannedShop_cannotRevert_withoutSuperAdmin() {
        InMemoryShopRepository repo = new InMemoryShopRepository();
        StoreManagementService service = new StoreManagementService(repo);
        Address address = new Address("Street 1", "Phnom Penh", "Dangkao", "Phnom Penh");
        Shop shop = service.registerShop(UUID.randomUUID(), "ban-shop", "Legal", "TAX-111", address);

        service.updateShopStatus(shop.getId(), ShopStatus.BANNED, Role.RoleName.SUPER_ADMIN);

        try {
            service.updateShopStatus(shop.getId(), ShopStatus.ACTIVE, Role.RoleName.OWNER);
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("admin"));
        }

        Shop updated = service.updateShopStatus(shop.getId(), ShopStatus.SUSPENDED, Role.RoleName.SUPER_ADMIN);
        assertEquals(ShopStatus.SUSPENDED, updated.getStatus());
    }

    @Test
    public void address_invalidProvince_throws() {
        try {
            new Address("Street 1", "Phnom Penh", "Dangkao", "NotAProvince");
            fail("Expected exception");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("province"));
        }
    }

    @Test
    public void payoutCalculation_usesPlatformFeeRate() {
        InMemoryShopRepository repo = new InMemoryShopRepository();
        StoreManagementService service = new StoreManagementService(repo);
        Address address = new Address("Street 1", "Phnom Penh", "Dangkao", "Phnom Penh");
        Shop shop = service.registerShop(UUID.randomUUID(), "payout-shop", "Legal", "TAX-222", address);
        shop.getShopSettings().setPlatformFeeRatePercent(new BigDecimal("10.00"));
        repo.save(shop);

        BigDecimal payout = service.payoutCalculation(shop.getId(), new BigDecimal("100.00"));
        assertEquals(new BigDecimal("90.00"), payout);
    }
}
