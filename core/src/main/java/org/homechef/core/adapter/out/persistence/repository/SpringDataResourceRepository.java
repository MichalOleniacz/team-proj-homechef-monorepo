package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.ResourceEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JDBC repository for ResourceEntity.
 */
public interface SpringDataResourceRepository extends CrudRepository<ResourceEntity, String> {

    Optional<ResourceEntity> findByUrlHash(String urlHash);

    @Modifying
    @Query("INSERT INTO resource (url_hash, url, created_at) VALUES (:urlHash, :url, :createdAt)")
    void insertResource(String urlHash, String url, Instant createdAt);
}