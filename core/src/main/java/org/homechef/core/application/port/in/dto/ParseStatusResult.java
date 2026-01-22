package org.homechef.core.application.port.in.dto;

import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.ParseStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of polling for parse request status.
 */
public record ParseStatusResult(
        UUID requestId,
        ParseStatus status,
        String errorMessage,           // present for FAILED
        SubmitUrlResult.RecipeData recipe  // present for COMPLETED
) {
    public static ParseStatusResult pending(UUID requestId) {
        return new ParseStatusResult(requestId, ParseStatus.PENDING, null, null);
    }

    public static ParseStatusResult processing(UUID requestId) {
        return new ParseStatusResult(requestId, ParseStatus.PROCESSING, null, null);
    }

    public static ParseStatusResult completed(UUID requestId, String urlHash, String title,
                                              List<Ingredient> ingredients, Instant parsedAt) {
        List<SubmitUrlResult.IngredientData> ingredientData = ingredients.stream()
                .map(SubmitUrlResult.IngredientData::from)
                .toList();
        return new ParseStatusResult(
                requestId,
                ParseStatus.COMPLETED,
                null,
                new SubmitUrlResult.RecipeData(urlHash, title, ingredientData, parsedAt)
        );
    }

    public static ParseStatusResult failed(UUID requestId, String errorMessage) {
        return new ParseStatusResult(requestId, ParseStatus.FAILED, errorMessage, null);
    }
}
