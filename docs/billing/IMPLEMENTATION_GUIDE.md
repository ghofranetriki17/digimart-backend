# 🎯 Minimal Billing System – Implementation Guide

Updated: 2026-02-05

## 📍 Starting Point (already in place)
- Tenant management
- User authentication
- RBAC (roles & permissions)
- Stores
- Audit logging

Missing today: products, orders, customers, and any e‑commerce features.

---

## 🎯 What We’re Adding: Standalone Billing

Works independently from products/orders.

### Core Features
1) 💰 Wallet System  
   - Each tenant gets a wallet on creation  
   - Track balance (credits/debits) with full history  
   - Admin can manually add/remove funds

2) 🎨 Premium Features Catalog  
   - Seeded by developers (e.g., `ADVANCED_ANALYTICS`, `MULTI_STORE`)  
   - Labels only for now; later can drive feature flags

3) 📦 Subscription Plans  
   - Admin creates plans (price, billing cycle, discounts)  
   - Select included features  
   - Special “Standard” plan (free, commission-based)

4) 📝 Subscription Management  
   - Assign plans to tenants  
   - Track active/expired subscriptions  
   - Maintain full history of plan changes

---

## 📊 Database Tables to Create

### 1) platform_config
```sql
CREATE TABLE platform_config (
  id BIGSERIAL PRIMARY KEY,
  config_key VARCHAR(255) UNIQUE NOT NULL,
  config_value TEXT NOT NULL,
  description TEXT,
  updated_at TIMESTAMP NOT NULL,
  updated_by BIGINT REFERENCES users(id)
);

INSERT INTO platform_config (config_key, config_value, description, updated_at) VALUES
('COMMISSION_PERCENTAGE', '2.5', 'Commission % to deduct from orders', NOW()),
('INITIAL_WALLET_BALANCE', '500', 'Initial credits for new tenants', NOW()),
('LOW_BALANCE_THRESHOLD', '50', 'Alert when balance drops below this', NOW()),
('DEFAULT_CURRENCY', 'TND', 'Platform currency', NOW());
```

### 2) tenant_wallets
```sql
CREATE TABLE tenant_wallets (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT UNIQUE NOT NULL REFERENCES tenants(id),
  balance DECIMAL(15,2) NOT NULL DEFAULT 0,
  currency VARCHAR(3) NOT NULL DEFAULT 'TND',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  last_transaction_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX idx_tenant_wallets_tenant ON tenant_wallets(tenant_id);
CREATE INDEX idx_tenant_wallets_status ON tenant_wallets(status);
```

### 3) wallet_transactions
```sql
CREATE TABLE wallet_transactions (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  wallet_id BIGINT NOT NULL REFERENCES tenant_wallets(id),
  type VARCHAR(50) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  balance_before DECIMAL(15,2) NOT NULL,
  balance_after DECIMAL(15,2) NOT NULL,
  reason TEXT NOT NULL,
  reference VARCHAR(255),
  processed_by BIGINT REFERENCES users(id),
  transaction_date TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_wallet_txn_type CHECK (type IN (
    'INITIAL_CREDIT',
    'MANUAL_CREDIT',
    'MANUAL_DEBIT',
    'ADJUSTMENT',
    'REFUND'
  ))
);

CREATE INDEX idx_wallet_txn_wallet ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_txn_tenant ON wallet_transactions(tenant_id);
CREATE INDEX idx_wallet_txn_date ON wallet_transactions(transaction_date DESC);
CREATE INDEX idx_wallet_txn_type ON wallet_transactions(type);
```

### 4) premium_features (seeded)
```sql
CREATE TABLE premium_features (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(50) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_feature_category CHECK (category IN (
    'ANALYTICS',
    'SALES',
    'MARKETING',
    'TECHNICAL',
    'SUPPORT'
  ))
);

INSERT INTO premium_features (code, name, description, category, display_order) VALUES
('ADVANCED_ANALYTICS', 'Advanced Analytics', 'Detailed sales reports and insights', 'ANALYTICS', 1),
('MULTI_STORE', 'Multi-Store Management', 'Manage multiple store locations', 'SALES', 2),
('CUSTOM_THEMES', 'Custom Themes', 'Personalize your storefront appearance', 'MARKETING', 3),
('API_ACCESS', 'API Access', 'Integrate with external systems', 'TECHNICAL', 4),
('BULK_OPERATIONS', 'Bulk Operations', 'Import/export data in bulk', 'TECHNICAL', 5),
('PRIORITY_SUPPORT', 'Priority Support', '24/7 dedicated support', 'SUPPORT', 6),
('CUSTOM_DOMAIN', 'Custom Domain', 'Use your own domain name', 'TECHNICAL', 7),
('LOYALTY_PROGRAM', 'Loyalty Program', 'Customer rewards and points', 'MARKETING', 8);
```

