-- Ensure the role check constraint supports all declared roles.
-- This script runs on startup and is safe to rerun (uses IF EXISTS / IF NOT EXISTS).

ALTER TABLE IF EXISTS users
  DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE IF EXISTS users
  ADD CONSTRAINT users_role_check CHECK (role IN ('ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMIN'));

-- Add is_first_login column if it doesn't exist, default TRUE for existing rows
ALTER TABLE IF EXISTS users
  ADD COLUMN IF NOT EXISTS is_first_login BOOLEAN NOT NULL DEFAULT TRUE;

-- Create employee_project_score table to store individual employee scores for projects
CREATE TABLE IF NOT EXISTS employee_project_score (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    employee_name VARCHAR(255) NOT NULL,
    overall_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    direct_skill_score DOUBLE PRECISION,
    related_skill_score DOUBLE PRECISION,
    semantic_skill_score DOUBLE PRECISION,
    matched_skills VARCHAR(3000),
    missing_skills VARCHAR(3000),
    full_match_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_project_employee ON employee_project_score(project_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_project_created ON employee_project_score(project_id, created_at);
CREATE INDEX IF NOT EXISTS idx_employee_id ON employee_project_score(employee_id);
CREATE INDEX IF NOT EXISTS idx_overall_score ON employee_project_score(project_id, overall_score DESC);

