package org.homechef.core.domain.recipe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Recipe")
class RecipeTest {

    private static final UrlHash SAMPLE_URL_HASH = UrlHash.fromUrl("https://example.com/recipe");

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates recipe with current timestamp")
        void createsWithCurrentTimestamp() {
            Instant before = Instant.now();

            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Test Recipe", List.of());

            Instant after = Instant.now();

            assertNotNull(recipe.getParsedAt());
            assertFalse(recipe.getParsedAt().isBefore(before));
            assertFalse(recipe.getParsedAt().isAfter(after));
        }

        @Test
        @DisplayName("stores title and ingredients")
        void storesTitleAndIngredients() {
            List<Ingredient> ingredients = List.of(
                Ingredient.of(new BigDecimal("2"), "cups", "flour"),
                Ingredient.of(new BigDecimal("1"), "tsp", "salt")
            );

            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "My Recipe", ingredients);

            assertEquals("My Recipe", recipe.getTitle());
            assertEquals(2, recipe.getIngredients().size());
            assertEquals("flour", recipe.getIngredients().get(0).name());
        }

        @Test
        @DisplayName("handles null ingredients list")
        void handlesNullIngredients() {
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Recipe", null);

            assertNotNull(recipe.getIngredients());
            assertTrue(recipe.getIngredients().isEmpty());
        }

        @Test
        @DisplayName("creates immutable ingredients list")
        void createsImmutableIngredientsList() {
            List<Ingredient> ingredients = List.of(Ingredient.of("salt"));
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Recipe", ingredients);

            assertThrows(UnsupportedOperationException.class, () ->
                recipe.getIngredients().add(Ingredient.of("pepper"))
            );
        }
    }

    @Nested
    @DisplayName("reconstitute()")
    class Reconstitute {

        @Test
        @DisplayName("recreates recipe from persistence data")
        void recreatesFromPersistence() {
            String urlHash = "a".repeat(64);
            Instant parsedAt = Instant.now().minusSeconds(3600);
            List<Ingredient> ingredients = List.of(Ingredient.of("butter"));

            Recipe recipe = Recipe.reconstitute(urlHash, "Old Recipe", ingredients, parsedAt);

            assertEquals(urlHash, recipe.getUrlHash().value());
            assertEquals("Old Recipe", recipe.getTitle());
            assertEquals(1, recipe.getIngredients().size());
            assertEquals(parsedAt, recipe.getParsedAt());
        }
    }

    @Nested
    @DisplayName("staleness checks")
    class StalenessChecks {

        @Test
        @DisplayName("isStale() returns false for fresh recipe (default 30 day TTL)")
        void isFreshWithDefaultTtl() {
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Fresh Recipe", List.of());

            assertFalse(recipe.isStale());
            assertTrue(recipe.isFresh());
        }

        @Test
        @DisplayName("isStale() returns true for recipe older than 30 days")
        void isStaleAfter30Days() {
            Instant oldParsedAt = Instant.now().minus(Duration.ofDays(31));
            Recipe recipe = Recipe.reconstitute(
                SAMPLE_URL_HASH.value(), "Old Recipe", List.of(), oldParsedAt
            );

            assertTrue(recipe.isStale());
            assertFalse(recipe.isFresh());
        }

        @Test
        @DisplayName("isStale(Duration) uses custom TTL")
        void customTtlForStaleness() {
            Instant twoDaysAgo = Instant.now().minus(Duration.ofDays(2));
            Recipe recipe = Recipe.reconstitute(
                SAMPLE_URL_HASH.value(), "Recipe", List.of(), twoDaysAgo
            );

            // With 1-day TTL, should be stale
            assertTrue(recipe.isStale(Duration.ofDays(1)));

            // With 3-day TTL, should be fresh
            assertFalse(recipe.isStale(Duration.ofDays(3)));
        }

        @Test
        @DisplayName("edge case: recipe exactly at TTL boundary is stale")
        void exactlyAtTtlBoundary() {
            Duration ttl = Duration.ofHours(1);
            Instant exactlyOneBoundary = Instant.now().minus(ttl);
            Recipe recipe = Recipe.reconstitute(
                SAMPLE_URL_HASH.value(), "Recipe", List.of(), exactlyOneBoundary
            );

            // At exactly the boundary, parsedAt + ttl == now, so isBefore(now) might be true
            // due to timing. The implementation uses isBefore, which is exclusive.
            // In practice, by the time we check, now() has advanced, making it stale.
            // This is acceptable boundary behavior - document rather than assert exact value.
            assertTrue(recipe.isStale(ttl), "At boundary or past, recipe should be stale");
        }

        @Test
        @DisplayName("recipe just past TTL is stale")
        void justPastTtlIsStale() {
            Duration ttl = Duration.ofHours(1);
            Instant justPastBoundary = Instant.now().minus(ttl).minusNanos(1);
            Recipe recipe = Recipe.reconstitute(
                SAMPLE_URL_HASH.value(), "Recipe", List.of(), justPastBoundary
            );

            assertTrue(recipe.isStale(ttl));
        }
    }

    @Nested
    @DisplayName("updateWith()")
    class UpdateWith {

        @Test
        @DisplayName("updates title and ingredients")
        void updatesTitleAndIngredients() {
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Original", List.of(Ingredient.of("salt")));
            List<Ingredient> newIngredients = List.of(
                Ingredient.of("pepper"),
                Ingredient.of("oregano")
            );

            recipe.updateWith("Updated Title", newIngredients);

            assertEquals("Updated Title", recipe.getTitle());
            assertEquals(2, recipe.getIngredients().size());
            assertEquals("pepper", recipe.getIngredients().get(0).name());
        }

        @Test
        @DisplayName("updates parsedAt timestamp")
        void updatesParsedAtTimestamp() {
            Instant oldParsedAt = Instant.now().minusSeconds(3600);
            Recipe recipe = Recipe.reconstitute(
                SAMPLE_URL_HASH.value(), "Old", List.of(), oldParsedAt
            );
            Instant before = Instant.now();

            recipe.updateWith("New Title", List.of());

            assertTrue(recipe.getParsedAt().isAfter(oldParsedAt) ||
                recipe.getParsedAt().equals(before));
        }

        @Test
        @DisplayName("handles null ingredients in update")
        void handlesNullIngredientsInUpdate() {
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "Recipe", List.of(Ingredient.of("salt")));

            recipe.updateWith("Updated", null);

            assertNotNull(recipe.getIngredients());
            assertTrue(recipe.getIngredients().isEmpty());
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("equals based on urlHash only")
        void equalsBasedOnUrlHash() {
            Recipe recipe1 = Recipe.create(SAMPLE_URL_HASH, "Title 1", List.of());
            Recipe recipe2 = Recipe.create(SAMPLE_URL_HASH, "Title 2", List.of(Ingredient.of("x")));

            assertEquals(recipe1, recipe2);
            assertEquals(recipe1.hashCode(), recipe2.hashCode());
        }

        @Test
        @DisplayName("not equal for different urlHash")
        void notEqualForDifferentUrlHash() {
            Recipe recipe1 = Recipe.create(UrlHash.fromUrl("https://a.com/1"), "Same Title", List.of());
            Recipe recipe2 = Recipe.create(UrlHash.fromUrl("https://b.com/2"), "Same Title", List.of());

            assertNotEquals(recipe1, recipe2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("includes relevant info")
        void includesRelevantInfo() {
            Recipe recipe = Recipe.create(SAMPLE_URL_HASH, "My Recipe", List.of(
                Ingredient.of("a"), Ingredient.of("b")
            ));

            String str = recipe.toString();

            assertTrue(str.contains("My Recipe"));
            assertTrue(str.contains("ingredientCount=2"));
        }
    }
}
