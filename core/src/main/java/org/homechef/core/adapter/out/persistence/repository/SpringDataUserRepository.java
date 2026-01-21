package org.homechef.core.adapter.out.persistence.repository;

import org.homechef.core.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data JDBC repository for UserEntity.
 */
public interface SpringDataUserRepository extends CrudRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("INSERT INTO app_user (id, email, password_hash, created_at) VALUES (:id, :email, :passwordHash, :createdAt)")
    void insertUser(@Param("id") String id, @Param("email") String email,
                    @Param("passwordHash") String passwordHash, @Param("createdAt") Instant createdAt);

    @Modifying
    @Query("UPDATE app_user SET email = :email, password_hash = :passwordHash WHERE id = :id")
    void updateUser(@Param("id") String id, @Param("email") String email,
                    @Param("passwordHash") String passwordHash);
}
