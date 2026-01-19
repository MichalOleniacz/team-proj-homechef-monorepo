# ADR-002: Parse Request Flow and Data Model

**Status:** Accepted
**Date:** 2025-01-08
**Deciders:** Michal Oleniacz

## Context

HomeChef Core receives recipe URLs from clients and orchestrates async parsing via Kafka. We need to define:

1. **Data model** for tracking URLs, parsed recipes, and request lifecycle
2. **Request flow** with caching and deduplication
3. **State transitions** for the parsing lifecycle

Key requirements:
- Cache parsed recipes with TTL-based invalidation (~30 days)
- Deduplicate concurrent requests for the same URL
- Support async polling for parse status
- Guest users (no auth) for PoC phase

## Decision

### Data Model

Three tables with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│ resource                                                     │
├─────────────────────────────────────────────────────────────┤
│ url_hash        VARCHAR(64) PK     -- SHA-256 of normalized │
│ url             TEXT NOT NULL      -- original URL           │
│ created_at      TIMESTAMPTZ                                  │
└─────────────────────────────────────────────────────────────┘
         │
         │ 1:1 (recipe may not exist yet)
         ▼
┌─────────────────────────────────────────────────────────────┐
│ recipe                                                       │
├─────────────────────────────────────────────────────────────┤
│ url_hash        VARCHAR(64) PK FK  -- same as resource       │
│ title           VARCHAR(500)       -- extracted from page    │
│ ingredients     JSONB              -- [{qty, unit, name}]    │
│ parsed_at       TIMESTAMPTZ        -- TTL anchor             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ parse_request                                                │
├─────────────────────────────────────────────────────────────┤
│ id              UUID PK                                      │
│ user_id         UUID NULL          -- NULL for guests        │
│ url_hash        VARCHAR(64) FK     -- references resource    │
│ status          VARCHAR(20)        -- lifecycle state        │
│ error_message   TEXT NULL          -- failure reason         │
│ created_at      TIMESTAMPTZ                                  │
│ updated_at      TIMESTAMPTZ                                  │
└─────────────────────────────────────────────────────────────┘
```

**Design rationale:**

| Table | Purpose |
|-------|---------|
| `resource` | URL registry. Immutable once created. Natural key = url_hash. |
| `recipe` | Parsed result. 1:1 with resource. Updated in-place on re-parse. `parsed_at` drives TTL. |
| `parse_request` | Request lifecycle tracking. Multiple requests can reference same resource. |

### Request Submission Flow

```
                    ┌──────────────────────────────────────────────┐
                    │             User submits URL                  │
                    └──────────────────────┬───────────────────────┘
                                           │
                                           ▼
                              ┌────────────────────────┐
                              │   Hash URL (SHA-256)   │
                              └────────────┬───────────┘
                                           │
                                           ▼
                    ┌──────────────────────────────────────────────┐
                    │  Recipe exists AND parsed_at > now - 30d?    │
                    └──────────────────────┬───────────────────────┘
                                           │
                         ┌─────────────────┴─────────────────┐
                         │ YES                               │ NO
                         ▼                                   ▼
              ┌─────────────────────┐      ┌────────────────────────────────┐
              │   Return Recipe     │      │  ParseRequest exists with      │
              │   immediately       │      │  status IN (PENDING,PROCESSING)│
              │                     │      │  for this url_hash?            │
              │   HTTP 200          │      └───────────────┬────────────────┘
              │   {recipe: {...}}   │                      │
              └─────────────────────┘        ┌─────────────┴─────────────┐
                                             │ YES                       │ NO
                                             ▼                           ▼
                              ┌─────────────────────┐     ┌─────────────────────┐
                              │  Return existing    │     │  Upsert Resource    │
                              │  request_id         │     │  (if not exists)    │
                              │                     │     └──────────┬──────────┘
                              │  HTTP 202           │                │
                              │  {requestId: "..."}│                ▼
                              │  (DEDUP)           │     ┌─────────────────────┐
                              └─────────────────────┘     │  Create ParseRequest│
                                                         │  status = PENDING   │
                                                         └──────────┬──────────┘
                                                                    │
                                                                    ▼
                                                         ┌─────────────────────┐
                                                         │  Emit Kafka Event   │
                                                         │  {requestId, url}   │
                                                         └──────────┬──────────┘
                                                                    │
                                                                    ▼
                                                         ┌─────────────────────┐
                                                         │  Return request_id  │
                                                         │                     │
                                                         │  HTTP 202           │
                                                         │  {requestId: "..."}│
                                                         └─────────────────────┘
```

**Flow summary:**

1. **Cache HIT** (fresh recipe exists): Return recipe immediately. No ParseRequest created.
2. **Dedup** (in-flight request exists): Return existing request_id. No Kafka emit.
3. **Cache MISS/STALE**: Create ParseRequest, emit Kafka event, return request_id.

### Polling Flow

```
              ┌─────────────────────────────────────────┐
              │  GET /parse-requests/{requestId}        │
              └────────────────────┬────────────────────┘
                                   │
                                   ▼
              ┌─────────────────────────────────────────┐
              │  Lookup ParseRequest by ID              │
              └────────────────────┬────────────────────┘
                                   │
                 ┌─────────────────┼─────────────────┬─────────────────┐
                 │                 │                 │                 │
                 ▼                 ▼                 ▼                 ▼
          ┌───────────┐     ┌───────────┐     ┌───────────┐     ┌───────────┐
          │  PENDING  │     │PROCESSING │     │ COMPLETED │     │  FAILED   │
          └─────┬─────┘     └─────┬─────┘     └─────┬─────┘     └─────┬─────┘
                │                 │                 │                 │
                ▼                 ▼                 ▼                 ▼
          ┌───────────┐     ┌───────────┐     ┌───────────┐     ┌───────────┐
          │ 200       │     │ 200       │     │ 200       │     │ 200       │
          │ {status:  │     │ {status:  │     │ {status:  │     │ {status:  │
          │  PENDING} │     │PROCESSING}│     │ COMPLETED,│     │  FAILED,  │
          └───────────┘     └───────────┘     │  recipe:  │     │  error:   │
                                              │  {...}}   │     │  "..."}   │
                                              └───────────┘     └───────────┘
