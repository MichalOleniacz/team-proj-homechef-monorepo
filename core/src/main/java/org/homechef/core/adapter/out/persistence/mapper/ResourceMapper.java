package org.homechef.core.adapter.out.persistence.mapper;

import org.homechef.core.adapter.out.persistence.entity.ResourceEntity;
import org.homechef.core.domain.recipe.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public ResourceEntity toEntity(Resource domain) {
        return new ResourceEntity(
                domain.getUrlHash().value(),
                domain.getUrl(),
                domain.getCreatedAt()
        );
    }

    public Resource toDomain(ResourceEntity entity) {
        return Resource.reconstitute(
                entity.urlHash(),
                entity.url(),
                entity.createdAt()
        );
    }
}