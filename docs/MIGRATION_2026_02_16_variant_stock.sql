-- Migration: variant per-store stock + track_stock on variants (PostgreSQL)
-- Run manually if not using Flyway/Liquibase.

ALTER TABLE product_variants
  ADD COLUMN IF NOT EXISTS track_stock BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE product_variants
  ADD COLUMN IF NOT EXISTS initial_price_override NUMERIC,
  ADD COLUMN IF NOT EXISTS final_price_override NUMERIC,
  ADD COLUMN IF NOT EXISTS cost_price_override NUMERIC,
  ADD COLUMN IF NOT EXISTS shipping_price_override NUMERIC,
  ADD COLUMN IF NOT EXISTS shipping_cost_price_override NUMERIC;

-- Safety: normalize existing rows if column already existed without default
UPDATE product_variants
SET track_stock = FALSE
WHERE track_stock IS NULL;

-- Backfill final_price_override from legacy price_override
UPDATE product_variants
SET final_price_override = price_override
WHERE final_price_override IS NULL
  AND price_override IS NOT NULL;

CREATE TABLE IF NOT EXISTS variant_store_inventory (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  quantity INTEGER NOT NULL DEFAULT 0,
  low_stock_threshold INTEGER,
  is_active_in_store BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_variant_store_inventory'
  ) THEN
    ALTER TABLE variant_store_inventory
      ADD CONSTRAINT uq_variant_store_inventory
      UNIQUE (tenant_id, variant_id, store_id);
  END IF;
END $$;

-- Optional but useful for lookup speed
CREATE INDEX IF NOT EXISTS idx_variant_store_inventory_variant
  ON variant_store_inventory (variant_id);

CREATE INDEX IF NOT EXISTS idx_variant_store_inventory_store
  ON variant_store_inventory (store_id);
