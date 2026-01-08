package org.homechef.core.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Persistence entity for the recipe table.
 * Ingredients stored as JSONB string, parsed in mapper.
 */
@Table("recipe")
public record RecipeEntity(
        @Id @Column("url_hash") String urlHash,
        @Column("title") String title,
        @Column("ingredients") String ingredients, // JSONB as String, converted in mapper
        @Column("parsed_at") Instant parsedAt
) {
}