package org.homechef.core.adapter.out.security;

import org.homechef.core.application.port.out.PasswordEncoder;
import org.homechef.core.domain.user.HashedPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt implementation of the PasswordEncoder port.
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt;

    public BCryptPasswordEncoderAdapter() {
        this.bcrypt = new BCryptPasswordEncoder();
    }

    @Override
    public HashedPassword encode(String rawPassword) {
        String encoded = bcrypt.encode(rawPassword);
        return HashedPassword.of(encoded);
    }

    @Override
    public boolean matches(String rawPassword, HashedPassword encoded) {
        return bcrypt.matches(rawPassword, encoded.value());
    }
}
