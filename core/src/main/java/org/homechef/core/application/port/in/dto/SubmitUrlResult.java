package org.homechef.core.application.port.in.dto;

import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.ParseStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of submitting a URL for parsing.
 * Either returns a cached recipe immediately, or a request ID for polling.
 */
public record SubmitUrlResult(
        ResultType type,
        UUID requestId,      // present for PENDING/PROCESSING
        ParseStatus status,  // present for PENDING/PROCESSING
        RecipeData recipe    // present for CACHED
) {
    public enum ResultType {
        CACHED,     // Fresh recipe found, returned immediately
        PENDING,    // New request created, poll for result
        DEDUPED     // Existing in-flight request found, poll for result
    }

    /**
     * Recipe data for cached responses.
     */
    public record RecipeData(
            String urlHash,
            String title,
            List<IngredientData> ingredients,
            Instant parsedAt
    ) {}

    public record IngredientData(
            String quantity,
            String unit,
            String name
    ) {
        public static IngredientData from(Ingredient ingredient) {
            return new IngredientData(
                    ingredient.quantity() != null ? ingredient.quantity().toPlainString() : null,
                    ingredient.unit(),
                    ingredient.name()
            );
        }
    }

    public static SubmitUrlResult cached(String urlHash, String title, List<Ingredient> ingredients, Instant parsedAt) {
        List<IngredientData> ingredientData = ingredients.stream()
                .map(IngredientData::from)
                .toList();
        return new SubmitUrlResult(
                ResultType.CACHED,
                null,
                ParseStatus.COMPLETED,
                new RecipeData(urlHash, title, ingredientData, parsedAt)
        );
    }

    public static SubmitUrlResult pending(UUID requestId) {
        return new SubmitUrlResult(ResultType.PENDING, requestId, ParseStatus.PENDING, null);
    }

    public static SubmitUrlResult deduped(UUID requestId, ParseStatus status) {
        return new SubmitUrlResult(ResultType.DEDUPED, requestId, status, null);
    }
}