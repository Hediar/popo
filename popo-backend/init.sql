-- Initialize PostgreSQL with pgvector extension

-- Create vector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify installation
\dx vector

-- Set timezone
SET timezone = 'Asia/Seoul';
