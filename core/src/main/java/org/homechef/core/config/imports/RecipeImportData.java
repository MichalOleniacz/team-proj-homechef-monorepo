package org.homechef.core.config.imports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for deserializing recipe data from JSON import files.
 * Matches expected LLM output format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RecipeImportData(
        String url,
        String title,
        List<IngredientImportData> ingredients
) {
    /**
     * DTO for ingredient data in import files.
     * Quantity and unit are optional (e.g., "salt to taste").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IngredientImportData(
            String quantity,
            String unit,
            String name
    ) {}
}
