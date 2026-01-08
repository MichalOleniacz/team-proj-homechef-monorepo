package org.homechef.core.adapter.out.persistence;

import org.homechef.core.adapter.out.persistence.entity.ResourceEntity;
import org.homechef.core.adapter.out.persistence.mapper.ResourceMapper;
import org.homechef.core.adapter.out.persistence.repository.SpringDataResourceRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.Resource;
import org.homechef.core.domain.recipe.UrlHash;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ResourceRepositoryAdapter implements ResourceRepository {

    private final SpringDataResourceRepository springDataRepository;
    private final ResourceMapper mapper;

    public ResourceRepositoryAdapter(SpringDataResourceRepository springDataRepository,
                                     ResourceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Resource> findByUrlHash(UrlHash urlHash) {
        return springDataRepository.findByUrlHash(urlHash.value())
                .map(mapper::toDomain);
    }

    @Override
    public Resource save(Resource resource) {
        ResourceEntity entity = mapper.toEntity(resource);
        ResourceEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByUrlHash(UrlHash urlHash) {
        return springDataRepository.existsById(urlHash.value());
    }
}