package org.homechef.core.application.service;

import org.homechef.core.application.port.in.dto.SubmitUrlCommand;
import org.homechef.core.application.port.in.dto.SubmitUrlResult;
import org.homechef.core.application.port.out.ParseEventPublisher;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.RecipeRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
@DisplayName("SubmitUrlService")
class SubmitUrlServiceTest {

    private static final String SAMPLE_URL = "https://example.com/recipe/lasagna";
    private static final UUID SAMPLE_USER_ID = UUID.randomUUID();

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ParseRequestRepository parseRequestRepository;

    @Mock
    private ParseEventPublisher parseEventPublisher;

    @Captor
    private ArgumentCaptor<ParseRequest> parseRequestCaptor;

    @Captor
    private ArgumentCaptor<Resource> resourceCaptor;

    private SubmitUrlService service;

    @BeforeEach
    void setUp() {
        service = new SubmitUrlService(
            resourceRepository,
            recipeRepository,
            parseRequestRepository,
            parseEventPublisher
        );
    }

    @Nested
    @DisplayName("cache hit path")
    class CacheHitPath {

        @Test
        @DisplayName("returns cached recipe when fresh recipe exists")
        void returnsCachedRecipeWhenFreshExists() {
            // Given
            UrlHash urlHash = UrlHash.fromUrl(SAMPLE_URL);
            Recipe freshRecipe = Recipe.create(
                urlHash,
                "World's Best Lasagna",
                List.of(
                    Ingredient.of(new BigDecimal("1"), "lb", "ground beef"),
                    Ingredient.of(new BigDecimal("2"), "cups", "ricotta cheese")
                )
            );

            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(freshRecipe));

            SubmitUrlCommand command = SubmitUrlCommand.forUser(SAMPLE_URL, SAMPLE_USER_ID);

            // When
            SubmitUrlResult result = service.execute(command);

            // Then
            assertEquals(SubmitUrlResult.ResultType.CACHED, result.type());
            assertNotNull(result.recipe());
            assertEquals("World's Best Lasagna", result.recipe().title());
            assertEquals(2, result.recipe().ingredients().size());
            assertNull(result.requestId());

