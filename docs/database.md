# Velora Platform — Database Design

## Conventions

- All primary keys are UUID
- All monetary fields use DECIMAL(19,2)
- All timestamps use TIMESTAMP WITH TIME ZONE (UTC)
- Soft deletes use `deleted_at TIMESTAMP` (nullable)
- Row-level security enforced via `shop_id` on all tenant-scoped tables
- Enums stored as VARCHAR with CHECK constraints

---

## Schema: Authentication

### users
| Column | Type | Constraints |
|---|---|---|
| user_id | UUID | PK |
| username | VARCHAR(30) | NOT NULL, UNIQUE |
| profile_url | TEXT | NULLABLE |
| bio | TEXT | NULLABLE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','SUSPENDED','DELETED') |

### user_auth
| Column | Type | Constraints |
|---|---|---|
| auth_id | UUID | PK |
| user_id | UUID | FK → users.user_id, NOT NULL |
| provider | VARCHAR(20) | NOT NULL, CHECK IN ('EMAIL','GOOGLE','FACEBOOK') |
| provider_uid | VARCHAR(255) | NULLABLE |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(72) | NULLABLE (null for OAuth) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| last_login_at | TIMESTAMP | NULLABLE |

**Indexes:** `user_auth(email)`, `user_auth(user_id)`

### roles
| Column | Type | Constraints |
|---|---|---|
| role_id | UUID | PK |
| role_name | VARCHAR(20) | NOT NULL, UNIQUE, CHECK IN ('SUPER_ADMIN','OWNER','MANAGER','SELLER') |

### memberships
| Column | Type | Constraints |
|---|---|---|
| member_id | UUID | PK |
| user_id | UUID | FK → users.user_id, NOT NULL |
| shop_id | UUID | FK → shops.shop_id, NULLABLE |
| role_id | UUID | FK → roles.role_id, NOT NULL |
| seller_name | VARCHAR(30) | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**Indexes:** `memberships(user_id)`, `memberships(shop_id)`, `memberships(user_id, shop_id)`

---

## Schema: Plan & Subscription

### subscription_plans
| Column | Type | Constraints |
|---|---|---|
| plan_id | UUID | PK |
| name | VARCHAR(100) | NOT NULL |
| slug | VARCHAR(100) | NOT NULL, UNIQUE |
| price | DECIMAL(19,2) | NOT NULL, CHECK >= 0 |
| duration_months | INT | NOT NULL, CHECK > 0 |
| payer_type | VARCHAR(10) | NOT NULL, CHECK IN ('USER','SHOP') |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE |

### features
| Column | Type | Constraints |
|---|---|---|
| feature_id | UUID | PK |
| feature_key | VARCHAR(64) | NOT NULL, UNIQUE |
| target_type | VARCHAR(10) | NOT NULL, CHECK IN ('USER','SHOP','BOTH') |
| description | TEXT | NULLABLE |

### plan_features
| Column | Type | Constraints |
|---|---|---|
| plan_id | UUID | FK → subscription_plans.plan_id, NOT NULL |
| feature_id | UUID | FK → features.feature_id, NOT NULL |
| limit_value | INT | NULLABLE |
| is_enabled | BOOLEAN | NOT NULL, DEFAULT TRUE |

**PK:** (plan_id, feature_id)

### platform_registry
| Column | Type | Constraints |
|---|---|---|
| registry_id | UUID | PK |
| owner_id | UUID | NOT NULL |
| target_type | VARCHAR(10) | NOT NULL, CHECK IN ('USER','SHOP') |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','BANNED','INACTIVE','PENDING') |
| ban_reason | TEXT | NULLABLE |
| transaction_id | UUID | NULLABLE |

**Indexes:** `platform_registry(owner_id)`, `platform_registry(status)`

### user_accounts
| Column | Type | Constraints |
|---|---|---|
| subscription_id | UUID | PK |
| user_id | UUID | NOT NULL |
| plan_id | UUID | FK → subscription_plans.plan_id, NOT NULL |
| registry_id | UUID | FK → platform_registry.registry_id, NOT NULL |
| transaction_id | UUID | NULLABLE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','TRIAL','EXPIRED','CANCELLED') |
| start_date | TIMESTAMP | NULLABLE |
| end_date | TIMESTAMP | NULLABLE |
| refund_deadline | TIMESTAMP | NULLABLE |

