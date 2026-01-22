package org.homechef.core.application.service;

import org.homechef.core.application.port.in.GetParseStatusUseCase;
import org.homechef.core.application.port.in.dto.ParseStatusResult;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.ParseStatus;
import org.homechef.core.domain.recipe.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Transactional(readOnly = true)
public class GetParseStatusService implements GetParseStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetParseStatusService.class);

    private final ParseRequestRepository parseRequestRepository;
    private final RecipeRepository recipeRepository;

    public GetParseStatusService(ParseRequestRepository parseRequestRepository,
                                 RecipeRepository recipeRepository) {
        this.parseRequestRepository = parseRequestRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Optional<ParseStatusResult> execute(UUID requestId) {
        log.debug("Polling parse request status", kv("requestId", requestId));

        Optional<ParseRequest> maybeRequest = parseRequestRepository.findById(requestId);
        if (maybeRequest.isEmpty()) {
            log.warn("Parse request not found", kv("requestId", requestId));
            return Optional.empty();
        }

        ParseRequest request = maybeRequest.get();
        ParseStatus status = request.getStatus();

        log.debug("Parse request found",
                kv("requestId", requestId),
                kv("status", status),
                kv("urlHash", request.getUrlHash().value()));

        return Optional.of(switch (status) {
            case PENDING -> ParseStatusResult.pending(requestId);
            case PROCESSING -> ParseStatusResult.processing(requestId);
            case FAILED -> ParseStatusResult.failed(requestId, request.getErrorMessage());
            case COMPLETED -> {
                // Fetch the recipe for completed requests
                Optional<Recipe> recipe = recipeRepository.findByUrlHash(request.getUrlHash());
                if (recipe.isEmpty()) {
                    log.error("Inconsistent state: COMPLETED request but no recipe found",
                            kv("requestId", requestId),
                            kv("urlHash", request.getUrlHash().value()));
                    yield ParseStatusResult.failed(requestId, "Recipe not found after completion");
                }
                Recipe r = recipe.get();
                yield ParseStatusResult.completed(
                        requestId,
                        r.getUrlHash().value(),
                        r.getTitle(),
                        r.getIngredients(),
                        r.getParsedAt()
                );
            }
        });
    }
}