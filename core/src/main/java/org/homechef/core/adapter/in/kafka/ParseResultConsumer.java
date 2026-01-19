package org.homechef.core.adapter.in.kafka;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.Ingredient;
import org.homechef.core.domain.recipe.ParseStatus;
import org.homechef.core.domain.recipe.Recipe;
import org.homechef.core.domain.recipe.UrlHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class ParseResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(ParseResultConsumer.class);

    private final ParseRequestRepository parseRequestRepository;
    private final RecipeRepository recipeRepository;
    private final ResourceRepository resourceRepository;
    private final Counter successCounter;
    private final Counter failureCounter;

    public ParseResultConsumer(
            ParseRequestRepository parseRequestRepository,
            RecipeRepository recipeRepository,
            ResourceRepository resourceRepository,
            MeterRegistry meterRegistry) {
        this.parseRequestRepository = parseRequestRepository;
        this.recipeRepository = recipeRepository;
        this.resourceRepository = resourceRepository;
        this.successCounter = meterRegistry.counter("kafka.consume", "topic", "parse-results", "outcome", "success");
        this.failureCounter = meterRegistry.counter("kafka.consume", "topic", "parse-results", "outcome", "failure");
    }

    @KafkaListener(topics = "${homechef.kafka.topic.parse-result:parse-results}", groupId = "${spring.kafka.consumer.group-id:homechef-core}")
    @Transactional
    public void handleParseResult(ParseResultEvent event) {
        MDC.put("requestId", event.requestId().toString());
        MDC.put("urlHash", event.urlHash());

        try {
            log.info("Received parse result event",
                    kv("requestId", event.requestId()),
                    kv("urlHash", event.urlHash()),
                    kv("success", event.success()));

            if (event.success()) {
                handleSuccess(event);
                successCounter.increment();
            } else {
                handleFailure(event);
                failureCounter.increment();
            }
        } catch (Exception e) {
            log.error("Error processing parse result event",
                    kv("requestId", event.requestId()),
                    kv("urlHash", event.urlHash()),
                    kv("error", e.getMessage()), e);
            failureCounter.increment();
            throw e; // Let Kafka handle retry
        } finally {
            MDC.remove("requestId");
            MDC.remove("urlHash");
        }
    }

    private void handleSuccess(ParseResultEvent event) {
        UrlHash urlHash = UrlHash.fromHash(event.urlHash());

        // Verify resource exists
        if (!resourceRepository.existsByUrlHash(urlHash)) {
            log.warn("Resource not found for parse result, skipping",
                    kv("requestId", event.requestId()),
                    kv("urlHash", event.urlHash()));
            return;
        }

        // Convert ingredients
        List<Ingredient> ingredients = event.ingredients().stream()
                .map(this::toIngredient)
                .toList();

        // Upsert recipe
        Recipe recipe = Recipe.create(urlHash, event.title(), ingredients);
        recipeRepository.save(recipe);

        // Update parse request status
        parseRequestRepository.updateStatus(
                event.requestId(),
                ParseStatus.COMPLETED.name(),
                null
        );

        log.info("Parse result processed successfully",
                kv("requestId", event.requestId()),
                kv("urlHash", event.urlHash()),
                kv("title", event.title()),
                kv("ingredientCount", ingredients.size()));
    }

    private void handleFailure(ParseResultEvent event) {
        parseRequestRepository.updateStatus(
                event.requestId(),
                ParseStatus.FAILED.name(),
                event.errorMessage()
        );

        log.warn("Parse result failed",
                kv("requestId", event.requestId()),
                kv("urlHash", event.urlHash()),
                kv("errorMessage", event.errorMessage()));
    }

    private Ingredient toIngredient(ParseResultEvent.IngredientEvent e) {
        BigDecimal quantity = null;
        if (e.quantity() != null && !e.quantity().isBlank()) {
            try {
                quantity = new BigDecimal(e.quantity());
            } catch (NumberFormatException ex) {
                log.debug("Could not parse quantity as number: {}", e.quantity());
            }
        }
        return Ingredient.of(quantity, e.unit(), e.name());
    }
}