package org.homechef.core.application.port.out;

import org.homechef.core.domain.user.Email;
import org.homechef.core.domain.user.User;
import org.homechef.core.domain.user.UserId;

import java.util.Optional;

/**
 * Output port for user persistence operations.
 */
public interface UserRepository {

    /**
     * Saves a user (insert or update).
     */
    User save(User user);

    /**
     * Finds a user by ID.
     */
    Optional<User> findById(UserId id);

    /**
     * Finds a user by email.
     */
    Optional<User> findByEmail(Email email);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(Email email);
}
