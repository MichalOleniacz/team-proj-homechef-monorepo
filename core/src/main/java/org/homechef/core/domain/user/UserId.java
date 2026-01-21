package org.homechef.core.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a User's unique identifier.
 * Immutable. Equality by UUID value.
 */
public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "UserId value cannot be null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId fromString(String value) {
        Objects.requireNonNull(value, "UserId string cannot be null");
        return new UserId(UUID.fromString(value));
    }
}
