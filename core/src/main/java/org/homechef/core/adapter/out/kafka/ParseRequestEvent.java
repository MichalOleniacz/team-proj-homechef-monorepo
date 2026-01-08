package org.homechef.core.adapter.out.kafka;

import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event payload for parse requests.
 * Published to trigger downstream scraping/LLM processing.
 */
public record ParseRequestEvent(
        UUID requestId,
        String url,
        String urlHash,
        Instant requestedAt
) {
}