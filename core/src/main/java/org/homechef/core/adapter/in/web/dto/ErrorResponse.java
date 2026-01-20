package org.homechef.core.adapter.in.web.dto;

import java.time.Instant;

/**
 * Standard error response format.
 */
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}
