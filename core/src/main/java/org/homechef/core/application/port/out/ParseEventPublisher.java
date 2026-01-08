package org.homechef.core.application.port.out;

import org.homechef.core.domain.recipe.ParseRequest;

/**
 * Driven port for publishing parse request events to message broker.
 */
public interface ParseEventPublisher {

    /**
     * Publishes a parse request event to trigger downstream scraping/LLM processing.
     * Event payload: {requestId, url, urlHash}
     */
    void publishParseRequest(ParseRequest parseRequest, String url);
}