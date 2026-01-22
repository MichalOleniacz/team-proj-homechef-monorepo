package org.homechef.core.adapter.out.persistence.mapper;

import org.homechef.core.adapter.out.persistence.entity.RecipeEntity;
import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Component
public class RecipeMapper {

    private static final Logger log = LoggerFactory.getLogger(RecipeMapper.class);
    private static final TypeReference<List<IngredientJson>> INGREDIENT_LIST_TYPE = new TypeReference<>() {};

    private final JsonMapper jsonMapper;

    public RecipeMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public RecipeEntity toEntity(Recipe domain) {
        String ingredientsJson = serializeIngredients(domain.getIngredients());
        return new RecipeEntity(
                domain.getUrlHash().value(),
                domain.getTitle(),
                ingredientsJson,
                domain.getParsedAt()
        );
    }

    public Recipe toDomain(RecipeEntity entity) {
        List<Ingredient> ingredients = deserializeIngredients(entity.ingredients());
        return Recipe.reconstitute(
                entity.urlHash(),
                entity.title(),
                ingredients,
                entity.parsedAt()
        );
    }

    private String serializeIngredients(List<Ingredient> ingredients) {
        try {
            List<IngredientJson> jsonList = ingredients.stream()
                    .map(i -> new IngredientJson(i.quantity(), i.unit(), i.name()))
                    .toList();
            return jsonMapper.writeValueAsString(jsonList);
        } catch (DatabindException e) {
            log.error("Failed to serialize ingredients", e);
            return "[]";
        }
    }

    private List<Ingredient> deserializeIngredients(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<IngredientJson> jsonList = jsonMapper.readValue(json, INGREDIENT_LIST_TYPE);
            return jsonList.stream()
                    .map(j -> Ingredient.of(j.quantity(), j.unit(), j.name()))
                    .toList();
        } catch (DatabindException e) {
            log.error("Failed to deserialize ingredients: {}", json, e);
            return List.of();
        }
    }

    /**
     * Internal DTO for JSON serialization of ingredients.
     */
    private record IngredientJson(
            java.math.BigDecimal quantity,
            String unit,
            String name
    ) {}
}