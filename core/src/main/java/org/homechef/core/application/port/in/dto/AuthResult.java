package org.homechef.core.application.port.in.dto;

import java.util.UUID;

/**
 * Result of successful authentication or registration.
 */
public record AuthResult(
    UUID userId,
    String email,
    String accessToken,
    long expiresIn
) {}
