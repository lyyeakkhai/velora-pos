You are tasked with designing enterprise-grade Java domain classes for a platform’s Plan and Subscription Management system.

The system controls feature access, billing lifecycle, and account activation for both Users and Shops.

You must convert the following database design and business logic into clean, maintainable, production-ready Java classes with strong validation and business rules.

--------------------------------------------------
ENTITIES
--------------------------------------------------

1. SUBSCRIPTION_PLANS
   - plan_id (UUID, PK, immutable)
   - name (String)
   - slug (String, unique)
   - price (BigDecimal, scale=2, HALF_UP)
   - duration_months (int)
   - payer_type (Enum: USER, SHOP)
   - is_active (boolean)

2. FEATURES
   - feature_id (UUID, PK)
   - feature_key (String, unique)
   - target_type (Enum: USER, SHOP, BOTH)
   - description (String)

3. PLAN_FEATURES (Bridge Table)
   - plan_id (UUID, FK)
   - feature_id (UUID, FK)
   - limit_value (Integer, nullable)
   - is_enabled (boolean)

4. PLATFORM_REGISTRY (Master Switch)
   - registry_id (UUID, PK)
   - owner_id (UUID)
   - target_type (Enum: USER, SHOP)
   - status (Enum: ACTIVE, BANNED, INACTIVE, PENDING)
   - ban_reason (String, nullable)
   - transaction_id (UUID, nullable)

5. SHOP_ACCOUNTS
   - subscription_id (UUID, PK)
   - shop_id (UUID)
   - plan_id (UUID)
   - registry_id (UUID)
   - status (Enum: ACTIVE, EXPIRED, CANCELLED, PAST_DUE)
   - start_date (LocalDateTime)
   - end_date (LocalDateTime)
   - refund_deadline (LocalDateTime)
   - is_auto_renew (boolean)

6. USER_ACCOUNTS
   - subscription_id (UUID, PK)
   - user_id (UUID)
   - plan_id (UUID)
   - registry_id (UUID)
   - transaction_id (UUID, nullable)
   - status (Enum: ACTIVE, TRIAL, EXPIRED, CANCELLED)
   - start_date (LocalDateTime)
   - end_date (LocalDateTime)
   - refund_deadline (LocalDateTime)

7. USER_SUBSCRIPTIONS
   - subscription_id (UUID, PK)
   - user_id (UUID)
   - transaction_id (UUID)
   - plan_id (UUID)
   - status (Enum: ACTIVE, EXPIRED, CANCELLED, REFUNDED)
   - start_date (LocalDateTime)
   - end_date (LocalDateTime)
   - refund_deadline (LocalDateTime)

8. SHOP_SUBSCRIPTIONS
   - subscription_id (UUID, PK)
   - shop_id (UUID)
   - transaction_id (UUID)
   - plan_id (UUID)
   - status (Enum: ACTIVE, EXPIRED, PAST_DUE, REFUNDED)
   - start_date (LocalDateTime)
   - end_date (LocalDateTime)
   - refund_deadline (LocalDateTime)

--------------------------------------------------
CORE DESIGN REQUIREMENTS
--------------------------------------------------

1. Constructors
   - Use constructors for mandatory fields only.
   - No default constructors.
   - All validation must run through setters or private validation methods.

2. Immutability
   - UUID fields, registry_id, and created timestamps must be immutable.
   - Their setters must be private/protected.

3. Monetary Values
   - All prices use BigDecimal
   - Scale = 2
   - RoundingMode.HALF_UP

4. Enums
   - All status, payer_type, and target_type fields must use enums.
   - No raw strings for state.

5. Validation Rules
   - UUID format validation
   - price >= 0
   - duration_months > 0
   - slug must be lowercase, unique, URL-safe
   - feature_key must be valid identifier format
   - dates must follow logical order (start < end)
   - implement validation in all setter
   - Use validaton support function from utils/ValidationUtils.java
   - validation function not exist inside ValidatoinUtils.java create it inside the ValidationUtils.java and that function must become another support function

6. State Machine Enforcement
   - Implement legal state transitions.
   - Illegal transitions must throw IllegalStateException.

   Example:
     ACTIVE → EXPIRED → CANCELLED
     BANNED → cannot return to ACTIVE

7. Timestamps
   - startDate set at activation
   - endDate calculated from plan duration
   - refundDeadline = startDate + 14 days
   - No public setters for audit timestamps

8. Relationships
   - Maintain ID references between entities
   - Example: ShopAccount → registryId → PlatformRegistry

9. Business Methods

   PlatformRegistry:
   - activate()
   - ban(reason)
   - deactivate()
   - verifyAccess()

   SubscriptionPlan:
   - isAvailable()
   - disable()
   - enable()

   UserAccount / ShopAccount:
   - activatePlan()
   - extendPlan()
   - expire()
   - cancel()
   - renew()
   - isActive()

   Subscription:
   - calculateEndDate()
   - calculateRefundDeadline()
   - markRefunded()

   Feature Access:
   - hasFeature(featureKey)
   - getFeatureLimit(featureKey)

10. Lifecycle Automation Support
    - Provide methods for cron/job usage:
      checkExpiration()
      markExpiredIfNeeded()

11. Code Quality
    - JavaDoc for all public methods
    - Clean separation of concerns
    - No duplicated validation logic
    - Utility class allowed for validation
    - Override equals(), hashCode(), toString() using UUID

12. ORM Ready (Optional)
    - Add JPA/Hibernate annotations only if useful
    - No database queries inside entities

13. plan_subscroption.java:
   - Test all class inside payement
   - Use create simple class with simple data
   - Test all case  

--------------------------------------------------
BUSINESS LOGIC TO IMPLEMENT
--------------------------------------------------

1. User Onboarding
   - Auto-create PlatformRegistry
   - Auto-assign Basic Plan
   - Start date = now
   - No payment required

2. Shop Creation
   - Requires active UserAccount
   - Registry status = PENDING until paid

3. Payment Confirmation
   - SUCCESS → activate plan
   - FAILED → remain locked

4. Registry Decision Engine
   - Routes activation to UserAccount or ShopAccount
   - Extends existing subscriptions if present

5. Expiration Job Support
   - Daily expiration check
   - Update registry and account status

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

- Provide full Java source code
- Proper package and imports
- All enums included
- Validation utility class if needed
- No pseudo-code
- No explanations
- Code only

--------------------------------------------------
GOAL
--------------------------------------------------

Generate enterprise-ready, domain-driven Java classes that implement:

- Plan management
- Feature control
- Platform registry authority
- Subscription lifecycle
- Billing enforcement
- Expiration automation
- Secure access control

The output must represent a real production-grade subscription engine, not simple POJOs.