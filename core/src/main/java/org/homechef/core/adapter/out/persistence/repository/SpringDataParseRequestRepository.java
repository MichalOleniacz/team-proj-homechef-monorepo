package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.ParseRequestEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JDBC repository for ParseRequestEntity.
 */
public interface SpringDataParseRequestRepository extends CrudRepository<ParseRequestEntity, UUID> {

    /**
     * Finds an in-flight (PENDING or PROCESSING) request for the given URL hash.
     * Used for deduplication.
     */
    @Query("SELECT * FROM parse_request WHERE url_hash = :urlHash AND status IN ('PENDING', 'PROCESSING') ORDER BY created_at DESC LIMIT 1")
    Optional<ParseRequestEntity> findInFlightByUrlHash(@Param("urlHash") String urlHash);

    /**
     * Updates the status of a parse request.
     */
    @Modifying
    @Query("UPDATE parse_request SET status = :status, error_message = :errorMessage, updated_at = now() WHERE id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status, @Param("errorMessage") String errorMessage);
}