**Indexes:** `user_accounts(user_id)`, `user_accounts(status)`

### shop_accounts
| Column | Type | Constraints |
|---|---|---|
| subscription_id | UUID | PK |
| shop_id | UUID | NOT NULL |
| plan_id | UUID | FK → subscription_plans.plan_id, NOT NULL |
| registry_id | UUID | FK → platform_registry.registry_id, NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','EXPIRED','CANCELLED','PAST_DUE') |
| start_date | TIMESTAMP | NULLABLE |
| end_date | TIMESTAMP | NULLABLE |
| refund_deadline | TIMESTAMP | NULLABLE |
| is_auto_renew | BOOLEAN | NOT NULL, DEFAULT FALSE |

**Indexes:** `shop_accounts(shop_id)`, `shop_accounts(status)`

### user_subscriptions
| Column | Type | Constraints |
|---|---|---|
| subscription_id | UUID | PK |
| user_id | UUID | NOT NULL |
| transaction_id | UUID | NOT NULL |
| plan_id | UUID | FK → subscription_plans.plan_id, NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','EXPIRED','CANCELLED','REFUNDED') |
| start_date | TIMESTAMP | NOT NULL |
| end_date | TIMESTAMP | NOT NULL |
| refund_deadline | TIMESTAMP | NOT NULL |

### shop_subscriptions
| Column | Type | Constraints |
|---|---|---|
| subscription_id | UUID | PK |
| shop_id | UUID | NOT NULL |
| transaction_id | UUID | NOT NULL |
| plan_id | UUID | FK → subscription_plans.plan_id, NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','EXPIRED','PAST_DUE','REFUNDED') |
| start_date | TIMESTAMP | NOT NULL |
| end_date | TIMESTAMP | NOT NULL |
| refund_deadline | TIMESTAMP | NOT NULL |

---

## Schema: Store Management

### shops
| Column | Type | Constraints |
|---|---|---|
| shop_id | UUID | PK |
| owner_id | UUID | FK → users.user_id, NOT NULL |
| legal_name | VARCHAR(255) | NULLABLE |
| tax_id | VARCHAR(50) | NULLABLE |
| slug | VARCHAR(100) | NOT NULL, UNIQUE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('PENDING','ACTIVE','SUSPENDED','BANNED') |
| street | VARCHAR(255) | NOT NULL |
| city | VARCHAR(100) | NOT NULL |
| district | VARCHAR(100) | NOT NULL |
| province | VARCHAR(100) | NOT NULL |

**Indexes:** `shops(owner_id)`, `shops(slug)`, `shops(status)`

### shop_settings
| Column | Type | Constraints |
|---|---|---|
| shop_id | UUID | PK, FK → shops.shop_id |
| currency | VARCHAR(10) | NOT NULL, DEFAULT 'USD' |
| timezone | VARCHAR(50) | NOT NULL, DEFAULT 'Asia/Phnom_Penh' |
| updated_at | TIMESTAMP | NOT NULL |

---

## Schema: Inventory & Event Management

### categories
| Column | Type | Constraints |
|---|---|---|
| category_id | UUID | PK |
| name | VARCHAR(100) | NOT NULL |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |

**Indexes:** `categories(shop_id)`

### products
| Column | Type | Constraints |
|---|---|---|
| product_id | UUID | PK |
| name | VARCHAR(255) | NOT NULL |
| slug | VARCHAR(255) | NOT NULL |
| base_price | DECIMAL(19,2) | NOT NULL, CHECK > 0 |
| cost_price | DECIMAL(19,2) | NOT NULL, CHECK >= 0 |
| category_id | UUID | FK → categories.category_id, NOT NULL |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `products(shop_id)`, `products(category_id)`, UNIQUE(shop_id, name)

