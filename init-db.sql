-- Database initialization script for Merchant API
-- This file is automatically executed when PostgreSQL container starts for the first time

-- Ensure the database exists (already created by POSTGRES_DB env var)
-- Grant all privileges to merchant user
GRANT ALL PRIVILEGES ON DATABASE merchantdb TO merchant;

-- Connect to the database
\c merchantdb;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO merchant;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO merchant;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO merchant;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO merchant;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO merchant;

-- Create extension for UUID generation (if needed)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Log successful initialization
SELECT 'Database initialized successfully for merchant user' AS status;