package org.homechef.core.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("creates valid email")
    void createsValidEmail() {
        Email email = Email.of("Test@Example.COM");

        assertEquals("test@example.com", email.value());
    }

    @Test
    @DisplayName("trims whitespace")
    void trimsWhitespace() {
        Email email = Email.of("  test@example.com  ");

        assertEquals("test@example.com", email.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid@example.com",
            "user.name@domain.org",
            "user+tag@example.co.uk",
            "test123@test.io"
    })
    @DisplayName("accepts valid email formats")
    void acceptsValidFormats(String validEmail) {
        assertDoesNotThrow(() -> Email.of(validEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "@example.com",
            "user@",
            "user@domain",
            "",
            "user@@domain.com",
            "user@domain..com"
    })
    @DisplayName("rejects invalid email formats")
    void rejectsInvalidFormats(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> Email.of(invalidEmail));
    }

    @Test
    @DisplayName("rejects null email")
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> Email.of(null));
    }

    @Test
    @DisplayName("equality is based on normalized value")
    void equalityBasedOnNormalizedValue() {
        Email email1 = Email.of("Test@Example.COM");
        Email email2 = Email.of("test@example.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }
}
