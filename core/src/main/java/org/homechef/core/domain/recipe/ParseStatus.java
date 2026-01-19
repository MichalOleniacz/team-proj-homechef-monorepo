package org.homechef.core.domain.recipe;

/**
 * Lifecycle states for a ParseRequest.
 */
public enum ParseStatus {

    /**
     * Request created, Kafka event emitted, awaiting processing.
     */
    PENDING,

    /**
     * Downstream scraper/LLM has picked up the request.
     */
    PROCESSING,

    /**
     * Parsing completed successfully. Recipe is available.
     */
    COMPLETED,

    /**
     * Parsing failed. Error message available.
     */
    FAILED;

    /**
     * Returns true if this status represents an in-flight request (not terminal).
     */
    public boolean isInFlight() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * Returns true if this status is terminal (no further transitions).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}