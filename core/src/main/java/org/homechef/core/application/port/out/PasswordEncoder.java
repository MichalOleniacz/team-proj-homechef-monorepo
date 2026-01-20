package org.homechef.core.application.port.out;

import org.homechef.core.domain.user.HashedPassword;

/**
 * Output port for password encoding operations.
 * Abstracts the actual hashing algorithm from the application layer.
 */
public interface PasswordEncoder {

    /**
     * Encodes a raw password into a hashed password.
     */
    HashedPassword encode(String rawPassword);

    /**
     * Verifies a raw password matches a hashed password.
     */
    boolean matches(String rawPassword, HashedPassword encoded);
}
