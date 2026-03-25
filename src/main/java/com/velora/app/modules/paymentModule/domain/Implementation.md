You are tasked with designing enterprise-grade Java classes for a platform's payment and revenue tracking system. The classes must fully represent the following database entities and enforce robust business logic, validation, and immutability.

Entities:

1. PAYMENT_METHODS
   - method_id (UUID, PK)
   - gateway_token (TEXT, encrypted)
   - card_type (Enum: VISA, MASTERCARD, AMEX)
   - last_four (CHAR(4), not null, must be digits)
   - expiry_date (DATE, not null)

2. PAYMENT_INTENTS
   - intent_id (UUID, PK)
   - transaction_id (UUID, FK to TRANSACTIONS)
   - qr_code_data (TEXT, nullable)
   - status (Enum: PENDING, SUCCESS, FAILED)
   - expires_at (TIMESTAMP, not null)
   - method_id (UUID, FK to PAYMENT_METHODS)

3. TRANSACTIONS
   - transaction_id (UUID, PK)
   - amount (BigDecimal, not null, scale=2, RoundingMode.HALF_UP)
   - currency (java.util.Currency)
   - payer_type (Enum: USER, SHOP)
   - payer_id (UUID, FK)
   - plan_id (UUID, FK)
   - gateway_ref (String, unique)
   - status (Enum: PENDING, PAID, FAILED)
   - created_at (LocalDateTime, set automatically)
   - paid_at (LocalDateTime, nullable)

4. INVOICES
   - invoice_id (UUID, PK)
   - invoice_no (String, unique, human-readable)
   - transaction_id (UUID, FK)
   - status (Enum: ISSUED, CANCELLED)
   - sub_total (BigDecimal)
   - tax_amount (BigDecimal)
   - total_amount (BigDecimal)
   - discount_price (BigDecimal, default 0)
   - Method: verifyTotal() → ensures sub_total + tax_amount - discount_price == total_amount

5. PLATFORM_REVENUE_SNAPSHOTS
   - platform_snap_id (UUID, PK)
   - snapshot_date (DATE, unique)
   - total_revenue (BigDecimal, aggregated)
   - active_paying_shops (Integer, aggregated)
   - invoice_id (UUID, FK to last invoice)

6. PaymentTesting.java:
   - Test all class inside payement
   - Use create simple class with simple data
   - Test all case  

Requirements:

1. Use **constructors for mandatory fields** and setters for optional/updatable fields.
2. Use setters inside constructor prevent using this access.
2. **Immutable IDs and audit fields**: UUIDs and createdAt must have private setters to prevent external modification after object creation.
3. **BigDecimal for all monetary fields** with scale=2 and RoundingMode.HALF_UP.
4. **State transitions**: Implement methods such as markPaid() and markFailed() with validation to prevent illegal transitions. Throw exceptions if a transition is not allowed.
5. **Enums and Value Objects**:
   - Currency uses `java.util.Currency`.
   - CardType, PaymentStatus, TransactionStatus, InvoiceStatus, PayerType as enums.
6. **Validation**:
   - UUID format for IDs.
   - last_four must be 4 digits.
   - Monetary amounts ≥ 0.
   - Enum values must be valid.
   - implement validation in all setter
   - Use validaton support function from utils/ValidationUtils.java
   - validation function not exist inside ValidatoinUtils.java create it inside the ValidationUtils.java and that function must become another support function
7. **Timestamps**:
   - createdAt auto-set in constructors.
   - paidAt updated only when payment succeeds.
   - expiresAt set when payment intent is generated.
8. **Relationships**: Include references as fields (e.g., transactionId in PaymentIntent).
9. **Business Methods**:
   - markPaid(), markFailed(), updateLastLogin() / paidAt timestamps.
   - verifyTotal() in Invoice.
   - Aggregate invoices into daily revenue snapshots.
10. **Code quality**:
    - Proper JavaDoc/comments.
    - Clean, maintainable, and scalable design.
    - Override equals(), hashCode(), and toString() using unique IDs.
    - Use utility classes for validation where necessary to avoid duplication.
11. **Output**: Provide fully structured Java classes with package/import statements. No database code; this is a pure domain/business logic design ready for ORM integration.

Goal: Generate **enterprise-ready Java classes** for PAYMENT_METHODS, PAYMENT_INTENTS, TRANSACTIONS, INVOICES, and PLATFORM_REVENUE_SNAPSHOTS with proper constructors, setters/getters, validation, state-machine enforcement, timestamps, enums, and monetary accuracy.
