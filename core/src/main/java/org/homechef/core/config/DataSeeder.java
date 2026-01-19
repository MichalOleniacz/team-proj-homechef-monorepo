package org.homechef.core.config;

import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the local database with mock recipe data for frontend development.
 * Only active in the "local" profile. Idempotent: skips if data already exists.
 */
@Component
@Profile("local")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ResourceRepository resourceRepository;
    private final RecipeRepository recipeRepository;

    public DataSeeder(ResourceRepository resourceRepository, RecipeRepository recipeRepository) {
        this.resourceRepository = resourceRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("DataSeeder: Checking if seed data is needed...");

        List<SeedRecipe> seedRecipes = createSeedData();
        int seededCount = 0;
        int failedCount = 0;

        for (SeedRecipe seed : seedRecipes) {
            try {
                Resource resource = Resource.create(seed.url());

                if (resourceRepository.existsByUrlHash(resource.getUrlHash())) {
                    log.debug("DataSeeder: Skipping existing resource: {}", seed.url());
                    continue;
                }

                // Save resource first (FK constraint)
                resourceRepository.save(resource);

                // Create and save recipe
                Recipe recipe = Recipe.create(resource.getUrlHash(), seed.title(), seed.ingredients());
                recipeRepository.save(recipe);

                seededCount++;
                log.info("DataSeeder: Seeded recipe '{}' from {}", seed.title(), seed.url());
            } catch (Exception e) {
                failedCount++;
                log.warn("DataSeeder: Failed to seed recipe '{}': {}", seed.title(), e.getMessage());
            }
        }

        if (seededCount > 0) {
            log.info("DataSeeder: Seeded {} new recipes", seededCount);
        }
        if (failedCount > 0) {
            log.warn("DataSeeder: {} recipes failed to seed", failedCount);
        }
        if (seededCount == 0 && failedCount == 0) {
            log.info("DataSeeder: No new data needed, all recipes already exist");
        }
    }

    private List<SeedRecipe> createSeedData() {
        return List.of(
                new SeedRecipe(
                        "https://www.allrecipes.com/recipe/23600/worlds-best-lasagna/",
                        "World's Best Lasagna",
                        List.of(
                                Ingredient.of(new BigDecimal("1"), "pound", "sweet Italian sausage"),
                                Ingredient.of(new BigDecimal("0.75"), "pound", "lean ground beef"),
                                Ingredient.of(new BigDecimal("0.5"), "cup", "minced onion"),
                                Ingredient.of(new BigDecimal("2"), "cloves", "garlic, crushed"),
                                Ingredient.of(new BigDecimal("28"), "ounce", "crushed tomatoes"),
                                Ingredient.of(new BigDecimal("6"), "ounce", "tomato paste"),
                                Ingredient.of(new BigDecimal("6.5"), "ounce", "canned tomato sauce"),
                                Ingredient.of(new BigDecimal("0.5"), "cup", "water"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "white sugar"),
                                Ingredient.of(new BigDecimal("4"), "tablespoons", "fresh parsley, chopped"),
                                Ingredient.of(new BigDecimal("1.5"), "teaspoons", "dried basil"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "Italian seasoning"),
                                Ingredient.of(new BigDecimal("0.5"), "teaspoon", "fennel seeds"),
                                Ingredient.of("salt to taste"),
                                Ingredient.of(new BigDecimal("12"), null, "lasagna noodles"),
                                Ingredient.of(new BigDecimal("32"), "ounce", "ricotta cheese"),
                                Ingredient.of(new BigDecimal("1"), null, "egg"),
                                Ingredient.of(new BigDecimal("0.75"), "pound", "mozzarella cheese, sliced"),
                                Ingredient.of(new BigDecimal("0.75"), "cup", "Parmesan cheese, grated")
                        )
                ),
                new SeedRecipe(
                        "https://www.seriouseats.com/the-best-chicken-tikka-masala-recipe",
                        "Chicken Tikka Masala",
                        List.of(
                                Ingredient.of(new BigDecimal("2"), "pounds", "boneless chicken thighs"),
                                Ingredient.of(new BigDecimal("1"), "cup", "plain yogurt"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "lemon juice"),
                                Ingredient.of(new BigDecimal("2"), "teaspoons", "garam masala"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "turmeric"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "cumin"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "paprika"),
                                Ingredient.of(new BigDecimal("0.5"), "teaspoon", "cayenne pepper"),
                                Ingredient.of(new BigDecimal("4"), "cloves", "garlic, minced"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "ginger, grated"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "butter"),
                                Ingredient.of(new BigDecimal("1"), null, "large onion, diced"),
                                Ingredient.of(new BigDecimal("14"), "ounce", "crushed tomatoes"),
                                Ingredient.of(new BigDecimal("1"), "cup", "heavy cream"),
                                Ingredient.of("salt to taste"),
                                Ingredient.of("fresh cilantro for garnish")
                        )
                ),
                new SeedRecipe(
                        "https://www.bonappetit.com/recipe/classic-carbonara",
                        "Classic Carbonara",
                        List.of(
                                Ingredient.of(new BigDecimal("1"), "pound", "spaghetti"),
                                Ingredient.of(new BigDecimal("8"), "ounces", "guanciale, cut into strips"),
                                Ingredient.of(new BigDecimal("4"), null, "large egg yolks"),
                                Ingredient.of(new BigDecimal("2"), null, "large whole eggs"),
                                Ingredient.of(new BigDecimal("1.5"), "cups", "Pecorino Romano, finely grated"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "freshly ground black pepper"),
                                Ingredient.of("kosher salt for pasta water")
                        )
                ),
                new SeedRecipe(
                        "https://www.foodnetwork.com/recipes/alton-brown/guacamole-recipe",
                        "Fresh Guacamole",
                        List.of(
                                Ingredient.of(new BigDecimal("3"), null, "ripe avocados"),
                                Ingredient.of(new BigDecimal("1"), null, "lime, juiced"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "kosher salt"),
                                Ingredient.of(new BigDecimal("0.5"), "cup", "cilantro, chopped"),
                                Ingredient.of(new BigDecimal("0.5"), null, "medium onion, diced"),
                                Ingredient.of(new BigDecimal("2"), null, "Roma tomatoes, diced"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "minced garlic"),
                                Ingredient.of(new BigDecimal("1"), null, "jalapeño, seeded and minced")
                        )
                ),
                new SeedRecipe(
                        "https://www.epicurious.com/recipes/food/views/perfect-buttermilk-pancakes",
                        "Perfect Buttermilk Pancakes",
                        List.of(
                                Ingredient.of(new BigDecimal("2"), "cups", "all-purpose flour"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "sugar"),
                                Ingredient.of(new BigDecimal("2"), "teaspoons", "baking powder"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "baking soda"),
                                Ingredient.of(new BigDecimal("0.5"), "teaspoon", "salt"),
                                Ingredient.of(new BigDecimal("2"), "cups", "buttermilk"),
                                Ingredient.of(new BigDecimal("0.25"), "cup", "unsalted butter, melted"),
                                Ingredient.of(new BigDecimal("2"), null, "large eggs"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "vanilla extract"),
                                Ingredient.of("butter for serving"),
                                Ingredient.of("maple syrup for serving")
                        )
                ),
                new SeedRecipe(
                        "https://www.budgetbytes.com/homemade-pad-thai/",
                        "Homemade Pad Thai",
                        List.of(
                                Ingredient.of(new BigDecimal("8"), "ounces", "flat rice noodles"),
                                Ingredient.of(new BigDecimal("3"), "tablespoons", "fish sauce"),
                                Ingredient.of(new BigDecimal("3"), "tablespoons", "rice vinegar"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "brown sugar"),
                                Ingredient.of(new BigDecimal("1"), "tablespoon", "soy sauce"),
                                Ingredient.of(new BigDecimal("1"), "tablespoon", "lime juice"),
                                Ingredient.of(new BigDecimal("0.5"), "teaspoon", "chili garlic sauce"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "vegetable oil"),
                                Ingredient.of(new BigDecimal("1"), "pound", "shrimp, peeled and deveined"),
                                Ingredient.of(new BigDecimal("3"), null, "eggs, beaten"),
                                Ingredient.of(new BigDecimal("4"), null, "green onions, sliced"),
                                Ingredient.of(new BigDecimal("1"), "cup", "bean sprouts"),
                                Ingredient.of(new BigDecimal("0.5"), "cup", "peanuts, chopped"),
                                Ingredient.of("lime wedges for serving"),
                                Ingredient.of("fresh cilantro for garnish")
                        )
                ),
                new SeedRecipe(
                        "https://www.kingarthurbaking.com/recipes/chocolate-chip-cookies-recipe",
                        "Classic Chocolate Chip Cookies",
                        List.of(
                                Ingredient.of(new BigDecimal("2.25"), "cups", "all-purpose flour"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "baking soda"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "salt"),
                                Ingredient.of(new BigDecimal("1"), "cup", "butter, softened"),
                                Ingredient.of(new BigDecimal("0.75"), "cup", "granulated sugar"),
                                Ingredient.of(new BigDecimal("0.75"), "cup", "packed brown sugar"),
                                Ingredient.of(new BigDecimal("2"), null, "large eggs"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "vanilla extract"),
                                Ingredient.of(new BigDecimal("2"), "cups", "semisweet chocolate chips"),
                                Ingredient.of(new BigDecimal("1"), "cup", "chopped walnuts (optional)")
                        )
                ),
                new SeedRecipe(
                        "https://www.simplyrecipes.com/recipes/homemade_pizza/",
                        "Homemade Pizza",
                        List.of(
                                Ingredient.of(new BigDecimal("1"), "cup", "warm water (110°F)"),
                                Ingredient.of(new BigDecimal("2.25"), "teaspoons", "active dry yeast"),
                                Ingredient.of(new BigDecimal("3"), "cups", "all-purpose flour"),
                                Ingredient.of(new BigDecimal("2"), "tablespoons", "olive oil"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "sugar"),
                                Ingredient.of(new BigDecimal("1"), "teaspoon", "salt"),
                                Ingredient.of(new BigDecimal("0.5"), "cup", "pizza sauce"),
                                Ingredient.of(new BigDecimal("2"), "cups", "mozzarella cheese, shredded"),
                                Ingredient.of("fresh basil leaves"),
                                Ingredient.of("desired toppings")
                        )
                )
        );
    }

    /**
     * Internal record for seed data transport.
     */
    private record SeedRecipe(String url, String title, List<Ingredient> ingredients) {}
}
