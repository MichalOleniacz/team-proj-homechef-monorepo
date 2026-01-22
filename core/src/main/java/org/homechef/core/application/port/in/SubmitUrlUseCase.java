package org.homechef.core.application.port.in;

import org.homechef.core.application.port.in.dto.SubmitUrlCommand;
import org.homechef.core.application.port.in.dto.SubmitUrlResult;

/**
 * Driving port for submitting a URL for parsing.
 */
public interface SubmitUrlUseCase {

    /**
     * Submits a URL for ingredient extraction.
     *
     * Returns immediately with either:
     * - Cached recipe (if fresh)
     * - Existing request ID (if in-flight request exists - dedup)
     * - New request ID (if parsing triggered)
     */
    SubmitUrlResult execute(SubmitUrlCommand command);
}