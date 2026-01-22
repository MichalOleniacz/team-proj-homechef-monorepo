package org.homechef.core.adapter.in.web;

import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.homechef.core.adapter.in.web.dto.AuthResponse;
import org.homechef.core.adapter.in.web.dto.LoginRequest;
import org.homechef.core.adapter.in.web.dto.RegisterRequest;
import org.homechef.core.application.port.in.AuthenticateUserUseCase;
import org.homechef.core.application.port.in.RegisterUserUseCase;
import org.homechef.core.application.port.in.dto.AuthResult;
import org.homechef.core.application.port.in.dto.LoginCommand;
import org.homechef.core.application.port.in.dto.RegisterCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @Timed(value = "auth.register.duration", description = "Time to process user registration")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request",
                kv("email", request.email()),
                kv("endpoint", "POST /auth/register"));

        RegisterCommand command = new RegisterCommand(request.email(), request.password());
        AuthResult result = registerUserUseCase.register(command);

        log.info("User registered successfully",
                kv("userId", result.userId()),
                kv("email", result.email()));

        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    @Timed(value = "auth.login.duration", description = "Time to process user login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request",
                kv("email", request.email()),
                kv("endpoint", "POST /auth/login"));

        LoginCommand command = new LoginCommand(request.email(), request.password());
        AuthResult result = authenticateUserUseCase.authenticate(command);

        log.info("User authenticated successfully",
                kv("userId", result.userId()),
                kv("email", result.email()));

        return ResponseEntity.ok(AuthResponse.from(result));
    }
}
