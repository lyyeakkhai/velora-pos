# Feature 4: Inventory & Event Management

## System Overview

This system manages products, variants, stock control, discount rules, and promotional campaigns with strict role enforcement and profit protection. It implements atomic product creation and row-level access security.

## Domain Model

### Entities

#### Product
Catalog item with pricing and categorization.

| Field | Type | Validation |
|-------|------|------------|
| productId | UUID | Immutable |
| shopId | UUID | FK, row-level security |
| name | String | Not blank, unique per shop |
| slug | String | Unique, lowercase, SEO-friendly |
| basePrice | BigDecimal | > 0, scale=2, HALF_UP |
| costPrice | BigDecimal | <= basePrice, scale=2, HALF_UP |
| categoryId | UUID | FK to Category |
| createdAt | LocalDateTime | Immutable |

#### ProductVariant
Specific product variation.

| Field | Type | Validation |
|-------|------|------------|
| variantId | UUID | PK |
| productId | UUID | FK |
| shopId | UUID | FK, RAS |
| categoryId | UUID | FK, RAS |
| size | String | Nullable |
| color | String | Nullable |
| stockQuantity | int | >= 0 |
| sku | String | Unique format: uppercase alphanumeric + hyphen/underscore, 3-32 chars |
| imageId | UUID | FK to ProductImage |
| createdAt | LocalDateTime | Managed |

#### Category
Product grouping.

| Field | Type | Validation |
|-------|------|------------|
| categoryId | UUID | PK |
| shopId | UUID | FK, RAS |
| name | String | Not blank |
| createdAt | LocalDateTime | Immutable |

#### EventType
Discount rule definition.

| Field | Type | Validation |
|-------|------|------------|
| eventId | UUID | PK |
| shopId | UUID | FK |
| name | String | Not blank |
| discountValue | BigDecimal | >= 0 |
| discountType | DiscountType | PERCENTAGE/FIXED |
| isAvailable | boolean | Default true |
| startDate | LocalDateTime | Not null |
| endDate | LocalDateTime | > startDate |
| minAmount | BigDecimal | >= 0, default 0 |
| usageLimit | int | >= 0, nullable |

#### EventProduct
Junction table linking products to events.

| Field | Type | Validation |
|-------|------|------------|
| eventProductId | UUID | PK |
| shopId | UUID | FK, RAS |
| productId | UUID | FK |
| categoryId | UUID | FK |
| eventId | UUID | FK |
| sortOrder | int | >= 0 |
| status | EventStatus | SCHEDULED/ACTIVE/ENDED |
| createdAt | LocalDateTime | Immutable |
| updatedAt | LocalDateTime | Managed |
| deletedAt | LocalDateTime | Nullable (soft delete) |

### Value Objects

| Value Object | Description |
|--------------|------------|
| DiscountType | PERCENTAGE, FIXED |
| EventStatus | SCHEDULED, ACTIVE, ENDED |

## Entity Relationships

```
Product (1:N) ProductVariant
Product (1:N) EventProduct
EventType (1:N) EventProduct
Product (1:1) Category
Category (1:N) Product
```

## Business Rules

1. basePrice > 0
2. costPrice <= basePrice (profit protection)
3. stockQuantity >= 0
4. SKU format: uppercase alphanumeric + hyphen/underscore, 3-32 chars
5. PERCENTAGE discount <= 100
6. finalPrice > costPrice (must maintain profit margin)
7. Date range: startDate < endDate
8. All entities scoped by shopId (row-level security)

## State Transitions

```
EventProduct: SCHEDULED → ACTIVE → ENDED (terminal)
```

## Implementation Details

### Role Enforcement

| Role | Access Level |
|------|-------------|
| OWNER | Full access (create, update, delete) |
| MANAGER | Limited catalog control |
| SELLER | Read-only |

### Business Services

| Service | Methods |
|---------|---------|
| ProductService | createProductAtomic(), updateProduct(), disableProduct(), bulkInsertVariants() |
| CategoryService | createCategory(), validateOwnership() |
| InventoryService | softCheckStock(), hardDeductStock(), restoreStock() |
| DiscountService | createEvent(), attachProductToEvent(), validateDiscount(), calculateFinalPrice() |

### Atomic Transaction (createProductAtomic)

1. Validate role and category ownership
2. Insert Product
3. Insert Variants (bulk)
4. Commit
5. Rollback on failure

No orphan records allowed.

### Discount Engine Rules

1. Verify OWNER role
2. Check event availability
3. Validate date range
4. Enforce usage limit
5. Check minimum spend
6. Calculate profit margin: finalPrice = salePrice - discount
   - If finalPrice <= costPrice → reject
7. Prevent negative price

## OOP Best Practices Applied

1. **Constructor Validation** - All validation in constructors/setters
2. **Immutability** - IDs and created timestamps immutable
3. **No Default Constructors** - Required fields mandatory
4. **Enums** - All status/type fields use enums
5. **Row-Level Access Security** - All entities scoped by shopId
6. **BigDecimal** - All monetary values use BigDecimal scale=2
7. **Strategy Pattern** - Discount strategies (Percentage, Fixed)
8. **Exception Handling** - Uses DomainException, IllegalStateException, IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `inventory-managementModule/domain/Product.java` | Catalog item entity |
| `inventory-managementModule/domain/ProductVariant.java` | Product variation entity |
| `inventory-managementModule/domain/Category.java` | Product grouping entity |
| `inventory-managementModule/domain/DiscountStrategy.java` | Discount strategy interface |
| `inventory-managementModule/domain/PercentageDiscountStrategy.java` | Percentage discount implementation |
| `inventory-managementModule/domain/FixedDiscountStrategy.java` | Fixed discount implementation |
| `inventory-managementModule/domain/DiscountService.java` | Discount engine service |
| `inventory-managementModule/domain/ProductService.java` | Product management service |
| `inventory-managementModule/domain/CategoryService.java` | Category management service |
| `inventory-managementModule/domain/RolePolicy.java` | Role-based access control |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses three types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `DomainException` | Business rule violations (role, discount rules, profit protection) |
| `IllegalStateException` | State transitions (stock, product status, uniqueness) |
| `IllegalArgumentException` | Parameter validation (null, blank, format) |

```
RuntimeException
└── DomainException (common)
    ├── IllegalStateException (state/uniqueness)
    │   ├── "Product name must be unique per shop"
    │   ├── "Product slug must be unique"
    │   ├── "SKU must be unique"
    │   ├── "Category name must be unique per shop"
    │   ├── "Product is disabled"
    │   ├── "stockQuantity cannot go negative"
    │   ├── "Cross-shop attachment rejected"
    │   └── "Product already attached to event"
    └── IllegalArgumentException (validation)
        ├── "name cannot be blank"
        ├── "variants cannot be empty"
        └── "drafts cannot be empty"

DomainException (business rules)
    ├── "SUPER_ADMIN role required"
    ├── "OWNER role required"
    ├── "Write access denied for SELLER"
    ├── "Event is not available"
    ├── "Event is outside active date range"
    ├── "Minimum spend not met"
    ├── "Usage limit exceeded"
    ├── "Final price cannot be negative"
    ├── "Discount would violate profit protection"
    ├── "Percentage discount must be between 0 and 100"
    └── "Fixed discount value must not be negative"
```

## Known Limitations

1. **No product images** - Image management not implemented

## Dependencies

- **Depends on**: Feature 1 (Authentication), Feature 3 (Store Management)
- **Used by**: Feature 5 (Sale Management), Feature 9 (Reporting & Analytics)

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity implementation with strategy pattern and RBAC  
**Last Updated**: 2026-04-07