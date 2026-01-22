package org.homechef.core.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.homechef.core.application.port.in.dto.SubmitUrlResult;
import org.homechef.core.domain.recipe.ParseStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response for URL submission.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubmitUrlResponse(
        ParseStatus status,
        UUID requestId,
        RecipeResponse recipe
) {
    public static SubmitUrlResponse from(SubmitUrlResult result) {
        return new SubmitUrlResponse(
                result.status(),
                result.requestId(),
                result.recipe() != null ? RecipeResponse.from(result.recipe()) : null
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecipeResponse(
            String urlHash,
            String title,
            List<IngredientResponse> ingredients,
            Instant parsedAt
    ) {
        public static RecipeResponse from(SubmitUrlResult.RecipeData data) {
            return new RecipeResponse(
                    data.urlHash(),
                    data.title(),
                    data.ingredients().stream().map(IngredientResponse::from).toList(),
                    data.parsedAt()
            );
        }
    }

    public record IngredientResponse(
            String quantity,
            String unit,
            String name
    ) {
        public static IngredientResponse from(SubmitUrlResult.IngredientData data) {
            return new IngredientResponse(
                    data.quantity(),
                    data.unit(),
                    data.name()
            );
        }
    }
}