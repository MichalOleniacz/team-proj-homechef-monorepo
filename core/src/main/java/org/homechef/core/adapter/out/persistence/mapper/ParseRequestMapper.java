package org.homechef.core.adapter.out.persistence.mapper;

import org.homechef.core.adapter.out.persistence.entity.ParseRequestEntity;
import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.ParseStatus;
import org.springframework.stereotype.Component;

@Component
public class ParseRequestMapper {

    public ParseRequestEntity toEntity(ParseRequest domain) {
        return new ParseRequestEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getUrlHash().value(),
                domain.getStatus().name(),
                domain.getErrorMessage(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public ParseRequest toDomain(ParseRequestEntity entity) {
        return ParseRequest.reconstitute(
                entity.id(),
                entity.userId(),
                entity.urlHash(),
                ParseStatus.valueOf(entity.status()),
                entity.errorMessage(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }
}