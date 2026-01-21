package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.RecipeEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JDBC repository for RecipeEntity.
 */
public interface SpringDataRecipeRepository extends CrudRepository<RecipeEntity, String> {

    Optional<RecipeEntity> findByUrlHash(String urlHash);

    /**
     * Finds a recipe that is still fresh (parsed within TTL).
     */
    @Query("SELECT * FROM recipe WHERE url_hash = :urlHash AND parsed_at > now() - CAST(:ttlDays || ' days' AS INTERVAL)")
    Optional<RecipeEntity> findFreshByUrlHash(@Param("urlHash") String urlHash, @Param("ttlDays") int ttlDays);

    /**
     * Inserts a new recipe (used for assigned IDs to bypass Spring Data JDBC's isNew() logic).
     */
    @Modifying
    @Query("INSERT INTO recipe (url_hash, title, ingredients, parsed_at) VALUES (:urlHash, :title, CAST(:ingredients AS JSONB), :parsedAt)")
    void insertRecipe(@Param("urlHash") String urlHash, @Param("title") String title,
                      @Param("ingredients") String ingredients, @Param("parsedAt") Instant parsedAt);

    /**
     * Updates an existing recipe.
     */
    @Modifying
    @Query("UPDATE recipe SET title = :title, ingredients = CAST(:ingredients AS JSONB), parsed_at = :parsedAt WHERE url_hash = :urlHash")
    void updateRecipe(@Param("urlHash") String urlHash, @Param("title") String title,
                      @Param("ingredients") String ingredients, @Param("parsedAt") Instant parsedAt);
}