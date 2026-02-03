-- Permission catalog + role templates (tenant_id = 0)
-- Safe to re-run.

TRUNCATE TABLE
  role_permissions,
  roles,
  permissions
RESTART IDENTITY CASCADE;

-- Permissions
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
