-- Migration: activity sectors + tenant sector_id (PostgreSQL)
-- Run manually if not using Flyway/Liquibase.

CREATE TABLE IF NOT EXISTS activity_sectors (
  id BIGSERIAL PRIMARY KEY,
  label VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

ALTER TABLE activity_sectors
  ADD CONSTRAINT IF NOT EXISTS uq_activity_sectors_label UNIQUE (label);

ALTER TABLE tenants
  ADD COLUMN IF NOT EXISTS sector_id BIGINT;

ALTER TABLE tenants
  ADD CONSTRAINT IF NOT EXISTS fk_tenants_sector
  FOREIGN KEY (sector_id) REFERENCES activity_sectors (id);
