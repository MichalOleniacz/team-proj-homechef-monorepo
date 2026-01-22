package org.homechef.core.adapter.in.web.dto;

import org.homechef.core.application.port.in.dto.AuthResult;

import java.util.UUID;

/**
 * REST response for authentication (login/register).
 */
public record AuthResponse(
        UUID userId,
        String email,
        String accessToken,
        long expiresIn
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.userId(),
                result.email(),
                result.accessToken(),
                result.expiresIn()
        );
    }
}
