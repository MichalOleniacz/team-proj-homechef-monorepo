package org.homechef.core.domain.recipe;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a parsed recipe.
 * 1:1 relationship with Resource (shares url_hash as PK).
 */
public class Recipe {

    private static final Duration DEFAULT_TTL = Duration.ofDays(30);

    private final UrlHash urlHash;
    private String title;
    private List<Ingredient> ingredients;
    private Instant parsedAt;

    private Recipe(UrlHash urlHash, String title, List<Ingredient> ingredients, Instant parsedAt) {
        this.urlHash = Objects.requireNonNull(urlHash, "urlHash cannot be null");
        this.title = title;
        this.ingredients = ingredients != null ? List.copyOf(ingredients) : List.of();
        this.parsedAt = Objects.requireNonNull(parsedAt, "parsedAt cannot be null");
    }

    /**
     * Creates a new Recipe from successful LLM parsing.
     */
    public static Recipe create(UrlHash urlHash, String title, List<Ingredient> ingredients) {
        return new Recipe(urlHash, title, ingredients, Instant.now());
    }

    /**
     * Reconstitutes a Recipe from persistence.
     */
    public static Recipe reconstitute(String urlHash, String title, List<Ingredient> ingredients, Instant parsedAt) {
        return new Recipe(
                UrlHash.fromHash(urlHash),
                title,
                ingredients,
                parsedAt
        );
    }

    /**
     * Checks if this recipe is stale based on the configured TTL.
     */
    public boolean isStale() {
        return isStale(DEFAULT_TTL);
    }

    /**
     * Checks if this recipe is stale based on a custom TTL.
     */
    public boolean isStale(Duration ttl) {
        return parsedAt.plus(ttl).isBefore(Instant.now());
    }

    /**
     * Checks if this recipe is fresh (not stale).
     */
    public boolean isFresh() {
        return !isStale();
    }

    /**
     * Updates the recipe with new parsed data (in-place update for re-parsing).
     */
    public void updateWith(String newTitle, List<Ingredient> newIngredients) {
        this.title = newTitle;
        this.ingredients = newIngredients != null ? List.copyOf(newIngredients) : List.of();
        this.parsedAt = Instant.now();
    }

    public UrlHash getUrlHash() {
        return urlHash;
    }

    public String getTitle() {
        return title;
    }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public Instant getParsedAt() {
        return parsedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(urlHash, recipe.urlHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlHash);
    }

    @Override
    public String toString() {
        return "Recipe{urlHash=" + urlHash.value() + ", title='" + title + "', ingredientCount=" + ingredients.size() + "}";
    }
}