package org.homechef.core.adapter.out.persistence;

import org.homechef.core.adapter.out.persistence.entity.ParseRequestEntity;
import org.homechef.core.adapter.out.persistence.mapper.ParseRequestMapper;
import org.homechef.core.adapter.out.persistence.repository.SpringDataParseRequestRepository;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.UrlHash;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ParseRequestRepositoryAdapter implements ParseRequestRepository {

    private final SpringDataParseRequestRepository springDataRepository;
    private final ParseRequestMapper mapper;

    public ParseRequestRepositoryAdapter(SpringDataParseRequestRepository springDataRepository,
                                         ParseRequestMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ParseRequest> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ParseRequest> findInFlightByUrlHash(UrlHash urlHash) {
        return springDataRepository.findInFlightByUrlHash(urlHash.value())
                .map(mapper::toDomain);
    }

    @Override
    public ParseRequest save(ParseRequest parseRequest) {
        ParseRequestEntity entity = mapper.toEntity(parseRequest);
        ParseRequestEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void updateStatus(UUID id, String status, String errorMessage) {
        springDataRepository.updateStatus(id, status, errorMessage);
    }
}