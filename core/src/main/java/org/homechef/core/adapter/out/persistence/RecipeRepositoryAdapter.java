package org.homechef.core.adapter.out.persistence;

import org.homechef.core.adapter.out.persistence.entity.RecipeEntity;
import org.homechef.core.adapter.out.persistence.mapper.RecipeMapper;
import org.homechef.core.adapter.out.persistence.repository.SpringDataRecipeRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.UrlHash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RecipeRepositoryAdapter implements RecipeRepository {

    private final SpringDataRecipeRepository springDataRepository;
    private final RecipeMapper mapper;
    private final int recipeTtlDays;

    public RecipeRepositoryAdapter(SpringDataRecipeRepository springDataRepository,
                                   RecipeMapper mapper,
                                   @Value("${homechef.recipe.ttl-days:30}") int recipeTtlDays) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
        this.recipeTtlDays = recipeTtlDays;
    }

    @Override
    public Optional<Recipe> findByUrlHash(UrlHash urlHash) {
        return springDataRepository.findByUrlHash(urlHash.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Recipe> findFreshByUrlHash(UrlHash urlHash) {
        return springDataRepository.findFreshByUrlHash(urlHash.value(), recipeTtlDays)
                .map(mapper::toDomain);
    }

    @Override
    public Recipe save(Recipe recipe) {
        RecipeEntity entity = mapper.toEntity(recipe);
        boolean exists = springDataRepository.existsById(entity.urlHash());
        if (exists) {
            // Update existing recipe
            springDataRepository.updateRecipe(entity.urlHash(), entity.title(),
                    entity.ingredients(), entity.parsedAt());
        } else {
            // Insert new recipe using custom query (avoids Spring Data JDBC's isNew() issue with assigned IDs)
            springDataRepository.insertRecipe(entity.urlHash(), entity.title(),
                    entity.ingredients(), entity.parsedAt());
        }
        return recipe;
    }
}