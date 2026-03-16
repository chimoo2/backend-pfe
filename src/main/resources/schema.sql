-- Ensure the role check constraint supports all declared roles.
-- This script runs on startup and is safe to rerun (uses IF EXISTS / IF NOT EXISTS).

ALTER TABLE IF EXISTS users
  DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE IF EXISTS users
  ADD CONSTRAINT users_role_check CHECK (role IN ('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMIN'));
