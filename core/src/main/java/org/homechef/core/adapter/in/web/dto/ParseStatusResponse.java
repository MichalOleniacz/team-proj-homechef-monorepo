package org.homechef.core.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.homechef.core.application.port.in.dto.ParseStatusResult;
import org.homechef.core.domain.recipe.ParseStatus;

import java.util.UUID;

/**
 * REST response for polling parse request status.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParseStatusResponse(
        UUID requestId,
        ParseStatus status,
        String error,
        SubmitUrlResponse.RecipeResponse recipe
) {
    public static ParseStatusResponse from(ParseStatusResult result) {
        return new ParseStatusResponse(
                result.requestId(),
                result.status(),
                result.errorMessage(),
                result.recipe() != null ? SubmitUrlResponse.RecipeResponse.from(result.recipe()) : null
        );
    }
}