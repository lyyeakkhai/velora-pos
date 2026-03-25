You are tasked with designing an enterprise-grade Notification Management System for a multi-tenant commerce and learning platform (Velora / SkillHub).

This system is responsible for delivering transactional, system, and support notifications through in-app and email channels while respecting user preferences and security requirements.

You must convert the following schema and business rules into scalable Java domain models, services, and dispatch pipelines.

--------------------------------------------------
DATA ENTITIES
--------------------------------------------------

1. NOTIFICATIONS (Core Storage)

   - notificationId (UUID, PK, immutable)
   - userId (UUID, FK, required)
   - type (ENUM: TRANSACTIONAL, SYSTEM, SUPPORT)
   - priority (ENUM: HIGH, NORMAL)
   - title (String, max 255, required)
   - content (String, required)
   - linkUrl (String, nullable, validated URL)
   - isRead (boolean, default false)
   - createdAt (LocalDateTime, immutable)

2. NOTIFICATION_PREFERENCES

   - userId (UUID, PK/FK)
   - emailEnabled (boolean, default true)
   - billingAlerts (boolean, default true, immutable)
   - marketingAlerts (boolean, default false)
   - updatedAt (LocalDateTime)

--------------------------------------------------
CORE DESIGN PRINCIPLES
--------------------------------------------------

1. Immutability
   - notificationId and createdAt cannot be modified
   - Notifications are append-only
   - No delete operations (soft archive only)

2. Reliability
   - No notification loss
   - All events must be persisted before delivery
   - Dispatch failures must be retried

3. Security
   - All queries scoped by userId
   - No cross-user access
   - Prevent unauthorized status updates

4. Performance
   - Indexed by userId, isRead, createdAt
   - Paginated queries only
   - Async dispatching

--------------------------------------------------
VALIDATION RULES
--------------------------------------------------

- userId must be valid UUID
- title must not be blank
- content must not be blank
- linkUrl must be valid URL if present
- priority and type must be non-null
- billingAlerts must always be TRUE

All validation must be centralized.

--------------------------------------------------
NOTIFICATION LIFECYCLE
--------------------------------------------------

1. Trigger Phase
   - Business event occurs (payment, expiry, warning, support reply)

2. Creation Phase
   - Insert notification record
   - Set isRead = false
   - Set createdAt = now()

3. Preference Check
   - Fetch notification_preferences
   - Validate delivery permissions

4. Dispatch Phase
   - In-App: Always available
   - Email: Only if:
       priority == HIGH
       AND emailEnabled == true

5. Delivery Confirmation
   - Log success/failure
   - Retry if failed

--------------------------------------------------
DISPATCH PIPELINE
--------------------------------------------------

NotificationDispatcher (Async Worker)

Steps:

1. Fetch pending notifications
2. Load user preferences
3. Apply filtering rules
4. Send email if allowed
5. Mark dispatch status
6. Persist logs

All operations must be idempotent.

--------------------------------------------------
BUSINESS SERVICES
--------------------------------------------------

NotificationService
   - createNotification()
   - markAsRead()
   - markAllAsRead()
   - getUnreadCount()
   - getUserNotifications()

PreferenceService
   - getPreferences()
   - updatePreferences()
   - resetToDefault()

DispatchService
   - dispatchNotification()
   - retryFailed()
   - scheduleDelivery()

--------------------------------------------------
QUERY STRATEGY
--------------------------------------------------

Unread Count:
   SELECT COUNT(*)
   FROM notifications
   WHERE user_id = ?
   AND is_read = false

Latest Notifications:
   ORDER BY created_at DESC
   LIMIT 20

--------------------------------------------------
ROLE ACCESS CONTROL
--------------------------------------------------

USER:
   - Read own notifications
   - Update own preferences

ADMIN:
   - System notifications only
   - No access to private alerts

SYSTEM:
   - Can create notifications
   - Cannot read user inbox

--------------------------------------------------
AUDIT & LOGGING
--------------------------------------------------

- All dispatch attempts logged
- Timestamped delivery records
- Failure reasons stored
- Retry counters maintained

--------------------------------------------------
ERROR HANDLING
--------------------------------------------------

- Invalid user → reject
- Disabled email → skip email
- SMTP failure → retry queue
- Duplicate dispatch → ignore

--------------------------------------------------
SCALABILITY RULES
--------------------------------------------------

- Use message queue for dispatch
- Batch email sending
- Rate limiting per user
- Backpressure handling

--------------------------------------------------
CODE QUALITY
--------------------------------------------------

- Clean Architecture
- Domain-driven design
- No business logic in controllers
- Centralized validation utilities
- equals/hashCode via UUID
- JavaDoc on public APIs
- No duplicated dispatch logic

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

- Java source code only
- No markdown
- No explanations
- No pseudocode
- Production-ready

--------------------------------------------------
GOAL
--------------------------------------------------

Generate a robust, secure, scalable notification system that supports:

- In-app alerts
- Priority email delivery
- User preferences
- Audit compliance
- High availability
- Fault tolerance

The result must represent enterprise-grade messaging infrastructure, not a simple alert utility.