            // Should not interact with other repositories
            verifyNoInteractions(parseRequestRepository);
            verifyNoInteractions(resourceRepository);
            verifyNoInteractions(parseEventPublisher);
        }
    }

    @Nested
    @DisplayName("dedup path")
    class DedupPath {

        @Test
        @DisplayName("returns existing request when in-flight request exists")
        void returnsExistingRequestWhenInFlightExists() {
            // Given
            UrlHash urlHash = UrlHash.fromUrl(SAMPLE_URL);
            UUID existingRequestId = UUID.randomUUID();
            ParseRequest existingRequest = ParseRequest.reconstitute(
                existingRequestId,
                UUID.randomUUID(),
                urlHash.value(),
                ParseStatus.PROCESSING,
                null,
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(30)
            );

            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(existingRequest));

            SubmitUrlCommand command = SubmitUrlCommand.forUser(SAMPLE_URL, SAMPLE_USER_ID);

            // When
            SubmitUrlResult result = service.execute(command);

            // Then
            assertEquals(SubmitUrlResult.ResultType.DEDUPED, result.type());
            assertEquals(existingRequestId, result.requestId());
            assertEquals(ParseStatus.PROCESSING, result.status());
            assertNull(result.recipe());

            // Should not create new resources or publish events
            verify(resourceRepository, never()).save(any());
            verifyNoInteractions(parseEventPublisher);
        }

        @Test
        @DisplayName("returns PENDING status when existing request is PENDING")
        void returnsPendingStatusForPendingRequest() {
            // Given
            UrlHash urlHash = UrlHash.fromUrl(SAMPLE_URL);
            ParseRequest pendingRequest = ParseRequest.reconstitute(
                UUID.randomUUID(),
                null,
                urlHash.value(),
                ParseStatus.PENDING,
                null,
                Instant.now(),
                Instant.now()
            );

            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(pendingRequest));

            SubmitUrlCommand command = SubmitUrlCommand.forGuest(SAMPLE_URL);

            // When
            SubmitUrlResult result = service.execute(command);

            // Then
            assertEquals(SubmitUrlResult.ResultType.DEDUPED, result.type());
            assertEquals(ParseStatus.PENDING, result.status());
        }
    }

    @Nested
    @DisplayName("cache miss path")
    class CacheMissPath {

        @Test
        @DisplayName("creates new parse request when no cache or in-flight exists")
        void createsNewParseRequest() {
            // Given
            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(resourceRepository.findByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());

            Resource savedResource = Resource.create(SAMPLE_URL);
            when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);

            when(parseRequestRepository.save(any(ParseRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            SubmitUrlCommand command = SubmitUrlCommand.forUser(SAMPLE_URL, SAMPLE_USER_ID);

            // When
            SubmitUrlResult result = service.execute(command);

            // Then
            assertEquals(SubmitUrlResult.ResultType.PENDING, result.type());
            assertNotNull(result.requestId());
            assertEquals(ParseStatus.PENDING, result.status());
            assertNull(result.recipe());

            // Verify resource was created
            verify(resourceRepository).save(resourceCaptor.capture());
            assertEquals(SAMPLE_URL.trim(), resourceCaptor.getValue().getUrl());

            // Verify parse request was saved
            verify(parseRequestRepository).save(parseRequestCaptor.capture());
            ParseRequest savedRequest = parseRequestCaptor.getValue();
            assertEquals(ParseStatus.PENDING, savedRequest.getStatus());
            assertEquals(SAMPLE_USER_ID, savedRequest.getUserId());

            // Verify event was published
            verify(parseEventPublisher).publishParseRequest(any(ParseRequest.class), eq(SAMPLE_URL));
        }

        @Test
        @DisplayName("reuses existing resource when resource already exists")
        void reusesExistingResource() {
            // Given
            Resource existingResource = Resource.create(SAMPLE_URL);

            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(resourceRepository.findByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(existingResource));
            when(parseRequestRepository.save(any(ParseRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            SubmitUrlCommand command = SubmitUrlCommand.forUser(SAMPLE_URL, SAMPLE_USER_ID);

            // When
            service.execute(command);

            // Then - resource should not be saved again
            verify(resourceRepository, never()).save(any());
            verify(parseEventPublisher).publishParseRequest(any(), eq(existingResource.getUrl()));
        }

        @Test
        @DisplayName("creates guest request with null userId")
        void createsGuestRequest() {
            // Given
            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(resourceRepository.findByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(Resource.create(SAMPLE_URL)));
            when(parseRequestRepository.save(any(ParseRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            SubmitUrlCommand command = SubmitUrlCommand.forGuest(SAMPLE_URL);

            // When
            service.execute(command);

            // Then
            verify(parseRequestRepository).save(parseRequestCaptor.capture());
            assertNull(parseRequestCaptor.getValue().getUserId());
        }
    }

    @Nested
    @DisplayName("URL hash consistency")
    class UrlHashConsistency {

        @Test
        @DisplayName("uses same URL hash for all lookups")
        void usesSameUrlHashForAllLookups() {
            // Given
            when(recipeRepository.findFreshByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(parseRequestRepository.findInFlightByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.empty());
            when(resourceRepository.findByUrlHash(any(UrlHash.class)))
                .thenReturn(Optional.of(Resource.create(SAMPLE_URL)));
            when(parseRequestRepository.save(any(ParseRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            SubmitUrlCommand command = SubmitUrlCommand.forUser(SAMPLE_URL, SAMPLE_USER_ID);

            // When
            service.execute(command);

            // Then - capture all URL hashes used
            ArgumentCaptor<UrlHash> recipeHashCaptor = ArgumentCaptor.forClass(UrlHash.class);
            ArgumentCaptor<UrlHash> parseRequestHashCaptor = ArgumentCaptor.forClass(UrlHash.class);
            ArgumentCaptor<UrlHash> resourceHashCaptor = ArgumentCaptor.forClass(UrlHash.class);

            verify(recipeRepository).findFreshByUrlHash(recipeHashCaptor.capture());
            verify(parseRequestRepository).findInFlightByUrlHash(parseRequestHashCaptor.capture());
            verify(resourceRepository).findByUrlHash(resourceHashCaptor.capture());

            // All should be the same hash
            String expectedHash = UrlHash.fromUrl(SAMPLE_URL).value();
            assertEquals(expectedHash, recipeHashCaptor.getValue().value());
            assertEquals(expectedHash, parseRequestHashCaptor.getValue().value());
            assertEquals(expectedHash, resourceHashCaptor.getValue().value());
        }
    }
}
