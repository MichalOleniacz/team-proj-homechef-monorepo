package org.homechef.core.application.port.out;

import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.UrlHash;

import java.util.Optional;

/**
 * Driven port for Recipe persistence.
 */
public interface RecipeRepository {

    /**
     * Saves or updates a recipe (upsert by url_hash).
     */
    Recipe save(Recipe recipe);

    /**
     * Finds a recipe by URL hash.
     */
    Optional<Recipe> findByUrlHash(UrlHash urlHash);

    /**
     * Finds a fresh (non-stale) recipe by URL hash.
     * Returns empty if recipe doesn't exist or is stale.
     */
    Optional<Recipe> findFreshByUrlHash(UrlHash urlHash);
}