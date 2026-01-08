package org.homechef.core.application.port.in;

import org.homechef.core.application.port.in.dto.ParseStatusResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Driving port for polling parse request status.
 */
public interface GetParseStatusUseCase {

    /**
     * Gets the current status of a parse request.
     * Returns empty if request not found.
     */
    Optional<ParseStatusResult> execute(UUID requestId);
}