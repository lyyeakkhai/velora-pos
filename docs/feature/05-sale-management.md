# Feature 5: Sale Management

## System Overview

This system manages permanent sales records, inventory deduction, payment confirmation, receipt generation, and physical delivery lifecycle. It implements atomic order finalization with webhook verification and fraud prevention.

## Domain Model

### Entities

#### Order
Aggregate root - permanent sale record.

| Field | Type | Validation |
|-------|------|------------|
| orderId | UUID | Immutable |
| shopId | UUID | FK |
| customerId | UUID | FK |
| status | OrderStatus | PENDING/PAID/CANCELLED |
| totalPrice | BigDecimal | >= 0, scale=2, HALF_UP |
| createdAt | LocalDateTime | Immutable |

#### OrderItem
Line item in an order.

| Field | Type | Validation |
|-------|------|------------|
| orderItemId | UUID | PK |
| orderId | UUID | FK |
| productId | UUID | FK |
| variantId | UUID | FK |
| quantity | int | > 0 |
| soldPrice | BigDecimal | >= 0, scale=2, HALF_UP |
| subtotal | BigDecimal | Derived: quantity * soldPrice |

#### Receipt
Payment proof document.

| Field | Type | Validation |
|-------|------|------------|
| receiptId | UUID | PK |
| orderId | UUID | FK, unique |
| receiptNumber | String | Unique format: INV-XXXX |
| isPaid | boolean | Default false |
| bankTransactionRef | String | Nullable |
| issuedAt | LocalDateTime | Immutable |

#### Delivery
Logistics tracking.

| Field | Type | Validation |
|-------|------|------------|
| deliveryId | UUID | PK |
| orderId | UUID | FK, unique |
| status | DeliveryStatus | PENDING/IN_TRANSIT/DELIVERED/FAILED |
| address | String | Not blank |
| completedAt | LocalDateTime | Nullable |

#### PaymentIntent
Temporary pre-payment record.

| Field | Type | Validation |
|-------|------|------------|
| intentId | UUID | PK |
| bankRefId | String | Unique |
| shopId | UUID | FK |
| customerId | UUID | FK |
| totalAmount | BigDecimal | scale=2, HALF_UP |
| cartSnapshot | JSON/String | Immutable after creation |
| status | IntentStatus | CREATED/CONFIRMED/EXPIRED |
| createdAt | LocalDateTime | Managed |

### Value Objects

| Value Object | Description |
|--------------|------------|
| OrderStatus | PENDING, PAID, CANCELLED |
| DeliveryStatus | PENDING, IN_TRANSIT, DELIVERED, FAILED |
| IntentStatus | CREATED, CONFIRMED, EXPIRED |

## Entity Relationships

```
Order (1:N) OrderItem
Order (1:1) Receipt
Order (1:1) Delivery (optional)
PaymentIntent → Order (1:1 after confirmation)
```

## Business Rules

1. totalPrice = sum of all orderItem subtotals
2. quantity > 0
3. soldPrice >= 0
4. receiptNumber format: INV-XXXX
5. cartSnapshot is immutable after intent creation
6. bankRefId must be unique (idempotency)
7. Webhook signature must be verified
8. All queries scoped by shopId (row-level security)

## State Transitions

```
Order:      PENDING → PAID
           PENDING/PAID → CANCELLED
           PAID → cannot revert

Delivery:  PENDING → IN_TRANSIT → DELIVERED
           ANY → FAILED (terminal)

PaymentIntent: CREATED → CONFIRMED (deleted after finalization)
              CREATED → EXPIRED
```

## Implementation Details

### Business Methods

| Entity | Methods |
|--------|---------|
| Order | markPaid(), cancel(), verifyTotal(), isFinalized() |
| OrderItem | calculateSubtotal() |
| Receipt | confirmPayment(bankRef), generateNumber() |
| Delivery | dispatch(), complete(), fail(reason) |
| PaymentIntent | confirm(), expire(), isValid() |

### Domain Services

| Service | Methods |
|---------|---------|
| InventoryService | softCheckStock(), hardDeductStock(), restoreStock() |
| OrderService | createIntent(), verifyPayment(), finalizeOrderAtomic(), cleanupIntent() |

### Atomic Transaction (finalizeOrderAtomic)

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

### Security & Fraud Protection

- Webhooks must be idempotent
- Reject duplicate bankRefId
- Reject mismatched amounts
- Validate webhook signature
- Prevent replay attacks

### Lifecycle Automation

| Method | Description |
|--------|-------------|
| expireOldIntents() | Clean up expired intents |
| cancelUnpaidOrders() | Cancel orders not paid within window |
| markFailedDeliveries() | Mark deliveries as failed |
| reconcilePayments() | Idempotent webhook replay |

## OOP Best Practices Applied

1. **Constructor Validation** - All validation in constructors/setters
2. **Immutability** - IDs, createdAt, issuedAt immutable
3. **No Default Constructors** - Required fields mandatory
4. **Enums** - All status fields use enums
5. **BigDecimal** - All monetary values scale=2, HALF_UP
6. **Row-Level Access Security** - All entities scoped by shopId
7. **Snapshot Protection** - cartSnapshot immutable after lock
8. **Atomic Transactions** - Order finalization with rollback support
9. **Exception Handling** - Uses IllegalStateException and IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `sale/domain/Order.java` | Aggregate root - permanent sale record |
| `sale/domain/OrderItem.java` | Line item in an order |
| `sale/domain/Receipt.java` | Payment proof document |
| `sale/domain/Delivery.java` | Logistics tracking |
| `sale/domain/PaymentIntent.java` | Temporary pre-payment record |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses two types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `IllegalStateException` | State transitions (order status, receipt, delivery) |
| `IllegalArgumentException` | Parameter validation (amount, price, quantity) |

```
RuntimeException
├── IllegalStateException (state transitions)
│   ├── Order:
│   │   ├── "Order status must be PENDING to mark as PAID"
│   │   ├── "Order is already cancelled"
│   │   ├── "Receipt can only be attached to PAID orders"
│   │   ├── "Delivery can only be set for PAID orders"
│   │   └── "Only PENDING orders can be cancelled"
│   ├── Receipt:
│   │   └── "Receipt is already paid"
│   ├── PaymentIntent:
│   │   ├── "Cannot confirm a payment intent that is not in CREATED state"
│   │   └── "Cannot expire a confirmed payment intent"
│   └── Delivery:
│       ├── "Delivery cannot be dispatched from status: X"
│       ├── "Delivery cannot be completed from status: X"
│       └── "Delivery cannot be marked as failed from status: X"
└── IllegalArgumentException (validation)
    ├── "totalAmount must be >= 0"
    └── "unitPrice must be >= 0"
```

## Known Limitations

1. **No payment gateway integration** - Stub implementation only

## Dependencies

- **Depends on**: Feature 1 (Authentication), Feature 3 (Store Management), Feature 4 (Inventory)
- **Used by**: Feature 6 (Payment), Feature 9 (Reporting)

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity implementation with atomic transactions  
**Last Updated**: 2026-04-07