### product_variants
| Column | Type | Constraints |
|---|---|---|
| variant_id | UUID | PK |
| product_id | UUID | FK → products.product_id, NOT NULL |
| size | VARCHAR(50) | NULLABLE |
| color | VARCHAR(50) | NULLABLE |
| stock_quantity | INT | NOT NULL, CHECK >= 0 |
| sku | VARCHAR(32) | NOT NULL, UNIQUE |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| category_id | UUID | FK → categories.category_id, NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `product_variants(product_id)`, `product_variants(shop_id)`, `product_variants(sku)`

### event_types
| Column | Type | Constraints |
|---|---|---|
| event_id | UUID | PK |
| name | VARCHAR(100) | NOT NULL |
| discount_value | DECIMAL(19,2) | NOT NULL, CHECK >= 0 |
| discount_type | VARCHAR(20) | NOT NULL, CHECK IN ('PERCENTAGE','FIXED') |
| is_available | BOOLEAN | NOT NULL |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| start_date | TIMESTAMP | NOT NULL |
| end_date | TIMESTAMP | NOT NULL |
| min_amount | DECIMAL(19,2) | NULLABLE |
| usage_limit | INT | NULLABLE |

### event_products
| Column | Type | Constraints |
|---|---|---|
| event_product_id | UUID | PK |
| sort_order | INT | NOT NULL, CHECK >= 0 |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ACTIVE','SCHEDULED','ENDED') |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| deleted_at | TIMESTAMP | NULLABLE |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| product_id | UUID | FK → products.product_id, NOT NULL |
| category_id | UUID | FK → categories.category_id, NOT NULL |
| event_id | UUID | FK → event_types.event_id, NOT NULL |

**Indexes:** `event_products(shop_id)`, `event_products(event_id)`, `event_products(product_id)`

---

## Schema: Sale Management

### payment_intents (temporary)
| Column | Type | Constraints |
|---|---|---|
| intent_id | UUID | PK |
| bank_ref_id | VARCHAR(255) | NOT NULL, UNIQUE |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| customer_id | UUID | NOT NULL |
| total_amount | DECIMAL(19,2) | NOT NULL |
| cart_snapshot | TEXT | NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('CREATED','CONFIRMED','EXPIRED') |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `payment_intents(bank_ref_id)`, `payment_intents(status)`

### orders
| Column | Type | Constraints |
|---|---|---|
| order_id | UUID | PK |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('PENDING','PAID','CANCELLED') |
| total_price | DECIMAL(19,2) | NOT NULL |
| shop_id | UUID | FK → shops.shop_id, NOT NULL |
| customer_id | UUID | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `orders(shop_id)`, `orders(customer_id)`, `orders(status)`

### order_items
| Column | Type | Constraints |
|---|---|---|
| order_item_id | UUID | PK |
| order_id | UUID | FK → orders.order_id, NOT NULL |
| product_id | UUID | FK → products.product_id, NOT NULL |
| quantity | INT | NOT NULL, CHECK > 0 |
| sold_price | DECIMAL(19,2) | NOT NULL |
| subtotal | DECIMAL(19,2) | NOT NULL |

**Indexes:** `order_items(order_id)`

### receipts
| Column | Type | Constraints |
|---|---|---|
| receipt_id | UUID | PK |
| order_id | UUID | FK → orders.order_id, NOT NULL, UNIQUE |
| receipt_number | VARCHAR(20) | NOT NULL, UNIQUE |
| is_paid | BOOLEAN | NOT NULL, DEFAULT FALSE |
| bank_transaction_ref | VARCHAR(255) | NULLABLE |
| issued_at | TIMESTAMP | NOT NULL |

### deliveries
| Column | Type | Constraints |
|---|---|---|
| delivery_id | UUID | PK |
| order_id | UUID | FK → orders.order_id, NOT NULL, UNIQUE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('PENDING','IN_TRANSIT','DELIVERED','FAILED') |
| address | TEXT | NOT NULL |
| completed_at | TIMESTAMP | NULLABLE |

---

## Schema: Payment

