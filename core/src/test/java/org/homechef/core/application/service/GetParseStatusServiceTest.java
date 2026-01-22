package org.homechef.core.application.service;

import org.homechef.core.application.port.in.dto.ParseStatusResult;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.domain.recipe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetParseStatusService")
class GetParseStatusServiceTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UrlHash SAMPLE_URL_HASH = UrlHash.fromUrl("https://example.com/recipe");

    @Mock
    private ParseRequestRepository parseRequestRepository;

    @Mock
    private RecipeRepository recipeRepository;

    private GetParseStatusService service;

    @BeforeEach
    void setUp() {
        service = new GetParseStatusService(parseRequestRepository, recipeRepository);
    }

    @Nested
    @DisplayName("request not found")
    class RequestNotFound {

        @Test
        @DisplayName("returns empty when request does not exist")
        void returnsEmptyWhenNotFound() {
            // Given
            when(parseRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isEmpty());
            verifyNoInteractions(recipeRepository);
        }
    }

    @Nested
    @DisplayName("PENDING status")
    class PendingStatus {

        @Test
        @DisplayName("returns PENDING result for pending request")
        void returnsPendingResult() {
            // Given
            ParseRequest pendingRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                UUID.randomUUID(),
                SAMPLE_URL_HASH.value(),
                ParseStatus.PENDING,
                null,
                Instant.now(),
                Instant.now()
            );

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(pendingRequest));

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isPresent());
            assertEquals(REQUEST_ID, result.get().requestId());
            assertEquals(ParseStatus.PENDING, result.get().status());
            assertNull(result.get().errorMessage());
            assertNull(result.get().recipe());

            // Should not query recipe repository for pending requests
            verifyNoInteractions(recipeRepository);
        }
    }

    @Nested
    @DisplayName("PROCESSING status")
    class ProcessingStatus {

        @Test
        @DisplayName("returns PROCESSING result for processing request")
        void returnsProcessingResult() {
            // Given
            ParseRequest processingRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                UUID.randomUUID(),
                SAMPLE_URL_HASH.value(),
                ParseStatus.PROCESSING,
                null,
                Instant.now().minusSeconds(30),
                Instant.now()
            );

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(processingRequest));

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isPresent());
            assertEquals(ParseStatus.PROCESSING, result.get().status());
            assertNull(result.get().recipe());

            verifyNoInteractions(recipeRepository);
        }
    }

    @Nested
    @DisplayName("FAILED status")
    class FailedStatus {

        @Test
        @DisplayName("returns FAILED result with error message")
        void returnsFailedResultWithErrorMessage() {
            // Given
            String errorMessage = "Failed to parse recipe: timeout";
            ParseRequest failedRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                UUID.randomUUID(),
                SAMPLE_URL_HASH.value(),
                ParseStatus.FAILED,
                errorMessage,
                Instant.now().minusSeconds(60),
                Instant.now()
            );

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(failedRequest));

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isPresent());
            assertEquals(ParseStatus.FAILED, result.get().status());
            assertEquals(errorMessage, result.get().errorMessage());
            assertNull(result.get().recipe());

            verifyNoInteractions(recipeRepository);
        }
    }

    @Nested
    @DisplayName("COMPLETED status")
    class CompletedStatus {

        @Test
        @DisplayName("returns COMPLETED result with recipe data")
        void returnsCompletedResultWithRecipe() {
            // Given
            ParseRequest completedRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                UUID.randomUUID(),
                SAMPLE_URL_HASH.value(),
                ParseStatus.COMPLETED,
                null,
                Instant.now().minusSeconds(60),
                Instant.now()
            );

            Recipe recipe = Recipe.create(
                SAMPLE_URL_HASH,
                "Delicious Pasta",
                List.of(
                    Ingredient.of(new BigDecimal("500"), "g", "pasta"),
                    Ingredient.of(new BigDecimal("2"), "cups", "tomato sauce")
                )
            );

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(completedRequest));
            when(recipeRepository.findByUrlHash(SAMPLE_URL_HASH))
                .thenReturn(Optional.of(recipe));

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isPresent());
            assertEquals(ParseStatus.COMPLETED, result.get().status());
            assertNull(result.get().errorMessage());
            assertNotNull(result.get().recipe());
            assertEquals("Delicious Pasta", result.get().recipe().title());
            assertEquals(2, result.get().recipe().ingredients().size());
        }

        @Test
        @DisplayName("returns FAILED when recipe not found for completed request (inconsistent state)")
        void returnsFailedWhenRecipeNotFound() {
            // Given - completed request but no recipe (data inconsistency)
            ParseRequest completedRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                UUID.randomUUID(),
                SAMPLE_URL_HASH.value(),
                ParseStatus.COMPLETED,
                null,
                Instant.now().minusSeconds(60),
                Instant.now()
            );

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(completedRequest));
            when(recipeRepository.findByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then - should handle gracefully by returning failed
            assertTrue(result.isPresent());
            assertEquals(ParseStatus.FAILED, result.get().status());
            assertEquals("Recipe not found after completion", result.get().errorMessage());
            assertNull(result.get().recipe());
        }
    }

    @Nested
    @DisplayName("URL hash lookup")
    class UrlHashLookup {

        @Test
        @DisplayName("uses URL hash from request to find recipe")
        void usesUrlHashFromRequestToFindRecipe() {
            // Given
            UrlHash specificHash = UrlHash.fromUrl("https://specific.com/recipe/123");
            ParseRequest completedRequest = ParseRequest.reconstitute(
                REQUEST_ID,
                null,
                specificHash.value(),
                ParseStatus.COMPLETED,
                null,
                Instant.now(),
                Instant.now()
            );

            Recipe recipe = Recipe.create(specificHash, "Specific Recipe", List.of());

            when(parseRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(completedRequest));
            when(recipeRepository.findByUrlHash(argThat(hash ->
                hash.value().equals(specificHash.value()))))
                .thenReturn(Optional.of(recipe));

            // When
            Optional<ParseStatusResult> result = service.execute(REQUEST_ID);

            // Then
            assertTrue(result.isPresent());
            assertEquals(specificHash.value(), result.get().recipe().urlHash());
        }
    }
}