### 5) subscription_plans
```sql
CREATE TABLE subscription_plans (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(15,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'TND',
  billing_cycle VARCHAR(20) NOT NULL,
  discount_percentage DECIMAL(5,2) DEFAULT 0,
  is_standard BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,
  start_date TIMESTAMP,
  end_date TIMESTAMP,
  created_by BIGINT REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_billing_cycle CHECK (billing_cycle IN (
    'MONTHLY',
    'QUARTERLY',
    'YEARLY',
    'ONE_TIME'
  )),
  CONSTRAINT check_price_non_negative CHECK (price >= 0),
  CONSTRAINT check_discount_range CHECK (discount_percentage >= 0 AND discount_percentage <= 100)
);

INSERT INTO subscription_plans (code, name, description, price, billing_cycle, is_standard, created_at) VALUES
('STANDARD', 'Standard Plan', 'Commission-based plan with basic features', 0, 'MONTHLY', true, NOW());
```

### 6) plan_features
```sql
CREATE TABLE plan_features (
  id BIGSERIAL PRIMARY KEY,
  plan_id BIGINT NOT NULL REFERENCES subscription_plans(id) ON DELETE CASCADE,
  feature_id BIGINT NOT NULL REFERENCES premium_features(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT unique_plan_feature UNIQUE (plan_id, feature_id)
);

CREATE INDEX idx_plan_features_plan ON plan_features(plan_id);
CREATE INDEX idx_plan_features_feature ON plan_features(feature_id);
```

### 7) tenant_subscriptions
```sql
CREATE TABLE tenant_subscriptions (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING_ACTIVATION',
  start_date TIMESTAMP NOT NULL,
  end_date TIMESTAMP,
  next_billing_date TIMESTAMP,
  price_paid DECIMAL(15,2),
  payment_reference VARCHAR(255),
  activated_by BIGINT REFERENCES users(id),
  activated_at TIMESTAMP,
  cancelled_at TIMESTAMP,
  cancellation_reason TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_subscription_status CHECK (status IN (
    'ACTIVE',
    'PENDING_ACTIVATION',
    'EXPIRED',
    'CANCELLED',
    'SUSPENDED'
  ))
);

CREATE INDEX idx_tenant_subs_tenant ON tenant_subscriptions(tenant_id);
CREATE INDEX idx_tenant_subs_plan ON tenant_subscriptions(plan_id);
CREATE INDEX idx_tenant_subs_status ON tenant_subscriptions(status);
CREATE INDEX idx_tenant_subs_dates ON tenant_subscriptions(start_date, end_date);
```

### 8) subscription_history
```sql
CREATE TABLE subscription_history (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenants(id),
  subscription_id BIGINT NOT NULL REFERENCES tenant_subscriptions(id),
  old_plan_id BIGINT REFERENCES subscription_plans(id),
  new_plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
  action VARCHAR(50) NOT NULL,
  notes TEXT,
  performed_by BIGINT REFERENCES users(id),
  performed_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT check_subscription_action CHECK (action IN (
    'CREATED',
    'ACTIVATED',
    'UPGRADED',
    'DOWNGRADED',
    'RENEWED',
    'CANCELLED',
    'SUSPENDED',
    'EXPIRED'
  ))
);

CREATE INDEX idx_sub_history_tenant ON subscription_history(tenant_id);
CREATE INDEX idx_sub_history_subscription ON subscription_history(subscription_id);
CREATE INDEX idx_sub_history_date ON subscription_history(performed_at DESC);
```

---

## 🔄 Key Workflows

