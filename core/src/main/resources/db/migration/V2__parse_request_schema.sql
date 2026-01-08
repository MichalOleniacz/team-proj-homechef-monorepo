-- V2: Parse request flow schema (ADR-002)

-- URL registry: tracks known URLs
CREATE TABLE resource (
    url_hash    VARCHAR(64) PRIMARY KEY,
    url         TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE resource IS 'Registry of known recipe URLs, keyed by SHA-256 hash';
COMMENT ON COLUMN resource.url_hash IS 'SHA-256 hash of normalized URL (64 hex chars)';

-- Parsed recipe: 1:1 with resource, stores LLM extraction result
CREATE TABLE recipe (
    url_hash    VARCHAR(64) PRIMARY KEY REFERENCES resource(url_hash) ON DELETE CASCADE,
    title       VARCHAR(500),
    ingredients JSONB NOT NULL DEFAULT '[]'::jsonb,
    parsed_at   TIMESTAMPTZ NOT NULL
);

COMMENT ON TABLE recipe IS 'Parsed recipe data, updated in-place on re-parse';
COMMENT ON COLUMN recipe.ingredients IS 'Array of {quantity, unit, name} objects';
COMMENT ON COLUMN recipe.parsed_at IS 'TTL anchor: recipe stale if parsed_at < now - 30d';

-- Parse request: tracks lifecycle of each parsing attempt
CREATE TABLE parse_request (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    url_hash        VARCHAR(64) NOT NULL REFERENCES resource(url_hash) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT parse_request_valid_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')
    )
);

COMMENT ON TABLE parse_request IS 'Lifecycle tracking for parse requests';
COMMENT ON COLUMN parse_request.user_id IS 'NULL for guest users';
COMMENT ON COLUMN parse_request.status IS 'State machine: PENDING -> PROCESSING -> COMPLETED|FAILED';

-- Index: dedup query - find in-flight requests for a URL
CREATE INDEX idx_parse_request_dedup
    ON parse_request (url_hash, status)
    WHERE status IN ('PENDING', 'PROCESSING');

-- Index: user history (Phase 2)
CREATE INDEX idx_parse_request_user_history
    ON parse_request (user_id, created_at DESC)
    WHERE user_id IS NOT NULL;

-- Index: recipe freshness check
CREATE INDEX idx_recipe_freshness
    ON recipe (url_hash, parsed_at);