### payment_methods
| Column | Type | Constraints |
|---|---|---|
| method_id | UUID | PK |
| gateway_token | TEXT | NOT NULL |
| card_type | VARCHAR(20) | NOT NULL, CHECK IN ('VISA','MASTERCARD','AMEX') |
| last_four | CHAR(4) | NOT NULL |
| expiry_date | DATE | NOT NULL |

### transactions
| Column | Type | Constraints |
|---|---|---|
| transaction_id | UUID | PK |
| amount | DECIMAL(19,2) | NOT NULL, CHECK >= 0 |
| currency | VARCHAR(10) | NOT NULL |
| payer_type | VARCHAR(10) | NOT NULL, CHECK IN ('USER','SHOP') |
| payer_id | UUID | NOT NULL |
| plan_id | UUID | NULLABLE |
| gateway_ref | VARCHAR(255) | NULLABLE, UNIQUE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('PENDING','PAID','FAILED') |
| created_at | TIMESTAMP | NOT NULL |
| paid_at | TIMESTAMP | NULLABLE |

**Indexes:** `transactions(payer_id)`, `transactions(gateway_ref)`, `transactions(status)`

### payment_intents_platform
| Column | Type | Constraints |
|---|---|---|
| intent_id | UUID | PK |
| transaction_id | UUID | FK → transactions.transaction_id, NOT NULL |
| qr_code_data | TEXT | NULLABLE |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('PENDING','SUCCESS','FAILED') |
| expires_at | TIMESTAMP | NOT NULL |
| method_id | UUID | FK → payment_methods.method_id, NULLABLE |

### invoices
| Column | Type | Constraints |
|---|---|---|
| invoice_id | UUID | PK |
| invoice_no | VARCHAR(50) | NOT NULL, UNIQUE |
| transaction_id | UUID | FK → transactions.transaction_id, NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('ISSUED','CANCELLED') |
| sub_total | DECIMAL(19,2) | NOT NULL |
| tax_amount | DECIMAL(19,2) | NOT NULL |
| total_amount | DECIMAL(19,2) | NOT NULL |
| discount_price | DECIMAL(19,2) | NOT NULL, DEFAULT 0 |

### platform_revenue_snapshots
| Column | Type | Constraints |
|---|---|---|
| platform_snap_id | UUID | PK |
| snapshot_date | DATE | NOT NULL, UNIQUE |
| total_revenue | DECIMAL(19,2) | NOT NULL |
| active_paying_shops | INT | NOT NULL |
| invoice_id | UUID | FK → invoices.invoice_id, NULLABLE |

---

## Schema: Notification

### notifications
| Column | Type | Constraints |
|---|---|---|
| notification_id | UUID | PK |
| user_id | UUID | NOT NULL |
| type | VARCHAR(20) | NOT NULL, CHECK IN ('TRANSACTIONAL','SYSTEM','SUPPORT') |
| priority | VARCHAR(10) | NOT NULL, CHECK IN ('HIGH','NORMAL') |
| title | VARCHAR(255) | NOT NULL |
| content | TEXT | NOT NULL |
| link_url | TEXT | NULLABLE |
| is_read | BOOLEAN | NOT NULL, DEFAULT FALSE |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `notifications(user_id)`, `notifications(user_id, is_read)`, `notifications(created_at DESC)`

### notification_preferences
| Column | Type | Constraints |
|---|---|---|
| user_id | UUID | PK |
| email_enabled | BOOLEAN | NOT NULL, DEFAULT TRUE |
| billing_alerts | BOOLEAN | NOT NULL, DEFAULT TRUE |
| marketing_alerts | BOOLEAN | NOT NULL, DEFAULT FALSE |
| updated_at | TIMESTAMP | NOT NULL |

### notification_dispatch_records
| Column | Type | Constraints |
|---|---|---|
| dispatch_id | UUID | PK |
| notification_id | UUID | FK → notifications.notification_id, NOT NULL |
| channel | VARCHAR(20) | NOT NULL, CHECK IN ('IN_APP','EMAIL') |
| status | VARCHAR(20) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| dispatched_at | TIMESTAMP | NULLABLE |
| retry_count | INT | NOT NULL, DEFAULT 0 |

