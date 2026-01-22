-- User authentication schema
CREATE TABLE app_user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_user_email ON app_user (email);

-- Add foreign key constraint to parse_request (soft reference, allows NULL for guests)
-- Note: Not adding FK constraint to avoid breaking guest user flow
-- Users can be deleted without cascading to their parse history
