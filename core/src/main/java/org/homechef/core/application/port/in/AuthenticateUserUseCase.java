package org.homechef.core.application.port.in;

import org.homechef.core.application.port.in.dto.AuthResult;
import org.homechef.core.application.port.in.dto.LoginCommand;

/**
 * Use case for authenticating an existing user.
 */
public interface AuthenticateUserUseCase {

    /**
     * Authenticates a user with email and password.
     *
     * @param command the login command
     * @return auth result with JWT token
     * @throws org.homechef.core.domain.user.exception.InvalidCredentialsException if credentials are invalid
     */
    AuthResult authenticate(LoginCommand command);
}
