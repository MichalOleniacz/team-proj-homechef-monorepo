package org.homechef.core.domain.user;

import java.util.Objects;

/**
 * Value Object representing a hashed password.
 * Never stores raw passwords. Immutable.
 */
public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "Hashed password cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Hashed password cannot be blank");
        }
    }

    public static HashedPassword of(String hashedValue) {
        return new HashedPassword(hashedValue);
    }
}
