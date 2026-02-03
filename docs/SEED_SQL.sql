-- Fresh seed data for Digimart (PostgreSQL)
-- Run this after the schema exists (Hibernate ddl-auto=update).

TRUNCATE TABLE
  audit_events,
  refresh_tokens,
  user_role_assignments,
  role_permissions,
  permissions,
  roles,
  users,
  stores,
  tenants
RESTART IDENTITY CASCADE;

-- Permissions (global catalog)
INSERT INTO permissions (code, domain, description)
VALUES
  ('STORE_VIEW', 'STORE', 'View stores'),
  ('STORE_CREATE', 'STORE', 'Create store'),
  ('STORE_UPDATE', 'STORE', 'Update store'),
  ('STORE_DELETE', 'STORE', 'Delete store'),
  ('USER_VIEW', 'USER', 'View users'),
  ('USER_CREATE', 'USER', 'Create users'),
  ('USER_UPDATE', 'USER', 'Update users'),
  ('USER_ROLE_UPDATE', 'USER', 'Update user roles'),
  ('PRODUCT_VIEW', 'PRODUCT', 'View products'),
  ('PRODUCT_CREATE', 'PRODUCT', 'Create products'),
  ('ORDER_VIEW', 'ORDER', 'View orders'),
  ('ORDER_UPDATE', 'ORDER', 'Update orders');

-- Role templates (tenant_id = 0)
INSERT INTO roles (tenant_id, code, label, system_role, created_at, updated_at)
VALUES
  (0, 'TEMPLATE_OWNER', 'Owner Template', true, now(), now()),
  (0, 'TEMPLATE_ADMIN', 'Admin Template', true, now(), now()),
  (0, 'TEMPLATE_MANAGER', 'Manager Template', true, now(), now()),
  (0, 'TEMPLATE_SUPPORT', 'Support Template', true, now(), now()),
  (0, 'TEMPLATE_VENDOR', 'Vendor Template', true, now(), now());

-- Template permissions
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
VALUES
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_DELETE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_ROLE_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_OWNER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_UPDATE'), now(), now()),

  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_ROLE_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_ADMIN' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_UPDATE'), now(), now()),

  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_UPDATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_CREATE'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_MANAGER' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_UPDATE'), now(), now()),

  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_SUPPORT' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_SUPPORT' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'USER_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_SUPPORT' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_SUPPORT' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_VIEW'), now(), now()),

  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_VENDOR' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'STORE_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_VENDOR' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'PRODUCT_VIEW'), now(), now()),
  (0, (SELECT id FROM roles WHERE code = 'TEMPLATE_VENDOR' AND tenant_id = 0),
      (SELECT id FROM permissions WHERE code = 'ORDER_VIEW'), now(), now());

-- Tenants
INSERT INTO tenants (id, name, subdomain, contact_email, contact_phone, logo_url, status, default_locale, created_at, updated_at)
VALUES
  (1, 'Digimart HQ', 'digimart', 'hq@digimart.com', '+21670000000', 'https://i.pravatar.cc/120?img=8', 'ACTIVE', 'FR', now(), now()),
  (2, 'Vendor Alpha', 'alpha', 'owner@alpha.com', '+21671111111', 'https://i.pravatar.cc/120?img=9', 'ACTIVE', 'FR', now(), now()),
  (3, 'Vendor Beta', 'beta', 'owner@beta.com', '+21672222222', 'https://i.pravatar.cc/120?img=7', 'ACTIVE', 'FR', now(), now());

-- Users (passwordHash is raw for now, per app logic)
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, phone, image_url, enabled, created_at, updated_at, last_login)
VALUES
  (1, 1, 'platform@digimart.com', 'Password123!', 'Platform', 'Owner', '+21670000000', 'https://i.pravatar.cc/80?img=10', true, now(), now(), null),
  (2, 2, 'owner@alpha.com', 'Password123!', 'Alpha', 'Owner', '+21671111111', 'https://i.pravatar.cc/80?img=11', true, now(), now(), null),
  (3, 2, 'manager@alpha.com', 'Password123!', 'Alpha', 'Manager', '+21671111112', 'https://i.pravatar.cc/80?img=12', true, now(), now(), null),
  (4, 2, 'support@alpha.com', 'Password123!', 'Alpha', 'Support', '+21671111113', 'https://i.pravatar.cc/80?img=13', true, now(), now(), null),
  (5, 3, 'owner@beta.com', 'Password123!', 'Beta', 'Owner', '+21672222222', 'https://i.pravatar.cc/80?img=14', true, now(), now(), null),
  (6, 3, 'vendor1@beta.com', 'Password123!', 'Beta', 'Vendor', '+21672222223', 'https://i.pravatar.cc/80?img=15', true, now(), now(), null),
  (7, 3, 'vendor2@beta.com', 'Password123!', 'Beta', 'Vendor', '+21672222224', 'https://i.pravatar.cc/80?img=16', true, now(), now(), null);

