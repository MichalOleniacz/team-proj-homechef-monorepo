package org.homechef.core.application.port.out;

import org.homechef.core.domain.user.User;
import org.homechef.core.domain.user.UserId;

import java.util.Optional;

/**
 * Output port for JWT token operations.
 */
public interface TokenProvider {

    /**
     * Generates a JWT token for the given user.
     */
    String generateToken(User user);

    /**
     * Validates a token and extracts the user ID.
     *
     * @return the user ID if token is valid, empty otherwise
     */
    Optional<UserId> validateToken(String token);

    /**
     * Returns the token expiration time in seconds.
     */
    long getExpirationSeconds();
}
