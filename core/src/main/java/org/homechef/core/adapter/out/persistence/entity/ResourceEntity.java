package org.homechef.core.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Persistence entity for the resource table.
 */
@Table("resource")
public record ResourceEntity(
        @Id @Column("url_hash") String urlHash,
        @Column("url") String url,
        @Column("created_at") Instant createdAt
) {
}