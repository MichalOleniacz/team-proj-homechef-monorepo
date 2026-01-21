package org.homechef.core.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Persistence entity for the app_user table.
 */
@Table("app_user")
public record UserEntity(
        @Id @Column("id") String id,
        @Column("email") String email,
        @Column("password_hash") String passwordHash,
        @Column("created_at") Instant createdAt
) {
}
