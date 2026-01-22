package org.homechef.core.adapter.in.web;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.homechef.core.adapter.in.security.AuthenticatedUser;
import org.homechef.core.adapter.in.web.dto.ParseStatusResponse;
import org.homechef.core.adapter.in.web.dto.SubmitUrlRequest;
import org.homechef.core.adapter.in.web.dto.SubmitUrlResponse;
import org.homechef.core.application.port.in.GetParseStatusUseCase;
import org.homechef.core.application.port.in.SubmitUrlUseCase;
import org.homechef.core.application.port.in.dto.ParseStatusResult;
import org.homechef.core.application.port.in.dto.SubmitUrlCommand;
import org.homechef.core.application.port.in.dto.SubmitUrlResult;
import org.homechef.core.domain.recipe.ParseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    private final SubmitUrlUseCase submitUrlUseCase;
    private final GetParseStatusUseCase getParseStatusUseCase;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter dedupCounter;

    public RecipeController(SubmitUrlUseCase submitUrlUseCase,
                            GetParseStatusUseCase getParseStatusUseCase,
                            MeterRegistry meterRegistry) {
        this.submitUrlUseCase = submitUrlUseCase;
        this.getParseStatusUseCase = getParseStatusUseCase;

        // Metrics
        this.cacheHitCounter = meterRegistry.counter("recipe.submit", "outcome", "cache_hit");
        this.cacheMissCounter = meterRegistry.counter("recipe.submit", "outcome", "cache_miss");
        this.dedupCounter = meterRegistry.counter("recipe.submit", "outcome", "dedup");
    }

    @PostMapping("/parse")
    @Timed(value = "recipe.submit.duration", description = "Time to process URL submission")
    public ResponseEntity<SubmitUrlResponse> submitUrl(@Valid @RequestBody SubmitUrlRequest request) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            log.info("Received URL submission request",
                    kv("url", request.url()),
                    kv("endpoint", "POST /api/v1/recipes/parse"));

            SubmitUrlCommand command = new SubmitUrlCommand(request.url(), AuthenticatedUser.currentUserIdOrNull());
            SubmitUrlResult result = submitUrlUseCase.execute(command);

            // Update metrics based on outcome
            recordOutcomeMetric(result.status());

            SubmitUrlResponse response = SubmitUrlResponse.from(result);

            // Determine HTTP status based on result
            if (result.status() == ParseStatus.COMPLETED) {
                // Cache hit - return 200 with recipe
                log.info("Returning cached recipe",
                        kv("status", result.status()),
                        kv("httpStatus", 200));
                return ResponseEntity.ok(response);
            } else {
                // Pending/Processing - return 202 Accepted
                log.info("Parse request accepted",
                        kv("status", result.status()),
                        kv("parseRequestId", result.requestId()),
                        kv("httpStatus", 202));
                return ResponseEntity.accepted().body(response);
            }
        } finally {
            MDC.remove("requestId");
        }
    }

    @GetMapping("/parse-requests/{id}")
    @Timed(value = "recipe.poll.duration", description = "Time to poll parse request status")
    public ResponseEntity<ParseStatusResponse> getParseStatus(@PathVariable UUID id) {
        MDC.put("parseRequestId", id.toString());

        try {
            log.debug("Polling parse request status",
                    kv("parseRequestId", id),
                    kv("endpoint", "GET /api/v1/recipes/parse-requests/{id}"));

            return getParseStatusUseCase.execute(id)
                    .map(result -> {
                        log.debug("Returning parse status",
                                kv("parseRequestId", id),
                                kv("status", result.status()));
                        return ResponseEntity.ok(ParseStatusResponse.from(result));
                    })
                    .orElseGet(() -> {
                        log.warn("Parse request not found",
                                kv("parseRequestId", id));
                        return ResponseEntity.notFound().build();
                    });
        } finally {
            MDC.remove("parseRequestId");
        }
    }

    private void recordOutcomeMetric(ParseStatus status) {
        switch (status) {
            case COMPLETED -> cacheHitCounter.increment();
            case PENDING -> cacheMissCounter.increment();
            case PROCESSING -> dedupCounter.increment();
            default -> {} // FAILED handled elsewhere
        }
    }
}