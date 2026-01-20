package org.homechef.core.adapter.in.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class to extract the currently authenticated user from SecurityContext.
 */
public record AuthenticatedUser(UUID userId) {

    /**
     * Gets the currently authenticated user, if any.
     *
     * @return the authenticated user, or empty if anonymous
     */
    public static Optional<AuthenticatedUser> current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            return Optional.of(new AuthenticatedUser(userId));
        }
        return Optional.empty();
    }

    /**
     * Gets the current user ID, or null if anonymous.
     * Convenient for places that accept nullable userId.
     */
    public static UUID currentUserIdOrNull() {
        return current().map(AuthenticatedUser::userId).orElse(null);
    }
}
