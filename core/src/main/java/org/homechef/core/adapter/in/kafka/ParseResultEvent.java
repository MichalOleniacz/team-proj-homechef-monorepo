package org.homechef.core.adapter.in.kafka;

import java.util.List;
import java.util.UUID;

/**
 * Kafka event payload for parsed results from LLM connector.
 */
public record ParseResultEvent(
        UUID requestId,
        String urlHash,
        boolean success,
        String errorMessage,      // present if success=false
        String title,             // present if success=true
        List<IngredientEvent> ingredients  // present if success=true
) {
    public record IngredientEvent(
            String quantity,
            String unit,
            String name
    ) {}
}