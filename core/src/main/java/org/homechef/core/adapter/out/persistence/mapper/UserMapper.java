package org.homechef.core.adapter.out.persistence.mapper;

import org.homechef.core.adapter.out.persistence.entity.UserEntity;
import org.homechef.core.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {
        return new UserEntity(
                domain.getId().value().toString(),
                domain.getEmail().value(),
                domain.getPassword().value(),
                domain.getCreatedAt()
        );
    }

    public User toDomain(UserEntity entity) {
        return User.reconstitute(
                UUID.fromString(entity.id()),
                entity.email(),
                entity.passwordHash(),
                entity.createdAt()
        );
    }
}
