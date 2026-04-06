# Feature 6: Payment

## System Overview

This system manages payment methods, payment intents, transactions, and invoices. It supports multiple card types, QR code payments, and platform revenue tracking.

## Domain Model

### Entities

#### PaymentMethod
Card payment details.

| Field | Type | Validation |
|-------|------|------------|
| methodId | UUID | PK |
| userId | UUID | FK |
| gatewayToken | String | Encrypted |
| cardType | CardType | VISA/MASTERCARD/AMEX |
| lastFour | String | Exactly 4 digits |
| expiryDate | LocalDate | Not null, not expired |

#### PaymentIntent
Payment session.

| Field | Type | Validation |
|-------|------|------------|
| intentId | UUID | PK |
| transactionId | UUID | FK to Transaction |
| qrCodeData | String | Nullable |
| status | PaymentIntentStatus | PENDING/SUCCESS/FAILED |
| expiresAt | LocalDateTime | Not null |
| methodId | UUID | FK to PaymentMethod |

#### Transaction
Financial record.

| Field | Type | Validation |
|-------|------|------------|
| transactionId | UUID | PK |
| amount | BigDecimal | >= 0, scale=2, HALF_UP |
| currency | Currency | From java.util.Currency |
| payerType | PayerType | USER/SHOP |
| payerId | UUID | FK |
| planId | UUID | FK (subscription) |
| gatewayRef | String | Unique |
| status | TransactionStatus | PENDING/PAID/FAILED |
| createdAt | LocalDateTime | Auto-set |
| paidAt | LocalDateTime | Nullable |

#### Invoice
Billing document.

| Field | Type | Validation |
|-------|------|------------|
| invoiceId | UUID | PK |
| invoiceNo | String | Unique |
| transactionId | UUID | FK |
| status | InvoiceStatus | ISSUED/CANCELLED |
| subTotal | BigDecimal | scale=2, HALF_UP |
| taxAmount | BigDecimal | scale=2, HALF_UP |
| discountPrice | BigDecimal | Default 0, scale=2, HALF_UP |
| totalAmount | BigDecimal | scale=2, HALF_UP |

#### PlatformRevenueSnapshot
Daily platform revenue aggregate.

| Field | Type | Validation |
|-------|------|------------|
| platformSnapId | UUID | PK |
| snapshotDate | LocalDate | Unique |
| totalRevenue | BigDecimal | Aggregated |
| activePayingShops | Integer | Aggregated |
| invoiceId | UUID | FK to last invoice |

### Value Objects

| Value Object | Description |
|--------------|------------|
| CardType | VISA, MASTERCARD, AMEX |
| PaymentIntentStatus | PENDING, SUCCESS, FAILED |
| TransactionStatus | PENDING, PAID, FAILED |
| InvoiceStatus | ISSUED, CANCELLED |
| PayerType | USER, SHOP |

## Entity Relationships

```
Transaction (1:1) PaymentIntent
Transaction (1:1) Invoice
PaymentMethod (1:N) PaymentIntent
User (1:N) PaymentMethod
```

## Business Rules

1. lastFour: exactly 4 digits
2. amount >= 0 with BigDecimal scale=2, HALF_UP
3. gatewayRef: unique, immutable once set
4. Invoice verification: subTotal + taxAmount - discountPrice == totalAmount (verifyTotal())
5. paidAt set only on PAID transition
6. expiryDate must be in the future when creating

## State Transitions

```
Transaction:  PENDING → PAID (sets paidAt)
              PENDING → FAILED
              PAID/FAILED → terminal

PaymentIntent: PENDING → SUCCESS / FAILED

Invoice:      ISSUED → CANCELLED
```

## Implementation Details

### Business Methods

| Entity | Methods |
|--------|---------|
| Transaction | markPaid(), markFailed() |
| Invoice | verifyTotal(), cancel() |
| PaymentIntent | markSuccess(), markFailed(), isValid() |

### Invoice Verification

```
verifyTotal(): subTotal + taxAmount - discountPrice == totalAmount
```

### Revenue Aggregation

Daily revenue snapshots aggregate from successful transactions.

## OOP Best Practices Applied

1. **Constructor Validation** - All validation in constructors/setters
2. **Immutability** - IDs and createdAt immutable
3. **No Default Constructors** - Required fields mandatory
4. **Enums** - All status fields use enums
5. **BigDecimal** - All monetary values scale=2, HALF_UP
6. **State Machine** - Transitions enforced with validation
7. **Invoice Verification** - Mathematical verification of totals
8. **Exception Handling** - Uses IllegalStateException and IllegalArgumentException

### Reference Implementation Files

| File | Purpose |
|------|---------|
| `payment/domain/PaymentMethod.java` | Card payment details |
| `payment/domain/PaymentIntent.java` | Payment session |
| `payment/domain/Transaction.java` | Financial record |
| `payment/domain/Invoice.java` | Billing document |
| `payment/domain/PlatformRevenueSnapshot.java` | Daily revenue aggregate |
| `common/DomainException.java` | Base business exception |

### Exception Handling

The module uses two types of exceptions:

| Exception Type | Usage |
|---------------|-------|
| `IllegalStateException` | State transitions (transaction, invoice, snapshot) |
| `IllegalArgumentException` | Parameter validation (amount, null checks) |

```
RuntimeException
├── IllegalStateException (state transitions)
│   ├── Transaction:
│   │   ├── "Transaction status must be PENDING to mark as PAID"
│   │   └── "Transaction status must be PENDING to mark as FAILED"
│   ├── Invoice:
│   │   ├── "Invoice is already cancelled"
│   │   └── "Only ISSUED invoices can be cancelled"
│   └── PlatformRevenueSnapshot:
│       ├── "Can only set metrics for DRAFT snapshots"
│       ├── "Can only finalize DRAFT snapshots"
│       ├── "Cannot finalize snapshot without metrics"
│       └── "Can only lock FINALIZED snapshots"
└── IllegalArgumentException (validation)
    └── "amount must be >= 0"
```

## Known Limitations

1. **No payment gateway** - Stub implementation only
2. **No QR generation** - Data field only

## Dependencies

- **Depends on**: Feature 1 (Authentication), Feature 2 (Plan & Subscription)
- **Used by**: Feature 10 (Revenue)

---

**Implementation Status**: Implemented (Reference Implementation)  
**OOP Maturity**: High - Full entity implementation with state machine  
**Last Updated**: 2026-04-07