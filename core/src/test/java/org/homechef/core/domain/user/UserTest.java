package org.homechef.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Aggregate")
class UserTest {

    @Test
    @DisplayName("creates new user with generated ID")
    void createsNewUserWithGeneratedId() {
        Email email = Email.of("test@example.com");
        HashedPassword password = HashedPassword.of("$2a$10$hashedpassword");

        User user = User.create(email, password);

        assertNotNull(user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("reconstitutes user from persistence")
    void reconstitutesUserFromPersistence() {
        UUID id = UUID.randomUUID();
        String email = "stored@example.com";
        String passwordHash = "$2a$10$storedpassword";
        Instant createdAt = Instant.now().minusSeconds(3600);

        User user = User.reconstitute(id, email, passwordHash, createdAt);

        assertEquals(id, user.getId().value());
        assertEquals(email, user.getEmail().value());
        assertEquals(passwordHash, user.getPassword().value());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    @DisplayName("equality is based on ID")
    void equalityBasedOnId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user1 = User.reconstitute(id, "user1@example.com", "hash1", now);
        User user2 = User.reconstitute(id, "user2@example.com", "hash2", now);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("different IDs are not equal")
    void differentIdsNotEqual() {
        Instant now = Instant.now();

        User user1 = User.reconstitute(UUID.randomUUID(), "same@example.com", "hash", now);
        User user2 = User.reconstitute(UUID.randomUUID(), "same@example.com", "hash", now);

        assertNotEquals(user1, user2);
    }
}
