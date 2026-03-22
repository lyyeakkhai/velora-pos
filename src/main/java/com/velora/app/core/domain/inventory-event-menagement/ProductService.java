package com.velora.app.core.domain.inventoryeventmanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.salemanagement.TransactionRunner;
import com.velora.app.core.utils.ValidationUtils;

/**
 * Product and variant orchestration service.
 */
public class ProductService {

    public record VariantDraft(String sku, Integer stockQuantity, UUID imageId, String size, String color) {
        public VariantDraft {
            ValidationUtils.validateNotBlank(sku, "sku");
            ValidationUtils.validateNonNegativeInteger(stockQuantity, "stockQuantity");
            ValidationUtils.validateUUID(imageId, "imageId");
        }
    }

    private final TransactionRunner transactionRunner;
    private final CategoryStore categoryStore;
    private final ProductStore productStore;
    private final ProductVariantStore variantStore;

    public ProductService(TransactionRunner transactionRunner, CategoryStore categoryStore, ProductStore productStore,
            ProductVariantStore variantStore) {
        this.transactionRunner = require(transactionRunner, "transactionRunner");
        this.categoryStore = require(categoryStore, "categoryStore");
        this.productStore = require(productStore, "productStore");
        this.variantStore = require(variantStore, "variantStore");
    }

    /**
     * Atomically creates a Product and its variants.
     */
    public Product createProductAtomic(Role.RoleName actorRole, UUID shopId, UUID categoryId, String name, String slug,
            java.math.BigDecimal basePrice, java.math.BigDecimal costPrice, List<VariantDraft> variants) {
        RolePolicy.requireCatalogWrite(actorRole);
        ValidationUtils.validateUUID(shopId, "shopId");
        ValidationUtils.validateUUID(categoryId, "categoryId");
        ValidationUtils.validateNotBlank(variants, "variants");
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("variants cannot be empty");
        }

        if (productStore.existsByShopIdAndName(shopId, name.trim())) {
            throw new IllegalStateException("Product name must be unique per shop");
        }
        if (productStore.existsBySlug(slug)) {
            throw new IllegalStateException("Product slug must be unique");
        }
        categoryStore.findById(categoryId).orElseThrow(() -> new IllegalStateException("Category not found"));

        final Product[] holder = new Product[1];
        transactionRunner.runInTransaction(() -> {
            Product product = productStore.save(new Product(shopId, categoryId, name, slug, basePrice, costPrice));
            List<ProductVariant> toSave = new ArrayList<>();
            for (VariantDraft draft : variants) {
                if (variantStore.existsBySku(draft.sku().trim())) {
                    throw new IllegalStateException("SKU must be unique");
                }
                toSave.add(new ProductVariant(product.getId(), shopId, categoryId, draft.sku(),
                        draft.stockQuantity(), draft.imageId(), draft.size(), draft.color()));
            }
            variantStore.saveAll(toSave);
            holder[0] = product;
        });

        return holder[0];
    }

    public Product updateProduct(Role.RoleName actorRole, Product product, String name, String slug,
            java.math.BigDecimal basePrice, java.math.BigDecimal costPrice, UUID categoryId) {
        RolePolicy.requireCatalogWrite(actorRole);
        ValidationUtils.validateNotBlank(product, "product");
        product.updateProduct(name, slug, basePrice, costPrice, categoryId);
        return productStore.save(product);
    }

    public Product disableProduct(Role.RoleName actorRole, Product product) {
        RolePolicy.requireOwner(actorRole);
        ValidationUtils.validateNotBlank(product, "product");
        product.disable();
        return productStore.save(product);
    }

    public List<ProductVariant> bulkInsertVariants(Role.RoleName actorRole, Product product,
            List<VariantDraft> drafts) {
        RolePolicy.requireCatalogWrite(actorRole);
        ValidationUtils.validateNotBlank(product, "product");
        ValidationUtils.validateNotBlank(drafts, "drafts");
        if (drafts.isEmpty()) {
            throw new IllegalArgumentException("drafts cannot be empty");
        }

        List<ProductVariant> variants = new ArrayList<>();
        for (VariantDraft draft : drafts) {
            if (variantStore.existsBySku(draft.sku().trim())) {
                throw new IllegalStateException("SKU must be unique");
            }
            variants.add(new ProductVariant(product.getId(), product.getShopId(), product.getCategoryId(),
                    draft.sku(), draft.stockQuantity(), draft.imageId(), draft.size(), draft.color()));
        }
        return variantStore.saveAll(variants);
    }

    private static <T> T require(T value, String fieldName) {
        ValidationUtils.validateNotBlank(value, fieldName);
        return value;
    }
}
