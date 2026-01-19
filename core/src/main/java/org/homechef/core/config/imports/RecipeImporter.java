package org.homechef.core.config.imports;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Imports recipe data from a JSON file on startup.
 * Only active in the "local" profile. Idempotent: skips existing recipes.
 *
 * <p>Configure the import file path via:
 * <pre>
 * homechef.import.recipes-file=classpath:data/recipes-import.json
 * homechef.import.recipes-file=file:/path/to/recipes.json
 * </pre>
 *
 * <p>Expected JSON format:
 * <pre>
 * [
 *   {
 *     "url": "https://example.com/recipe",
 *     "title": "Recipe Title",
 *     "ingredients": [
 *       { "quantity": "2", "unit": "cups", "name": "flour" },
 *       { "name": "salt to taste" }
 *     ]
 *   }
 * ]
 * </pre>
 */
@Component
@Profile("local")
@Order(1) // Run before DataSeeder (default order)
public class RecipeImporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RecipeImporter.class);

    private final ResourceRepository resourceRepository;
    private final RecipeRepository recipeRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String recipesFilePath;
    private final boolean enabled;

    public RecipeImporter(
            ResourceRepository resourceRepository,
            RecipeRepository recipeRepository,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper,
            @Value("${homechef.import.recipes-file:}") String recipesFilePath,
            @Value("${homechef.import.enabled:true}") boolean enabled
    ) {
        this.resourceRepository = resourceRepository;
        this.recipeRepository = recipeRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.recipesFilePath = recipesFilePath;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("RecipeImporter: Disabled via configuration");
            return;
        }

        if (recipesFilePath == null || recipesFilePath.isBlank()) {
            log.debug("RecipeImporter: No import file configured (homechef.import.recipes-file)");
            return;
        }

        log.info("RecipeImporter: Loading recipes from {}", recipesFilePath);

        try {
            List<RecipeImportData> recipes = loadRecipesFromFile();
            importRecipes(recipes);
        } catch (IOException e) {
            log.warn("RecipeImporter: Failed to load import file: {}", e.getMessage());
        }
    }

    private List<RecipeImportData> loadRecipesFromFile() throws IOException {
        org.springframework.core.io.Resource resource = resourceLoader.getResource(recipesFilePath);

        if (!resource.exists()) {
            throw new IOException("Import file not found: " + recipesFilePath);
        }

        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<RecipeImportData>>() {});
        }
    }

    private void importRecipes(List<RecipeImportData> recipes) {
        int importedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (RecipeImportData data : recipes) {
            try {
                if (data.url() == null || data.url().isBlank()) {
                    log.warn("RecipeImporter: Skipping recipe with missing URL");
                    failedCount++;
                    continue;
                }

                Resource resource = Resource.create(data.url());

                if (resourceRepository.existsByUrlHash(resource.getUrlHash())) {
                    log.debug("RecipeImporter: Skipping existing recipe: {}", data.url());
                    skippedCount++;
                    continue;
                }

                // Save resource first (FK constraint)
                resourceRepository.save(resource);

                // Convert and save recipe
                List<Ingredient> ingredients = convertIngredients(data.ingredients());
                Recipe recipe = Recipe.create(resource.getUrlHash(), data.title(), ingredients);
                recipeRepository.save(recipe);

                importedCount++;
                log.info("RecipeImporter: Imported '{}' from {}", data.title(), data.url());

            } catch (Exception e) {
                failedCount++;
                log.warn("RecipeImporter: Failed to import '{}': {}", data.title(), e.getMessage());
            }
        }

        log.info("RecipeImporter: Complete - imported={}, skipped={}, failed={}",
                importedCount, skippedCount, failedCount);
    }

    private List<Ingredient> convertIngredients(List<RecipeImportData.IngredientImportData> ingredientData) {
        if (ingredientData == null) {
            return List.of();
        }

        return ingredientData.stream()
                .filter(i -> i.name() != null && !i.name().isBlank())
                .map(this::convertIngredient)
                .toList();
    }

    private Ingredient convertIngredient(RecipeImportData.IngredientImportData data) {
        BigDecimal quantity = parseQuantity(data.quantity());
        String unit = data.unit() != null && !data.unit().isBlank() ? data.unit() : null;
        return Ingredient.of(quantity, unit, data.name());
    }

    private BigDecimal parseQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) {
            return null;
        }

        try {
            // Handle fractions like "1/2" or "1 1/2"
            if (quantity.contains("/")) {
                return parseFraction(quantity);
            }
            return new BigDecimal(quantity.trim());
        } catch (NumberFormatException e) {
            log.debug("RecipeImporter: Could not parse quantity '{}', treating as null", quantity);
            return null;
        }
    }

    private BigDecimal parseFraction(String fraction) {
        String[] parts = fraction.trim().split("\\s+");

        if (parts.length == 2) {
            // Mixed number: "1 1/2"
            BigDecimal whole = new BigDecimal(parts[0]);
            BigDecimal frac = parseSingleFraction(parts[1]);
            return whole.add(frac);
        } else {
            // Simple fraction: "1/2"
            return parseSingleFraction(parts[0]);
        }
    }

    private BigDecimal parseSingleFraction(String fraction) {
        String[] parts = fraction.split("/");
        if (parts.length != 2) {
            throw new NumberFormatException("Invalid fraction: " + fraction);
        }
        BigDecimal numerator = new BigDecimal(parts[0].trim());
        BigDecimal denominator = new BigDecimal(parts[1].trim());
        return numerator.divide(denominator, 4, java.math.RoundingMode.HALF_UP);
    }
}
