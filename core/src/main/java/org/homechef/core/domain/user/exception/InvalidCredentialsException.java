package org.homechef.core.domain.user.exception;

/**
 * Thrown when authentication fails due to invalid email or password.
 * Intentionally vague to prevent user enumeration attacks.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
