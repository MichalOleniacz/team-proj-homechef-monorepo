package org.homechef.core.domain.recipe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParseStatus")
class ParseStatusTest {

    @Nested
    @DisplayName("isInFlight()")
    class IsInFlight {

        @Test
        @DisplayName("PENDING is in-flight")
        void pendingIsInFlight() {
            assertTrue(ParseStatus.PENDING.isInFlight());
        }

        @Test
        @DisplayName("PROCESSING is in-flight")
        void processingIsInFlight() {
            assertTrue(ParseStatus.PROCESSING.isInFlight());
        }

        @Test
        @DisplayName("COMPLETED is not in-flight")
        void completedIsNotInFlight() {
            assertFalse(ParseStatus.COMPLETED.isInFlight());
        }

        @Test
        @DisplayName("FAILED is not in-flight")
        void failedIsNotInFlight() {
            assertFalse(ParseStatus.FAILED.isInFlight());
        }
    }

    @Nested
    @DisplayName("isTerminal()")
    class IsTerminal {

        @Test
        @DisplayName("PENDING is not terminal")
        void pendingIsNotTerminal() {
            assertFalse(ParseStatus.PENDING.isTerminal());
        }

        @Test
        @DisplayName("PROCESSING is not terminal")
        void processingIsNotTerminal() {
            assertFalse(ParseStatus.PROCESSING.isTerminal());
        }

        @Test
        @DisplayName("COMPLETED is terminal")
        void completedIsTerminal() {
            assertTrue(ParseStatus.COMPLETED.isTerminal());
        }

        @Test
        @DisplayName("FAILED is terminal")
        void failedIsTerminal() {
            assertTrue(ParseStatus.FAILED.isTerminal());
        }
    }

    @Nested
    @DisplayName("invariants")
    class Invariants {

        @ParameterizedTest
        @EnumSource(ParseStatus.class)
        @DisplayName("isInFlight and isTerminal are mutually exclusive")
        void inFlightAndTerminalAreMutuallyExclusive(ParseStatus status) {
            // A status cannot be both in-flight and terminal
            assertNotEquals(status.isInFlight(), status.isTerminal(),
                status + " should be exactly one of in-flight or terminal");
        }

        @ParameterizedTest
        @EnumSource(ParseStatus.class)
        @DisplayName("every status is either in-flight or terminal")
        void everyStatusIsInFlightOrTerminal(ParseStatus status) {
            assertTrue(status.isInFlight() || status.isTerminal(),
                status + " should be either in-flight or terminal");
        }
    }

    @Nested
    @DisplayName("enum completeness")
    class EnumCompleteness {

        @Test
        @DisplayName("has exactly 4 statuses")
        void hasFourStatuses() {
            assertEquals(4, ParseStatus.values().length);
        }

        @Test
        @DisplayName("contains expected values")
        void containsExpectedValues() {
            assertNotNull(ParseStatus.valueOf("PENDING"));
            assertNotNull(ParseStatus.valueOf("PROCESSING"));
            assertNotNull(ParseStatus.valueOf("COMPLETED"));
            assertNotNull(ParseStatus.valueOf("FAILED"));
        }
    }
}
