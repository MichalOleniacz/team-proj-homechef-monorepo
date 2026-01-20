package org.homechef.core.application.port.in;

import org.homechef.core.application.port.in.dto.AuthResult;
import org.homechef.core.application.port.in.dto.RegisterCommand;

/**
 * Use case for registering a new user.
 */
public interface RegisterUserUseCase {

    /**
     * Registers a new user with email and password.
     *
     * @param command the registration command
     * @return auth result with JWT token
     * @throws org.homechef.core.domain.user.exception.EmailAlreadyExistsException if email is taken
     */
    AuthResult register(RegisterCommand command);
}