---

## Schema: Feedback

### feature_suggestions
| Column | Type | Constraints |
|---|---|---|
| suggestion_id | UUID | PK |
| user_id | UUID | NOT NULL |
| category | VARCHAR(50) | NOT NULL |
| problem_text | TEXT | NOT NULL |
| solution_text | TEXT | NOT NULL |
| status | VARCHAR(20) | NOT NULL, CHECK IN ('NEW','IN_REVIEW','BACKLOG','SHIPPED') |
| admin_notes | TEXT | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `feature_suggestions(user_id)`, `feature_suggestions(status)`

---

## Schema: Analytics

### daily_product_snapshots
| Column | Type | Constraints |
|---|---|---|
| snapshot_id | UUID | PK |
| snapshot_date | DATE | NOT NULL |
| product_id | UUID | NOT NULL |
| variant_id | UUID | NOT NULL |
| seller_id | UUID | NOT NULL |
| category_id | UUID | NOT NULL |
| shop_id | UUID | NOT NULL |
| qty_sold | INT | NOT NULL, CHECK >= 0 |
| base_cost_price | DECIMAL(19,2) | NOT NULL |
| unit_sale_price | DECIMAL(19,2) | NOT NULL |
| stock_at_midnight | INT | NOT NULL, CHECK >= 0 |
| created_at | TIMESTAMP | NOT NULL |

**Indexes:** `daily_product_snapshots(shop_id, snapshot_date)`, `daily_product_snapshots(seller_id)`, `daily_product_snapshots(category_id)`
**Unique:** (shop_id, product_id, variant_id, snapshot_date)

### daily_category_snapshots
| Column | Type | Constraints |
|---|---|---|
| snapshot_id | UUID | PK |
| snapshot_date | DATE | NOT NULL |
| category_id | UUID | NOT NULL |
| shop_id | UUID | NOT NULL |
| cat_gross_revenue | DECIMAL(19,2) | NOT NULL |
| cat_net_profit | DECIMAL(19,2) | NOT NULL |
| cat_items_sold | INT | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Unique:** (shop_id, category_id, snapshot_date)

### daily_snapshots
| Column | Type | Constraints |
|---|---|---|
| snapshot_id | UUID | PK |
| snapshot_date | DATE | NOT NULL |
| org_id | UUID | NOT NULL |
| shop_id | UUID | NOT NULL |
| total_gross | DECIMAL(19,2) | NOT NULL |
| total_profit | DECIMAL(19,2) | NOT NULL |
| order_count | INT | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Unique:** (shop_id, snapshot_date)

---

## Schema: Revenue

### platform_revenue_snapshots_v2
| Column | Type | Constraints |
|---|---|---|
| platform_snap_id | UUID | PK |
| snapshot_date | DATE | NOT NULL, UNIQUE |
| total_subscription_revenue | DECIMAL(19,2) | NOT NULL |
| platform_net_profit | DECIMAL(19,2) | NOT NULL |
| active_paying_shops | INT | NOT NULL, CHECK >= 0 |
| snapshot_status | VARCHAR(20) | NOT NULL, CHECK IN ('DRAFT','FINALIZED','LOCKED') |
| created_at | TIMESTAMP | NOT NULL |

---

## Key Relationships Summary

```
users (1:1) user_auth
users (1:N) memberships
roles (1:N) memberships
shops (1:N) memberships
shops (1:1) shop_settings
shops (1:N) products
shops (1:N) categories
products (1:N) product_variants
products (1:N) event_products
event_types (1:N) event_products
shops (1:N) orders
orders (1:N) order_items
orders (1:1) receipts
orders (1:1) deliveries
transactions (1:1) invoices
transactions (1:1) payment_intents_platform
users (1:1) notification_preferences
users (1:N) notifications
users (1:N) feature_suggestions
platform_registry (1:1) user_accounts
platform_registry (1:1) shop_accounts
subscription_plans (1:N) plan_features
features (1:N) plan_features
shops (1:N) daily_product_snapshots
shops (1:N) daily_category_snapshots
shops (1:N) daily_snapshots
```
