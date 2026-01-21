package org.homechef.core.domain.user.exception;

import org.homechef.core.domain.user.Email;

/**
 * Thrown when attempting to register a user with an email that already exists.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(Email email) {
        super("Email already registered: " + email.value());
        this.email = email.value();
    }

    public String getEmail() {
        return email;
    }
}
