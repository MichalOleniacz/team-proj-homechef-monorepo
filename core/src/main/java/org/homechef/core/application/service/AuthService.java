package org.homechef.core.application.service;

import org.homechef.core.application.port.in.AuthenticateUserUseCase;
import org.homechef.core.application.port.in.RegisterUserUseCase;
import org.homechef.core.application.port.in.dto.AuthResult;
import org.homechef.core.application.port.in.dto.LoginCommand;
import org.homechef.core.application.port.in.dto.RegisterCommand;
import org.homechef.core.application.port.out.PasswordEncoder;
import org.homechef.core.application.port.out.TokenProvider;
import org.homechef.core.application.port.out.UserRepository;
import org.homechef.core.domain.user.Email;
import org.homechef.core.domain.user.HashedPassword;
import org.homechef.core.domain.user.User;
import org.homechef.core.domain.user.exception.EmailAlreadyExistsException;
import org.homechef.core.domain.user.exception.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class AuthService implements RegisterUserUseCase, AuthenticateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResult register(RegisterCommand command) {
        Email email = Email.of(command.email());

        log.info("Processing user registration", kv("email", email.value()));

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - email already exists", kv("email", email.value()));
            throw new EmailAlreadyExistsException(email);
        }

        HashedPassword hashedPassword = passwordEncoder.encode(command.password());
        User user = User.create(email, hashedPassword);
        User saved = userRepository.save(user);

        String token = tokenProvider.generateToken(saved);

        log.info("User registered successfully",
                kv("userId", saved.getId().value()),
                kv("email", saved.getEmail().value()));

        return new AuthResult(
            saved.getId().value(),
            saved.getEmail().value(),
            token,
            tokenProvider.getExpirationSeconds()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult authenticate(LoginCommand command) {
        Email email = Email.of(command.email());

        log.info("Processing user authentication", kv("email", email.value()));

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Authentication failed - user not found", kv("email", email.value()));
                return new InvalidCredentialsException();
            });

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("Authentication failed - invalid password",
                    kv("userId", user.getId().value()),
                    kv("email", email.value()));
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user);

        log.info("User authenticated successfully",
                kv("userId", user.getId().value()),
                kv("email", email.value()));

        return new AuthResult(
            user.getId().value(),
            user.getEmail().value(),
            token,
            tokenProvider.getExpirationSeconds()
        );
    }
}
