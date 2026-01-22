package org.homechef.core.adapter.in.web;

import org.homechef.core.IntegrationTestBase;
import org.homechef.core.application.port.in.AuthenticateUserUseCase;
import org.homechef.core.application.port.in.RegisterUserUseCase;
import org.homechef.core.application.port.in.dto.AuthResult;
import org.homechef.core.application.port.in.dto.LoginCommand;
import org.homechef.core.application.port.in.dto.RegisterCommand;
import org.homechef.core.application.port.out.TokenProvider;
import org.homechef.core.domain.user.UserId;
import org.homechef.core.domain.user.exception.EmailAlreadyExistsException;
import org.homechef.core.domain.user.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auth Service Integration")
class AuthControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM app_user");
    }

    @Nested
    @DisplayName("User Registration")
    class Registration {

        @Test
        @DisplayName("registers new user successfully")
        void registersNewUser() {
            RegisterCommand command = new RegisterCommand("test@example.com", "password123");

            AuthResult result = registerUserUseCase.register(command);

            assertNotNull(result.userId());
            assertEquals("test@example.com", result.email());
            assertNotNull(result.accessToken());
            assertEquals(3600L, result.expiresIn());
        }

        @Test
        @DisplayName("throws exception for duplicate email")
        void throwsExceptionForDuplicateEmail() {
            RegisterCommand command = new RegisterCommand("duplicate@example.com", "password123");

            // First registration succeeds
            registerUserUseCase.register(command);

            // Second registration fails
            assertThrows(EmailAlreadyExistsException.class, () ->
                registerUserUseCase.register(command)
            );
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmail() {
            RegisterCommand command = new RegisterCommand("TEST@EXAMPLE.COM", "password123");

            AuthResult result = registerUserUseCase.register(command);

            assertEquals("test@example.com", result.email());
        }
    }

    @Nested
    @DisplayName("User Authentication")
    class Authentication {

        @Test
        @DisplayName("authenticates user with correct credentials")
        void authenticatesUser() {
            // Register first
            RegisterCommand registerCommand = new RegisterCommand("login@example.com", "password123");
            registerUserUseCase.register(registerCommand);

            // Login
            LoginCommand loginCommand = new LoginCommand("login@example.com", "password123");
            AuthResult result = authenticateUserUseCase.authenticate(loginCommand);

            assertNotNull(result.userId());
            assertEquals("login@example.com", result.email());
            assertNotNull(result.accessToken());
        }

        @Test
        @DisplayName("throws exception for wrong password")
        void throwsExceptionForWrongPassword() {
            // Register first
            RegisterCommand registerCommand = new RegisterCommand("wrongpass@example.com", "password123");
            registerUserUseCase.register(registerCommand);

            // Try to login with wrong password
            LoginCommand loginCommand = new LoginCommand("wrongpass@example.com", "wrongpassword");

            assertThrows(InvalidCredentialsException.class, () ->
                authenticateUserUseCase.authenticate(loginCommand)
            );
        }

        @Test
        @DisplayName("throws exception for non-existent user")
        void throwsExceptionForNonExistentUser() {
            LoginCommand loginCommand = new LoginCommand("nonexistent@example.com", "password123");

            assertThrows(InvalidCredentialsException.class, () ->
                authenticateUserUseCase.authenticate(loginCommand)
            );
        }
    }

    @Nested
    @DisplayName("JWT Token")
    class TokenTests {

        @Test
        @DisplayName("generated token is valid")
        void generatedTokenIsValid() {
            RegisterCommand command = new RegisterCommand("token@example.com", "password123");
            AuthResult result = registerUserUseCase.register(command);

            Optional<UserId> validatedUser = tokenProvider.validateToken(result.accessToken());

            assertTrue(validatedUser.isPresent());
            assertEquals(result.userId(), validatedUser.get().value());
        }

        @Test
        @DisplayName("invalid token returns empty")
        void invalidTokenReturnsEmpty() {
            Optional<UserId> validatedUser = tokenProvider.validateToken("invalid.token.here");

            assertTrue(validatedUser.isEmpty());
        }
    }
}
