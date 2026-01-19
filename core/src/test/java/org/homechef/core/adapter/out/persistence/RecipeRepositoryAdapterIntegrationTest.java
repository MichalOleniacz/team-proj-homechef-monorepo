package org.homechef.core.adapter.out.persistence;

import org.homechef.core.IntegrationTestBase;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.Resource;
import org.homechef.core.domain.recipe.UrlHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecipeRepositoryAdapter Integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RecipeRepositoryAdapterIntegrationTest extends IntegrationTestBase {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SAMPLE_URL = "https://example.com/recipe/test-" + System.currentTimeMillis();

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM parse_request");
        jdbcTemplate.execute("DELETE FROM recipe");
        jdbcTemplate.execute("DELETE FROM resource");
    }

    private Resource createResource(String url) {
        return resourceRepository.save(Resource.create(url));
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("saves new recipe with ingredients")
        void savesNewRecipeWithIngredients() {
            // Given
            String url = SAMPLE_URL + "-save";
            Resource resource = createResource(url);
            List<Ingredient> ingredients = List.of(
                Ingredient.of(new BigDecimal("2"), "cups", "flour"),
                Ingredient.of(new BigDecimal("1"), "tsp", "salt"),
                Ingredient.of("butter for greasing")
            );
            Recipe recipe = Recipe.create(resource.getUrlHash(), "Test Recipe", ingredients);

            // When
            Recipe saved = recipeRepository.save(recipe);

            // Then
            assertNotNull(saved);
            assertEquals("Test Recipe", saved.getTitle());
            assertEquals(3, saved.getIngredients().size());
            assertEquals("flour", saved.getIngredients().get(0).name());
        }

        @Test
        @DisplayName("updates existing recipe (upsert)")
        void updatesExistingRecipe() {
            // Given
            String url = SAMPLE_URL + "-upsert";
            Resource resource = createResource(url);

            Recipe original = Recipe.create(
                resource.getUrlHash(),
                "Original Title",
                List.of(Ingredient.of("salt"))
            );
            recipeRepository.save(original);

            // When - save again with different data
            Recipe updated = Recipe.reconstitute(
                resource.getUrlHash().value(),
                "Updated Title",
                List.of(Ingredient.of("pepper"), Ingredient.of("oregano")),
                Instant.now()
            );
            Recipe result = recipeRepository.save(updated);

            // Then
            assertEquals("Updated Title", result.getTitle());
            assertEquals(2, result.getIngredients().size());

            // Only one recipe should exist
            Optional<Recipe> found = recipeRepository.findByUrlHash(resource.getUrlHash());
            assertTrue(found.isPresent());
            assertEquals("Updated Title", found.get().getTitle());
        }
    }

    @Nested
    @DisplayName("findByUrlHash()")
    class FindByUrlHash {

        @Test
        @DisplayName("finds existing recipe by URL hash")
        void findsExistingRecipe() {
            // Given
            String url = SAMPLE_URL + "-find";
            Resource resource = createResource(url);
            Recipe recipe = Recipe.create(
                resource.getUrlHash(),
                "Findable Recipe",
                List.of(Ingredient.of(new BigDecimal("500"), "g", "pasta"))
            );
            recipeRepository.save(recipe);

            // When
            Optional<Recipe> found = recipeRepository.findByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            assertEquals("Findable Recipe", found.get().getTitle());
            assertEquals(1, found.get().getIngredients().size());
            assertEquals("pasta", found.get().getIngredients().get(0).name());
        }

        @Test
        @DisplayName("returns empty for non-existent URL hash")
        void returnsEmptyForNonExistent() {
            // Given
            UrlHash nonExistent = UrlHash.fromUrl("https://nonexistent.com/recipe");

            // When
            Optional<Recipe> found = recipeRepository.findByUrlHash(nonExistent);

            // Then
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findFreshByUrlHash()")
    class FindFreshByUrlHash {

        @Test
        @DisplayName("finds fresh recipe within TTL")
        void findsFreshRecipe() {
            // Given
            String url = SAMPLE_URL + "-fresh";
            Resource resource = createResource(url);
            Recipe freshRecipe = Recipe.create(
                resource.getUrlHash(),
                "Fresh Recipe",
                List.of()
            );
            recipeRepository.save(freshRecipe);

            // When
            Optional<Recipe> found = recipeRepository.findFreshByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            assertEquals("Fresh Recipe", found.get().getTitle());
        }

        @Test
        @DisplayName("returns empty for stale recipe beyond TTL")
        void returnsEmptyForStaleRecipe() {
            // Given - insert recipe with old parsed_at using raw SQL
            String url = SAMPLE_URL + "-stale";
            Resource resource = createResource(url);
            UrlHash urlHash = resource.getUrlHash();

            // Insert stale recipe (parsed 60 days ago)
            jdbcTemplate.update(
                "INSERT INTO recipe (url_hash, title, ingredients, parsed_at) VALUES (?, ?, ?::jsonb, ?)",
                urlHash.value(),
                "Stale Recipe",
                "[]",
                Instant.now().minus(Duration.ofDays(60))
            );

            // When
            Optional<Recipe> found = recipeRepository.findFreshByUrlHash(urlHash);

            // Then
            assertTrue(found.isEmpty(), "Stale recipe should not be returned");

            // But findByUrlHash should still find it
            Optional<Recipe> anyRecipe = recipeRepository.findByUrlHash(urlHash);
            assertTrue(anyRecipe.isPresent(), "Recipe should still exist");
        }

        @Test
        @DisplayName("returns empty for non-existent recipe")
        void returnsEmptyForNonExistent() {
            // Given
            UrlHash nonExistent = UrlHash.fromUrl("https://nonexistent.com/fresh");

            // When
            Optional<Recipe> found = recipeRepository.findFreshByUrlHash(nonExistent);

            // Then
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("ingredients JSONB")
    class IngredientsJsonb {

        @Test
        @DisplayName("correctly serializes and deserializes ingredients")
        void serializesAndDeserializesIngredients() {
            // Given
            String url = SAMPLE_URL + "-jsonb";
            Resource resource = createResource(url);
            List<Ingredient> ingredients = List.of(
                Ingredient.of(new BigDecimal("2.5"), "cups", "flour"),
                Ingredient.of(new BigDecimal("0.5"), null, "lemons"),
                Ingredient.of("salt to taste")
            );
            Recipe recipe = Recipe.create(resource.getUrlHash(), "JSONB Test", ingredients);

            // When
            recipeRepository.save(recipe);
            Optional<Recipe> found = recipeRepository.findByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            List<Ingredient> loadedIngredients = found.get().getIngredients();
            assertEquals(3, loadedIngredients.size());

            // Check first ingredient (full)
            assertEquals(new BigDecimal("2.5"), loadedIngredients.get(0).quantity());
            assertEquals("cups", loadedIngredients.get(0).unit());
            assertEquals("flour", loadedIngredients.get(0).name());

            // Check second ingredient (no unit)
            assertEquals(new BigDecimal("0.5"), loadedIngredients.get(1).quantity());
            assertNull(loadedIngredients.get(1).unit());
            assertEquals("lemons", loadedIngredients.get(1).name());

            // Check third ingredient (name only)
            assertNull(loadedIngredients.get(2).quantity());
            assertNull(loadedIngredients.get(2).unit());
            assertEquals("salt to taste", loadedIngredients.get(2).name());
        }

        @Test
        @DisplayName("handles empty ingredients list")
        void handlesEmptyIngredients() {
            // Given
            String url = SAMPLE_URL + "-empty";
            Resource resource = createResource(url);
            Recipe recipe = Recipe.create(resource.getUrlHash(), "No Ingredients", List.of());

            // When
            recipeRepository.save(recipe);
            Optional<Recipe> found = recipeRepository.findByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            assertTrue(found.get().getIngredients().isEmpty());
        }
    }
}
