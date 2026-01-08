package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.ResourceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Spring Data JDBC repository for ResourceEntity.
 */
public interface SpringDataResourceRepository extends CrudRepository<ResourceEntity, String> {

    Optional<ResourceEntity> findByUrlHash(String urlHash);
}