package org.homechef.core.application.port.in.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Command for user login.
 */
public record LoginCommand(
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {}