```

### ParseRequest State Machine

```
                              ┌─────────────┐
                              │   PENDING   │
                              │             │
                              │ Initial     │
                              │ state on    │
                              │ creation    │
                              └──────┬──────┘
                                     │
                                     │ Kafka consumer picks up
                                     │ (downstream scraper starts)
                                     ▼
                              ┌─────────────┐
                              │ PROCESSING  │
                              │             │
                              │ Scraping &  │
                              │ LLM parsing │
                              │ in progress │
                              └──────┬──────┘
                                     │
                   ┌─────────────────┴─────────────────┐
                   │                                   │
                   │ Success                           │ Failure
                   ▼                                   ▼
            ┌─────────────┐                     ┌─────────────┐
            │  COMPLETED  │                     │   FAILED    │
            │             │                     │             │
            │ Recipe      │                     │ error_msg   │
            │ upserted    │                     │ populated   │
            └─────────────┘                     └─────────────┘
                   │                                   │
                   │                                   │
                   └───────────────┬───────────────────┘
                                   │
                                   ▼
                          (Terminal states)

                          User can retry by
                          submitting URL again
                          (creates new ParseRequest)
```

**State transitions:**

| From | To | Trigger | Side Effects |
|------|----|---------|--------------|
| - | PENDING | New request created | Kafka event emitted |
| PENDING | PROCESSING | Downstream consumer starts | updated_at set |
| PROCESSING | COMPLETED | LLM returns success | Recipe upserted, updated_at set |
| PROCESSING | FAILED | LLM returns error | error_message set, updated_at set |

### API Response Matrix

| Scenario | HTTP | Response Body |
|----------|------|---------------|
| Fresh recipe exists | 200 | `{recipe: {title, ingredients, parsedAt}}` |
| In-flight request (dedup) | 202 | `{requestId, status: "PENDING\|PROCESSING"}` |
| New request created | 202 | `{requestId, status: "PENDING"}` |
| Poll - pending | 200 | `{requestId, status: "PENDING"}` |
| Poll - processing | 200 | `{requestId, status: "PROCESSING"}` |
| Poll - completed | 200 | `{requestId, status: "COMPLETED", recipe: {...}}` |
| Poll - failed | 200 | `{requestId, status: "FAILED", error: "..."}` |
| Poll - not found | 404 | `{error: "Request not found"}` |

### Database Schema (DDL)

```sql
-- URL registry
CREATE TABLE resource (
    url_hash        VARCHAR(64) PRIMARY KEY,
    url             TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Parsed recipe (1:1 with resource)
CREATE TABLE recipe (
    url_hash        VARCHAR(64) PRIMARY KEY REFERENCES resource(url_hash),
    title           VARCHAR(500),
    ingredients     JSONB NOT NULL,
    parsed_at       TIMESTAMPTZ NOT NULL
);

-- Request lifecycle tracking
CREATE TABLE parse_request (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    url_hash        VARCHAR(64) NOT NULL REFERENCES resource(url_hash),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT valid_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')
    )
);

-- Dedup query optimization: find in-flight requests
CREATE INDEX idx_parse_request_dedup
    ON parse_request (url_hash, status)
    WHERE status IN ('PENDING', 'PROCESSING');

-- User history (Phase 2)
CREATE INDEX idx_parse_request_user
    ON parse_request (user_id, created_at DESC)
    WHERE user_id IS NOT NULL;

-- Recipe freshness check
CREATE INDEX idx_recipe_freshness
    ON recipe (url_hash, parsed_at);
```

### Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `recipe.ttl.days` | 30 | Recipe considered stale after this period |
| `kafka.topic.parse-request` | `parse-requests` | Outbound topic for new requests |
| `kafka.topic.parse-result` | `parse-results` | Inbound topic for LLM results |

## Consequences

### Positive

1. **Efficient caching**: Fresh recipes served immediately, no redundant parsing
2. **Deduplication**: Concurrent requests don't spam Kafka
3. **Clear separation**: Resource (URL registry) vs Recipe (parsed data) vs ParseRequest (lifecycle)
4. **Audit trail**: ParseRequest history for debugging and user history (Phase 2)
5. **Stateless Core**: All state in Postgres, horizontal scaling possible

### Negative

1. **Polling overhead**: Clients must poll for async results (WebSocket upgrade in future)
2. **Stale reads possible**: TTL-based invalidation means slightly outdated recipes
3. **No versioning**: In-place recipe updates lose history (acceptable for PoC)

### Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Kafka unavailable | ParseRequest stays PENDING; add retry/DLQ |
| LLM timeout | PROCESSING → FAILED with timeout error; client retries |
| Hash collision | SHA-256 collision probability negligible (~10^-60) |
| Orphaned PROCESSING requests | Scheduled job to timeout stale PROCESSING (>15min) |

## References

- ADR-001: Hexagonal Architecture and DDD
- Kafka integration patterns: Request-Reply vs Fire-and-Forget (we use async polling)