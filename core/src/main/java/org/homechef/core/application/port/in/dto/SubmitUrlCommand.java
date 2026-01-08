package org.homechef.core.application.port.in.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command to submit a URL for parsing.
 */
public record SubmitUrlCommand(
        String url,
        UUID userId
) {
    public SubmitUrlCommand {
        Objects.requireNonNull(url, "url cannot be null");
        if (url.isBlank()) {
            throw new IllegalArgumentException("url cannot be blank");
        }
    }

    public static SubmitUrlCommand forGuest(String url) {
        return new SubmitUrlCommand(url, null);
    }

    public static SubmitUrlCommand forUser(String url, UUID userId) {
        return new SubmitUrlCommand(url, userId);
    }
}