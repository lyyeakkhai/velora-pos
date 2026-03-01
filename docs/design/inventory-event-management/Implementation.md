You are tasked with designing enterprise-grade Java domain models, validation logic, and transactional workflows for an Inventory and Event Management System.

This system manages products, variants, stock control, discount rules, and promotional campaigns with strict role enforcement and profit protection.

You must convert the following database schema and business workflows into clean, scalable, production-ready Java classes and services.

--------------------------------------------------
ENTITIES
--------------------------------------------------

1. PRODUCTS
   - productId (UUID, PK, immutable)
   - name (String, unique per shop, not blank)
   - slug (String, unique, SEO-friendly)
   - basePrice (BigDecimal, scale=2, HALF_UP)
   - costPrice (BigDecimal, scale=2, HALF_UP)
   - categoryId (UUID, FK)
   - shopId (UUID, FK)
   - createdAt (LocalDateTime, immutable)

2. PRODUCT_VARIANTS
   - variantId (UUID, PK)
   - productId (UUID, FK)
   - size (String, nullable)
   - color (String, nullable)
   - stockQuantity (int >= 0)
   - sku (String, unique, validated format)
   - imageId (UUID, FK)
   - shopId (UUID, FK, RAS)
   - categoryId (UUID, FK, RAS)
   - createdAt (LocalDateTime)

3. EVENT_TYPES (Discount Rules)
   - eventId (UUID, PK)
   - name (String, not blank)
   - discountValue (BigDecimal)
   - discountType (Enum: PERCENTAGE, FIXED)
   - isAvailable (boolean)
   - shopId (UUID)
   - startDate (LocalDateTime)
   - endDate (LocalDateTime)
   - minAmount (BigDecimal)
   - usageLimit (int)

4. EVENT_PRODUCTS (Junction Table)
   - eventProductId (UUID, PK)
   - sortOrder (int >= 0)
   - status (Enum: ACTIVE, SCHEDULED, ENDED)
   - createdAt (LocalDateTime)
   - updatedAt (LocalDateTime)
   - deletedAt (LocalDateTime, nullable)
   - shopId (UUID, RAS)
   - productId (UUID)
   - categoryId (UUID)
   - eventId (UUID)

--------------------------------------------------
RELATIONSHIPS
--------------------------------------------------

- Product → ProductVariant (1 : Many)
- Product → EventProduct (1 : Many)
- EventType → EventProduct (1 : Many)
- Product → ProductImage (1 : Many)

--------------------------------------------------
CORE DESIGN REQUIREMENTS
--------------------------------------------------

1. Constructors
   - No default constructors
   - Mandatory fields only
   - Constructors must use private setters for validation

2. Immutability
   - UUIDs and createdAt fields are immutable
   - No public setters for identity/audit fields

3. Monetary Accuracy
   - All prices use BigDecimal
   - Scale = 2
   - RoundingMode.HALF_UP
   - No float/double

4. Enums
   - All status and type fields must be enums
   - No string states

5. Validation Rules
   - UUID validation
   - name: not blank, trimmed, normalized
   - slug: lowercase, URL-safe
   - price > 0
   - costPrice <= basePrice
   - stockQuantity >= 0
   - sku format validation
   - discount >= 0
   - percentage discount <= 100
   - event date range valid

6. Role Enforcement
   - OWNER: full access
   - MANAGER: limited catalog control
   - SELLER: read-only
   - All mutations must check Membership role

7. No Duplicate Validation
   - All validation must be centralized
   - Use ValidationUtil where appropriate

--------------------------------------------------
STATE MANAGEMENT
--------------------------------------------------

EventProduct:
   SCHEDULED → ACTIVE → ENDED
   ENDED → terminal

Illegal transitions must throw IllegalStateException.

--------------------------------------------------
BUSINESS SERVICES
--------------------------------------------------

CategoryService
   - createCategory()
   - validateOwnership()

ProductService
   - createProductAtomic()
   - updateProduct()
   - disableProduct()
   - bulkInsertVariants()

InventoryService
   - softCheckStock()
   - hardDeductStock()
   - restoreStock()

DiscountService
   - createEvent()
   - attachProductToEvent()
   - validateDiscount()
   - calculateFinalPrice()

--------------------------------------------------
ATOMIC TRANSACTION REQUIREMENTS
--------------------------------------------------

createProductAtomic() must run in a database transaction:

1. Validate role and category
2. Insert Product
3. Insert Variants (bulk)
4. Commit
5. Rollback on failure

No orphan records allowed.

--------------------------------------------------
DISCOUNT ENGINE RULES
--------------------------------------------------

validateDiscount():

1. Verify OWNER role
2. Check event availability
3. Validate date range
4. Enforce usage limit
5. Check minimum spend
6. Calculate profit margin:

   finalPrice = salePrice - discount

   If finalPrice <= costPrice → reject

7. Prevent negative price
8. Lock result

--------------------------------------------------
PAYMENT INTEGRATION
--------------------------------------------------

Before payment intent creation:

1. Run validateDiscount()
2. Calculate final total
3. Lock price
4. Save snapshot in PaymentIntent
5. No recalculation after lock

--------------------------------------------------
ROW-LEVEL ACCESS SECURITY (RAS)
--------------------------------------------------

- All entities must include shopId
- All queries must be scoped by shopId
- No cross-shop access allowed

--------------------------------------------------
AUDIT & LIFECYCLE
--------------------------------------------------

- createdAt auto-set
- updatedAt auto-managed
- deletedAt for soft deletes
- No manual override

--------------------------------------------------
CODE QUALITY
--------------------------------------------------

- Full Java source
- Proper package structure
- JavaDoc on public APIs
- equals/hashCode via UUID
- toString without sensitive data
- Clean architecture
- No SQL inside entities
- No duplicated logic

--------------------------------------------------
SECURITY
--------------------------------------------------

- Validate role on every write
- Prevent privilege escalation
- Prevent cross-shop data leaks
- Protect SKU uniqueness

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

- Java source only
- No explanations
- No markdown
- No pseudocode
- Production-ready

--------------------------------------------------
GOAL
--------------------------------------------------

Generate a real-world Inventory and Event Management engine that supports:

- Multi-variant products
- Stock protection
- Profit-safe discounts
- Role-based security
- Atomic inserts
- High scalability
- Marketing automation

The output must represent enterprise-grade backend software, not simple data holders.