-- Tenant roles (created by cloning templates)
INSERT INTO roles (tenant_id, code, label, system_role, created_at, updated_at)
VALUES
  (1, 'SUPER_ADMIN', 'Platform Admin', true, now(), now()),
  (1, 'OWNER', 'Tenant Owner', true, now(), now()),
  (1, 'ADMIN', 'Tenant Admin', true, now(), now()),

  (2, 'OWNER', 'Tenant Owner', true, now(), now()),
  (2, 'ADMIN', 'Tenant Admin', true, now(), now()),
  (2, 'MANAGER', 'Manager', false, now(), now()),
  (2, 'SUPPORT', 'Support', false, now(), now()),

  (3, 'OWNER', 'Tenant Owner', true, now(), now()),
  (3, 'ADMIN', 'Tenant Admin', true, now(), now()),
  (3, 'VENDOR', 'Vendor', false, now(), now());

-- Role permissions for tenant roles
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 1, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_CREATE','STORE_UPDATE','STORE_DELETE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_ROLE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 1 AND r.code = 'OWNER';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 2, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_CREATE','STORE_UPDATE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_ROLE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 2 AND r.code = 'ADMIN';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 2, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_CREATE','STORE_UPDATE','STORE_DELETE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_ROLE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 2 AND r.code = 'OWNER';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 2, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 2 AND r.code = 'MANAGER';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 2, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','USER_VIEW','PRODUCT_VIEW','ORDER_VIEW')
WHERE r.tenant_id = 2 AND r.code = 'SUPPORT';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 3, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_CREATE','STORE_UPDATE','STORE_DELETE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_ROLE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 3 AND r.code = 'OWNER';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 3, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','STORE_CREATE','STORE_UPDATE','USER_VIEW','USER_CREATE','USER_UPDATE','USER_ROLE_UPDATE','PRODUCT_VIEW','PRODUCT_CREATE','ORDER_VIEW','ORDER_UPDATE')
WHERE r.tenant_id = 3 AND r.code = 'ADMIN';

INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_at, updated_at)
SELECT 3, r.id, p.id, now(), now()
FROM roles r
JOIN permissions p ON p.code IN ('STORE_VIEW','PRODUCT_VIEW','ORDER_VIEW')
WHERE r.tenant_id = 3 AND r.code = 'VENDOR';

-- User role assignments
INSERT INTO user_role_assignments (tenant_id, user_id, role_id, active, created_at, updated_at)
VALUES
  (1, 1, (SELECT id FROM roles WHERE tenant_id = 1 AND code = 'SUPER_ADMIN'), true, now(), now()),
  (1, 1, (SELECT id FROM roles WHERE tenant_id = 1 AND code = 'OWNER'), true, now(), now()),
  (1, 1, (SELECT id FROM roles WHERE tenant_id = 1 AND code = 'ADMIN'), true, now(), now()),

  (2, 2, (SELECT id FROM roles WHERE tenant_id = 2 AND code = 'OWNER'), true, now(), now()),
  (2, 3, (SELECT id FROM roles WHERE tenant_id = 2 AND code = 'MANAGER'), true, now(), now()),
  (2, 4, (SELECT id FROM roles WHERE tenant_id = 2 AND code = 'SUPPORT'), true, now(), now()),

  (3, 5, (SELECT id FROM roles WHERE tenant_id = 3 AND code = 'OWNER'), true, now(), now()),
  (3, 6, (SELECT id FROM roles WHERE tenant_id = 3 AND code = 'VENDOR'), true, now(), now()),
  (3, 7, (SELECT id FROM roles WHERE tenant_id = 3 AND code = 'VENDOR'), true, now(), now());

-- Stores (Point de Vente)
INSERT INTO stores (tenant_id, name, code, address, city, postal_code, country, phone, email, image_url, latitude, longitude, active, created_at, updated_at)
VALUES
  (2, 'Alpha Centre', 'ALP-01', '1 Rue Alpha', 'Tunis', '1000', 'TN', '+21670000001', 'alpha@digimart.com', null, 36.8065, 10.1815, true, now(), now()),
  (2, 'Alpha Sousse', 'ALP-02', '2 Rue Alpha', 'Sousse', '4000', 'TN', '+21670000002', 'alpha2@digimart.com', null, 35.8256, 10.6084, true, now(), now()),
  (3, 'Beta Ariana', 'BET-01', 'Zone Ariana', 'Ariana', '2000', 'TN', '+21670000003', 'beta@digimart.com', null, 36.8665, 10.1647, true, now(), now());

-- Refresh tokens (hashes are random samples; not used by login)
INSERT INTO refresh_tokens (tenant_id, user_id, token_hash, expires_at, revoked_at, device_info, created_at, updated_at)
VALUES
  (1, 1, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', now() + interval '7 days', null, 'seeded-device', now(), now());

-- Audit events
INSERT INTO audit_events (tenant_id, entity_type, entity_id, action, before_json, after_json, actor_user_id, ip_address, user_agent, correlation_id, occurred_at, created_at, updated_at)
VALUES
  (1, 'Tenant', 1, 'CREATE', null, '{"name":"Digimart HQ"}', 1, '127.0.0.1', 'seed-script', 'corr-001', now(), now(), now()),
  (2, 'User', 2, 'CREATE', null, '{"email":"owner@alpha.com"}', 2, '127.0.0.1', 'seed-script', 'corr-002', now(), now(), now());
