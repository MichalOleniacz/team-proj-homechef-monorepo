package org.homechef.core.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * REST request body for user login.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
