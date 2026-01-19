package org.homechef.core.domain.recipe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UrlHash")
class UrlHashTest {

    @Nested
    @DisplayName("fromUrl()")
    class FromUrl {

        @Test
        @DisplayName("produces consistent hash for same URL")
        void consistentHashForSameUrl() {
            String url = "https://example.com/recipe/123";
            UrlHash hash1 = UrlHash.fromUrl(url);
            UrlHash hash2 = UrlHash.fromUrl(url);

            assertEquals(hash1, hash2);
            assertEquals(hash1.value(), hash2.value());
        }

        @Test
        @DisplayName("produces 64-character SHA-256 hex hash")
        void produces64CharHash() {
            UrlHash hash = UrlHash.fromUrl("https://example.com/recipe");

            assertEquals(64, hash.value().length());
            assertTrue(hash.value().matches("[a-f0-9]{64}"), "Should be lowercase hex");
        }

        @ParameterizedTest
        @DisplayName("normalizes equivalent URLs to same hash")
        @CsvSource({
            // Trailing slash removal
            "https://example.com/recipe/, https://example.com/recipe",
            // Case normalization (scheme + host)
            "HTTPS://EXAMPLE.COM/Recipe, https://example.com/Recipe",
            // Default port removal
            "https://example.com:443/recipe, https://example.com/recipe",
            "http://example.com:80/recipe, http://example.com/recipe",
            // Whitespace trimming
            "  https://example.com/recipe  , https://example.com/recipe"
        })
        void normalizesEquivalentUrls(String url1, String url2) {
            UrlHash hash1 = UrlHash.fromUrl(url1);
            UrlHash hash2 = UrlHash.fromUrl(url2);

            assertEquals(hash1.value(), hash2.value(),
                "URLs '" + url1 + "' and '" + url2 + "' should normalize to same hash");
        }

        @ParameterizedTest
        @DisplayName("produces different hashes for different URLs")
        @CsvSource({
            "https://example.com/recipe/1, https://example.com/recipe/2",
            "https://example.com/recipe, https://other.com/recipe",
            "https://example.com/recipe?a=1, https://example.com/recipe?a=2",
            "http://example.com/recipe, https://example.com/recipe"
        })
        void differentHashesForDifferentUrls(String url1, String url2) {
            UrlHash hash1 = UrlHash.fromUrl(url1);
            UrlHash hash2 = UrlHash.fromUrl(url2);

            assertNotEquals(hash1.value(), hash2.value(),
                "URLs '" + url1 + "' and '" + url2 + "' should have different hashes");
        }

        @Test
        @DisplayName("preserves query parameters in hash")
        void preservesQueryParameters() {
            UrlHash withQuery = UrlHash.fromUrl("https://example.com/recipe?id=123");
            UrlHash withoutQuery = UrlHash.fromUrl("https://example.com/recipe");

            assertNotEquals(withQuery.value(), withoutQuery.value());
        }

        @Test
        @DisplayName("strips fragment from URL before hashing")
        void stripsFragment() {
            UrlHash withFragment = UrlHash.fromUrl("https://example.com/recipe#section");
            UrlHash withoutFragment = UrlHash.fromUrl("https://example.com/recipe");

            assertEquals(withFragment.value(), withoutFragment.value(),
                "Fragment should be stripped during normalization");
        }

        @Test
        @DisplayName("rejects null URL")
        void rejectsNullUrl() {
            assertThrows(NullPointerException.class, () -> UrlHash.fromUrl(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("rejects empty or blank URLs")
        void rejectsEmptyOrBlankUrls(String invalidUrl) {
            assertThrows(IllegalArgumentException.class, () -> UrlHash.fromUrl(invalidUrl));
        }

        @Test
        @DisplayName("handles malformed URLs gracefully via fallback")
        void handlesMalformedUrls() {
            // Should not throw - falls back to lowercase trimmed string
            UrlHash hash = UrlHash.fromUrl("not-a-valid-url");

            assertNotNull(hash);
            assertEquals(64, hash.value().length());
        }

        @Test
        @DisplayName("preserves non-default ports")
        void preservesNonDefaultPorts() {
            UrlHash port8080 = UrlHash.fromUrl("https://example.com:8080/recipe");
            UrlHash noPort = UrlHash.fromUrl("https://example.com/recipe");

            assertNotEquals(port8080.value(), noPort.value());
        }
    }

    @Nested
    @DisplayName("fromHash()")
    class FromHash {

        @Test
        @DisplayName("reconstructs UrlHash from valid 64-char hash")
        void reconstructsFromValidHash() {
            String validHash = "a".repeat(64);
            UrlHash hash = UrlHash.fromHash(validHash);

            assertEquals(validHash, hash.value());
        }

        @Test
        @DisplayName("rejects empty hash")
        void rejectsEmptyHash() {
            assertThrows(IllegalArgumentException.class, () -> UrlHash.fromHash(""));
        }

        @Test
        @DisplayName("rejects short hash")
        void rejectsShortHash() {
            assertThrows(IllegalArgumentException.class, () -> UrlHash.fromHash("abc"));
        }

        @Test
        @DisplayName("rejects hash with 63 characters")
        void rejectsHash63Chars() {
            String hash63 = "a".repeat(63);
            assertThrows(IllegalArgumentException.class, () -> UrlHash.fromHash(hash63));
        }

        @Test
        @DisplayName("rejects hash with 65 characters")
        void rejectsHash65Chars() {
            String hash65 = "a".repeat(65);
            assertThrows(IllegalArgumentException.class, () -> UrlHash.fromHash(hash65));
        }

        @Test
        @DisplayName("rejects null hash")
        void rejectsNullHash() {
            assertThrows(NullPointerException.class, () -> UrlHash.fromHash(null));
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("equals based on hash value")
        void equalsBasedOnValue() {
            UrlHash hash1 = UrlHash.fromUrl("https://example.com/recipe");
            UrlHash hash2 = UrlHash.fromUrl("https://example.com/recipe");

            assertEquals(hash1, hash2);
            assertEquals(hash1.hashCode(), hash2.hashCode());
        }

        @Test
        @DisplayName("not equal for different hash values")
        void notEqualForDifferentValues() {
            UrlHash hash1 = UrlHash.fromUrl("https://example.com/recipe1");
            UrlHash hash2 = UrlHash.fromUrl("https://example.com/recipe2");

            assertNotEquals(hash1, hash2);
        }
    }
}
