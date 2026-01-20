package org.homechef.core.adapter.out.persistence;

import org.homechef.core.adapter.out.persistence.entity.UserEntity;
import org.homechef.core.adapter.out.persistence.mapper.UserMapper;
import org.homechef.core.adapter.out.persistence.repository.SpringDataUserRepository;
import org.homechef.core.application.port.out.UserRepository;
import org.homechef.core.domain.user.Email;
import org.homechef.core.domain.user.User;
import org.homechef.core.domain.user.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(SpringDataUserRepository springDataRepository,
                                 UserMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        boolean exists = springDataRepository.existsById(entity.id());
        if (exists) {
            springDataRepository.updateUser(entity.id(), entity.email(), entity.passwordHash());
        } else {
            springDataRepository.insertUser(entity.id(), entity.email(), entity.passwordHash(), entity.createdAt());
        }
        return user;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataRepository.findById(id.value().toString())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataRepository.existsByEmail(email.value());
    }
}
