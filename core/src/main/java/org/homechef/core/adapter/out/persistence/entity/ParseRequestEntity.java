package org.homechef.core.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity for the parse_request table.
 */
@Table("parse_request")
public record ParseRequestEntity(
        @Id @Column("id") UUID id,
        @Column("user_id") UUID userId,
        @Column("url_hash") String urlHash,
        @Column("status") String status,
        @Column("error_message") String errorMessage,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt
) {
}