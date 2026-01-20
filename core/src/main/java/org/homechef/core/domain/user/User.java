package org.homechef.core.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * User aggregate root.
 * Represents a registered user with email/password authentication.
 */
public class User {

    private final UserId id;
    private final Email email;
    private final HashedPassword password;
    private final Instant createdAt;

    private User(UserId id, Email email, HashedPassword password, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "User id cannot be null");
        this.email = Objects.requireNonNull(email, "User email cannot be null");
        this.password = Objects.requireNonNull(password, "User password cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "User createdAt cannot be null");
    }

    /**
     * Factory method for creating a new user.
     */
    public static User create(Email email, HashedPassword password) {
        return new User(
            UserId.generate(),
            email,
            password,
            Instant.now()
        );
    }

    /**
     * Factory method for reconstituting a user from persistence.
     */
    public static User reconstitute(UUID id, String email, String passwordHash, Instant createdAt) {
        return new User(
            UserId.of(id),
            Email.of(email),
            HashedPassword.of(passwordHash),
            createdAt
        );
    }

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
