package org.homechef.core.domain.user;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a validated email address.
 * Immutable. Stored in lowercase, trimmed.
 */
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        value = value.toLowerCase().trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }
}
