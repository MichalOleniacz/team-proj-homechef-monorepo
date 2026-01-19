package org.homechef.core.application.port.out;

import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.UrlHash;

import java.util.Optional;
import java.util.UUID;

/**
 * Driven port for ParseRequest persistence.
 */
public interface ParseRequestRepository {

    /**
     * Saves a new parse request or updates an existing one.
     */
    ParseRequest save(ParseRequest parseRequest);

    /**
     * Finds a parse request by ID.
     */
    Optional<ParseRequest> findById(UUID id);

    /**
     * Finds an in-flight (PENDING or PROCESSING) request for the given URL hash.
     * Used for deduplication.
     */
    Optional<ParseRequest> findInFlightByUrlHash(UrlHash urlHash);

    /**
     * Updates the status of a parse request.
     */
    void updateStatus(UUID id, String status, String errorMessage);
}