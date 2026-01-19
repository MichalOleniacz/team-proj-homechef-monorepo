package org.homechef.core.application.service;

import org.homechef.core.application.port.in.SubmitUrlUseCase;
import org.homechef.core.application.port.in.dto.SubmitUrlCommand;
import org.homechef.core.application.port.in.dto.SubmitUrlResult;
import org.homechef.core.application.port.out.ParseEventPublisher;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.Resource;
import org.homechef.core.domain.recipe.UrlHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
public class SubmitUrlService implements SubmitUrlUseCase {

    private static final Logger log = LoggerFactory.getLogger(SubmitUrlService.class);

    private final ResourceRepository resourceRepository;
    private final RecipeRepository recipeRepository;
    private final ParseRequestRepository parseRequestRepository;
    private final ParseEventPublisher parseEventPublisher;

    public SubmitUrlService(ResourceRepository resourceRepository,
                            RecipeRepository recipeRepository,
                            ParseRequestRepository parseRequestRepository,
                            ParseEventPublisher parseEventPublisher) {
        this.resourceRepository = resourceRepository;
        this.recipeRepository = recipeRepository;
        this.parseRequestRepository = parseRequestRepository;
        this.parseEventPublisher = parseEventPublisher;
    }

    @Override
    @Transactional
    public SubmitUrlResult execute(SubmitUrlCommand command) {
        String url = command.url();
        UrlHash urlHash = UrlHash.fromUrl(url);

        log.info("Processing URL submission",
                kv("urlHash", urlHash.value()),
                kv("userId", command.userId()));

        // 1. Check for fresh cached recipe
        Optional<Recipe> freshRecipe = recipeRepository.findFreshByUrlHash(urlHash);
        if (freshRecipe.isPresent()) {
            Recipe recipe = freshRecipe.get();
            log.info("Cache HIT: returning fresh recipe",
                    kv("urlHash", urlHash.value()),
                    kv("outcome", "cache_hit"),
                    kv("recipeTitle", recipe.getTitle()));
            return SubmitUrlResult.cached(
                    recipe.getUrlHash().value(),
                    recipe.getTitle(),
                    recipe.getIngredients(),
                    recipe.getParsedAt()
            );
        }

        // 2. Check for in-flight request (dedup)
        Optional<ParseRequest> inFlightRequest = parseRequestRepository.findInFlightByUrlHash(urlHash);
        if (inFlightRequest.isPresent()) {
            ParseRequest existing = inFlightRequest.get();
            log.info("Dedup: returning existing request",
                    kv("urlHash", urlHash.value()),
                    kv("outcome", "dedup"),
                    kv("existingRequestId", existing.getId()),
                    kv("existingStatus", existing.getStatus()));
            return SubmitUrlResult.deduped(existing.getId(), existing.getStatus());
        }

        // 3. Cache miss - create new request
        log.info("Cache MISS: creating new parse request",
                kv("urlHash", urlHash.value()),
                kv("outcome", "cache_miss"));

        // Ensure resource exists
        Resource resource = resourceRepository.findByUrlHash(urlHash)
                .orElseGet(() -> {
                    Resource newResource = Resource.create(url);
                    return resourceRepository.save(newResource);
                });

        // Create parse request
        ParseRequest parseRequest = ParseRequest.create(urlHash, command.userId());
        parseRequest = parseRequestRepository.save(parseRequest);

        // Emit Kafka event
        parseEventPublisher.publishParseRequest(parseRequest, resource.getUrl());

        log.info("Parse request created and event published",
                kv("requestId", parseRequest.getId()),
                kv("urlHash", urlHash.value()),
                kv("outcome", "request_created"));

        return SubmitUrlResult.pending(parseRequest.getId());
    }
}