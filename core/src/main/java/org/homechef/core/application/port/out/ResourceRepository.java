package org.homechef.core.application.port.out;

import org.homechef.core.domain.recipe.Resource;
import org.homechef.core.domain.recipe.UrlHash;

import java.util.Optional;

/**
 * Driven port for Resource persistence.
 */
public interface ResourceRepository {

    /**
     * Saves a resource. If url_hash already exists, this is a no-op (upsert semantics).
     */
    Resource save(Resource resource);

    /**
     * Finds a resource by its URL hash.
     */
    Optional<Resource> findByUrlHash(UrlHash urlHash);

    /**
     * Checks if a resource exists for the given URL hash.
     */
    boolean existsByUrlHash(UrlHash urlHash);
}