### Workflow 1: New Tenant Registration
1. Tenant created (existing flow)  
2. Create `TenantWallet` with `INITIAL_WALLET_BALANCE` and status `ACTIVE`  
3. Create `WalletTransaction` with `type = INITIAL_CREDIT` and amount = initial balance  
4. Create `TenantSubscription` on Standard plan with status `ACTIVE`, start now  
5. Create `SubscriptionHistory` with action `CREATED`

### Workflow 2: Admin Adds Credits to Tenant
1. Fetch tenant wallet  
2. Record `balance_before`  
3. Update wallet balance (`+ amount`)  
4. Create `WalletTransaction` (`MANUAL_CREDIT`, reason, reference, processed_by admin)

### Workflow 3: Admin Creates New Plan
1. Create `SubscriptionPlan` (name, price, billing cycle, etc.)  
2. For each selected feature, create `PlanFeature(plan_id, feature_id)`

### Workflow 4: Admin Activates Premium Plan for Tenant
1. Deactivate prior active subscription (set status `EXPIRED`)  
2. Create new `TenantSubscription` (`ACTIVE`, start now, set end/next billing)  
3. Create `SubscriptionHistory` (`UPGRADED` with `old_plan_id`, `new_plan_id`)

---

## 🎨 Admin UI (wireframes)

### Platform Configuration
```
Settings → Platform Config
┌─────────────────────────────────────┐
│ Commission Percentage:    [2.5] %   │
│ Initial Wallet Balance:   [500] TND │
│ Low Balance Alert:        [50] TND  │
│                                     │
│ [Save Changes]                      │
└─────────────────────────────────────┘
```

### Wallet Management
```
Tenants → Acme Corp → Wallet
┌──────────────────────────────────────────┐
│ Current Balance: 475 TND                 │
│ Status: ACTIVE                           │
│                                          │
│ [+ Add Credits]  [- Deduct Credits]      │
│                                          │
│ Recent Transactions:                     │
│ ┌────────────────────────────────────┐   │
│ │ 2026-02-04  INITIAL_CREDIT  +500   │   │
│ │ 2026-02-05  MANUAL_DEBIT    -25    │   │
│ └────────────────────────────────────┘   │
└──────────────────────────────────────────┘
```

### Plan Management
```
Plans → List All
┌─────────────────────────────────────────────┐
│ [+ Create New Plan]                         │
│                                             │
│ Standard Plan (Free)                        │
│ - Commission-based                          │
│ - Basic features                            │
│ [View] [Edit]                               │
│                                             │
│ Professional Plan (200 TND/month)           │
│ - Advanced Analytics                        │
│ - Multi-Store                               │
│ - Custom Themes                             │
│ [View] [Edit] [Deactivate]                  │
└─────────────────────────────────────────────┘
```

### Tenant Subscription Management
```
Tenants → Acme Corp → Subscription
┌──────────────────────────────────────────┐
│ Current Plan: Standard                   │
│ Status: ACTIVE                           │
│ Since: 2026-01-15                        │
│                                          │
│ [Change Plan]                            │
│                                          │
│ Plan History:                            │
│ ┌────────────────────────────────────┐   │
│ │ 2026-01-15  CREATED  Standard      │   │
│ └────────────────────────────────────┘   │
└──────────────────────────────────────────┘
```

---

## ✅ Testing Checklist
- [ ] Create new tenant → wallet created automatically
- [ ] Initial balance = 500 TND (config-driven)
- [ ] Admin can add credits manually
- [ ] Admin can view transaction history
- [ ] Admin can create new subscription plan
- [ ] Admin can assign features to plan
- [ ] Admin can activate premium plan for tenant
- [ ] Subscription history recorded correctly
- [ ] Can view all plans with their features
- [ ] Can deactivate/reactivate plans

---

## 🚀 What This Enables (without orders yet)
1. Test the billing infrastructure end-to-end  
2. Define plans (pricing, features, discounts)  
3. Onboard tenants and assign plans manually  
4. Track subscriptions and history  
5. Manage wallets (credit/debit) for testing

---

## 📝 Next Steps After This
1. Add products/catalog  
2. Add orders  
3. Hook commission deduction on order completion  
4. Add online payment gateway for automatic wallet recharge

For now, this delivers a complete standalone billing stack ready to integrate later.
