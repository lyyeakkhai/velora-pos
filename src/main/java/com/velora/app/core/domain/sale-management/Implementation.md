You are tasked with designing enterprise-grade Java domain models and transaction workflows for an Order & Logistics Management System.

This system manages permanent sales records, inventory deduction, payment confirmation, receipt generation, and physical delivery lifecycle.

You must convert the following database schema and business workflow into clean, maintainable, production-ready Java classes with strict validation, atomic processing, and fraud prevention.

--------------------------------------------------
ENTITIES
--------------------------------------------------

1. ORDERS
   - orderId (UUID, PK, immutable)
   - status (Enum: PENDING, PAID, CANCELLED)
   - totalPrice (BigDecimal, scale=2, HALF_UP)
   - shopId (UUID, FK)
   - customerId (UUID, FK)
   - createdAt (LocalDateTime, immutable)

2. ORDER_ITEMS
   - orderItemId (UUID, PK)
   - orderId (UUID, FK)
   - productId (UUID, FK)
   - quantity (int > 0)
   - soldPrice (BigDecimal, scale=2, HALF_UP)
   - subtotal (BigDecimal, derived)

3. RECEIPTS
   - receiptId (UUID, PK)
   - orderId (UUID, FK, unique)
   - receiptNumber (String, unique)
   - isPaid (boolean)
   - bankTransactionRef (String, nullable)
   - issuedAt (LocalDateTime, immutable)

4. DELIVERIES
   - deliveryId (UUID, PK)
   - orderId (UUID, FK, unique)
   - status (Enum: PENDING, IN_TRANSIT, DELIVERED, FAILED)
   - address (String)
   - completedAt (LocalDateTime, nullable)

5. PAYMENT_INTENTS (Temporary Table)
   - intentId (UUID, PK)
   - bankRefId (String, unique)
   - shopId (UUID)
   - customerId (UUID)
   - totalAmount (BigDecimal)
   - cartSnapshot (JSON/String)
   - status (Enum: CREATED, CONFIRMED, EXPIRED)
   - createdAt (LocalDateTime)

--------------------------------------------------
RELATIONSHIPS
--------------------------------------------------

- Order → OrderItem (1 : Many)
- Order → Receipt (1 : 1)
- Order → Delivery (1 : 1, optional)
- Shop → Customer (1 : Many)
- PaymentIntent → Order (1 : 1 after confirmation)

--------------------------------------------------
CORE DESIGN REQUIREMENTS
--------------------------------------------------

1. Constructors
   - Mandatory fields only
   - No default constructors
   - All validation in constructors or private setters

2. Immutability
   - UUIDs, createdAt, issuedAt must be immutable
   - No public setters

3. Monetary Values
   - Use BigDecimal
   - Scale = 2
   - RoundingMode.HALF_UP
   - No float/double

4. Enums
   - All status fields must be enums
   - No string states

5. Validation Rules
   - UUID validation
   - price >= 0
   - quantity > 0
   - totalPrice = sum(items)
   - address not empty
   - receiptNumber format: INV-XXXX

6. State Machine Enforcement

   Order:
     PENDING → PAID → CANCELLED
     PAID → cannot revert

   Delivery:
     PENDING → IN_TRANSIT → DELIVERED
     FAILED → terminal

   PaymentIntent:
     CREATED → CONFIRMED → DELETED

   Illegal transitions must throw IllegalStateException.

7. Snapshot Protection
   - cartSnapshot must be immutable
   - OrderItems must be created only from snapshot
   - No price re-fetch after intent creation

8. Audit Safety
   - createdAt auto-set
   - No manual overrides

--------------------------------------------------
BUSINESS METHODS
--------------------------------------------------

Order:
   - markPaid()
   - cancel()
   - verifyTotal()
   - isFinalized()

OrderItem:
   - calculateSubtotal()

Receipt:
   - confirmPayment(bankRef)
   - generateNumber()

Delivery:
   - dispatch()
   - complete()
   - fail(reason)

PaymentIntent:
   - confirm()
   - expire()
   - isValid()

InventoryService (Domain Service):
   - softCheckStock()
   - hardDeductStock()

OrderService (Domain Orchestrator):
   - createIntent()
   - verifyPayment()
   - finalizeOrderAtomic()
   - cleanupIntent()

--------------------------------------------------
ATOMIC TRANSACTION REQUIREMENT
--------------------------------------------------

finalizeOrderAtomic() must run inside a single database transaction.

Steps:

1. Lock PaymentIntent
2. Validate payment amount
3. Create Order
4. Create OrderItems from snapshot
5. Deduct inventory
6. Mark Receipt paid
7. Create Delivery (if needed)
8. Commit
9. Delete intent

If any step fails → rollback.

--------------------------------------------------
SECURITY & FRAUD PROTECTION
--------------------------------------------------

- Webhooks must be idempotent
- Reject duplicate bankRefId
- Reject mismatched amounts
- Validate webhook signature
- Prevent replay attacks

--------------------------------------------------
LIFECYCLE AUTOMATION
--------------------------------------------------

- expireOldIntents()
- cancelUnpaidOrders()
- markFailedDeliveries()
- reconcilePayments()

--------------------------------------------------
CODE QUALITY
--------------------------------------------------

- Full Java source
- Proper packages/imports
- JavaDoc for public methods
- equals(), hashCode(), toString() via UUID
- No duplicated validation
- Validation utility allowed
- No database queries in entities

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

- Code only
- No explanations
- No pseudocode
- No markdown
- Production-ready classes

--------------------------------------------------
GOAL
--------------------------------------------------

Generate a real-world Order & Logistics engine that supports:

- Secure payment processing
- Inventory protection
- Atomic order finalization
- Receipt auditing
- Delivery tracking
- Fraud prevention
- High-volume scalability

The output must represent enterprise-grade transactional software, not simple POJOs.