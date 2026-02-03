-- Seed data for NexaShop core tables (PostgreSQL)
-- Run this after the schema exists (Hibernate ddl-auto=update).
--
-- TRUNCATE order matters due to FK constraints
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

-- Tenants
INSERT INTO tenants (id, name, subdomain, contact_email, contact_phone, status, default_locale, created_at, updated_at)
VALUES
  (1, 'Demo Tenant', 'demo', 'owner@demo.com', '+212600000000', 'ACTIVE', 'FR', now(), now()),
  (2, 'Trial Tenant', 'trial', 'owner@trial.com', '+212611111111', 'TRIAL', 'EN', now(), now()),
  (3, 'Suspended Tenant', 'suspended', 'owner@sus.com', '+212622222222', 'SUSPENDED', 'AR', now(), now());

-- Users (passwordHash is raw for now, per app logic)
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, enabled, created_at, updated_at, last_login)
VALUES
  (1, 1, 'admin@demo.com', 'Password123!', 'Admin', 'User', true, now(), now(), null),
  (2, 1, 'staff@demo.com', 'Password123!', 'Staff', 'User', true, now(), now(), null),
  (3, 2, 'owner@trial.com', 'Password123!', 'Trial', 'Owner', true, now(), now(), null),
  (4, 3, 'owner@sus.com', 'Password123!', 'Suspended', 'Owner', false, now(), now(), null);

-- Stores (Point de Vente)
INSERT INTO stores (id, tenant_id, name, code, address, city, postal_code, country, phone, email, image_url, latitude, longitude, active, created_at, updated_at)
VALUES
  (1, 1, 'Store Casablanca', 'CASA-01', '1 Rue Exemple', 'Casablanca', '20000', 'MA', '+212600000000', 'store@demo.com', 'https://example.com/store.png', 33.5731, -7.5898, true, now(), now()),
  (2, 1, 'Store Rabat', 'RAB-01', '10 Avenue Test', 'Rabat', '10000', 'MA', '+212600000001', 'rabat@demo.com', null, 34.0209, -6.8416, true, now(), now()),
  (3, 2, 'Trial Store', 'TRY-01', '2 Trial Road', 'Rabat', '10010', 'MA', '+212611111111', 'trial@demo.com', null, 34.0209, -6.8416, true, now(), now());

-- Roles
INSERT INTO roles (id, tenant_id, code, label, system_role, created_at, updated_at)
VALUES
  (1, 1, 'SUPER_ADMIN', 'Platform Admin', true, now(), now()),
  (2, 1, 'OWNER', 'Tenant Owner', true, now(), now()),
  (3, 1, 'ADMIN', 'Tenant Admin', true, now(), now()),
  (4, 2, 'OWNER', 'Tenant Owner', true, now(), now()),
  (5, 3, 'OWNER', 'Tenant Owner', true, now(), now());

-- Permissions
INSERT INTO permissions (id, code, domain, description)
VALUES
  (1, 'TENANT_READ', 'TENANT', 'Read tenant data'),
  (2, 'TENANT_WRITE', 'TENANT', 'Update tenant data'),
  (3, 'USER_READ', 'USER', 'Read user data'),
  (4, 'USER_WRITE', 'USER', 'Update user data');

-- Role permissions
INSERT INTO role_permissions (id, tenant_id, role_id, permission_id, created_at, updated_at)
VALUES
  (1, 1, 1, 1, now(), now()),
  (2, 1, 1, 2, now(), now()),
  (3, 1, 1, 3, now(), now()),
  (4, 1, 1, 4, now(), now()),
  (5, 1, 2, 1, now(), now()),
  (6, 1, 2, 2, now(), now()),
  (7, 1, 2, 3, now(), now()),
  (8, 1, 2, 4, now(), now()),
  (9, 1, 3, 1, now(), now()),
  (10, 1, 3, 3, now(), now()),
  (11, 2, 4, 1, now(), now()),
  (12, 2, 4, 2, now(), now()),
  (13, 2, 4, 3, now(), now()),
  (14, 2, 4, 4, now(), now()),
  (15, 3, 5, 1, now(), now()),
  (16, 3, 5, 2, now(), now()),
  (17, 3, 5, 3, now(), now()),
  (18, 3, 5, 4, now(), now());

-- User role assignments
INSERT INTO user_role_assignments (id, tenant_id, user_id, role_id, active, created_at, updated_at)
VALUES
  (1, 1, 1, 1, true, now(), now()), -- SUPER_ADMIN
  (2, 1, 1, 2, true, now(), now()), -- OWNER
  (3, 1, 1, 3, true, now(), now()), -- ADMIN
  (4, 1, 2, 3, true, now(), now()), -- ADMIN
  (5, 2, 3, 4, true, now(), now()), -- OWNER
  (6, 3, 4, 5, true, now(), now()); -- OWNER

-- Refresh tokens (hashes are random samples; not used by login)
INSERT INTO refresh_tokens (id, tenant_id, user_id, token_hash, expires_at, revoked_at, device_info, created_at, updated_at)
VALUES
  (1, 1, 1, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', now() + interval '7 days', null, 'seeded-device', now(), now()),
  (2, 1, 2, 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', now() + interval '7 days', null, 'seeded-device', now(), now());

-- Audit events
INSERT INTO audit_events (id, tenant_id, entity_type, entity_id, action, before_json, after_json, actor_user_id, ip_address, user_agent, correlation_id, occurred_at, created_at, updated_at)
VALUES
  (1, 1, 'Tenant', 1, 'CREATE', null, '{"name":"Demo Tenant"}', 1, '127.0.0.1', 'seed-script', 'corr-001', now(), now(), now()),
  (2, 1, 'User', 1, 'CREATE', null, '{"email":"admin@demo.com"}', 1, '127.0.0.1', 'seed-script', 'corr-002', now(), now(), now());
