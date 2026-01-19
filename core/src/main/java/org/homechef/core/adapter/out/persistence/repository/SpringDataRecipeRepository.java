package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.RecipeEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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
}