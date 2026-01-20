package org.homechef.core.adapter.out.persistence;

import org.homechef.core.IntegrationTestBase;
import org.homechef.core.application.port.out.ParseRequestRepository;
import org.homechef.core.application.port.out.ResourceRepository;
import org.homechef.core.domain.recipe.ParseRequest;
import org.homechef.core.domain.recipe.ParseStatus;
import org.homechef.core.domain.recipe.Resource;
import org.homechef.core.domain.recipe.UrlHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParseRequestRepositoryAdapter Integration")
class ParseRequestRepositoryAdapterIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ParseRequestRepository parseRequestRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String BASE_URL = "https://example.com/recipe/request-" + System.currentTimeMillis();

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM parse_request");
        jdbcTemplate.execute("DELETE FROM recipe");
        jdbcTemplate.execute("DELETE FROM resource");
    }

    private Resource createResource(String url) {
        return resourceRepository.save(Resource.create(url));
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("saves new parse request")
        void savesNewRequest() {
            // Given
            String url = BASE_URL + "-save";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());

            // When
            ParseRequest saved = parseRequestRepository.save(request);

            // Then
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals(ParseStatus.PENDING, saved.getStatus());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        @DisplayName("saves guest request with null userId")
        void savesGuestRequest() {
            // Given
            String url = BASE_URL + "-guest";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.createForGuest(resource.getUrlHash());

            // When
            ParseRequest saved = parseRequestRepository.save(request);

            // Then
            assertNotNull(saved);
            assertNull(saved.getUserId());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("finds existing request by ID")
        void findsExistingRequest() {
            // Given
            String url = BASE_URL + "-findById";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            ParseRequest saved = parseRequestRepository.save(request);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findById(saved.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(ParseStatus.PENDING, found.get().getStatus());
        }

        @Test
        @DisplayName("returns empty for non-existent ID")
        void returnsEmptyForNonExistent() {
            // When
            Optional<ParseRequest> found = parseRequestRepository.findById(UUID.randomUUID());

            // Then
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findInFlightByUrlHash()")
    class FindInFlightByUrlHash {

        @Test
        @DisplayName("finds PENDING request for URL hash")
        void findsPendingRequest() {
            // Given
            String url = BASE_URL + "-pending";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            parseRequestRepository.save(request);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.PENDING, found.get().getStatus());
        }

        @Test
        @DisplayName("finds PROCESSING request for URL hash")
        void findsProcessingRequest() {
            // Given
            String url = BASE_URL + "-processing";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            request.markProcessing();
            parseRequestRepository.save(request);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.PROCESSING, found.get().getStatus());
        }

        @Test
        @DisplayName("returns empty for COMPLETED request")
        void returnsEmptyForCompletedRequest() {
            // Given
            String url = BASE_URL + "-completed";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            request.markCompleted();
            parseRequestRepository.save(request);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isEmpty(), "COMPLETED requests should not be returned");
        }

        @Test
        @DisplayName("returns empty for FAILED request")
        void returnsEmptyForFailedRequest() {
            // Given
            String url = BASE_URL + "-failed";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            request.markFailed("Test failure");
            parseRequestRepository.save(request);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(resource.getUrlHash());

            // Then
            assertTrue(found.isEmpty(), "FAILED requests should not be returned");
        }

        @Test
        @DisplayName("returns empty for non-existent URL hash")
        void returnsEmptyForNonExistentUrlHash() {
            // Given
            UrlHash nonExistent = UrlHash.fromUrl("https://nonexistent.com/inflight");

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(nonExistent);

            // Then
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("updates status to PROCESSING")
        void updatesToProcessing() {
            // Given
            String url = BASE_URL + "-update-processing";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            ParseRequest saved = parseRequestRepository.save(request);

            // When
            parseRequestRepository.updateStatus(saved.getId(), "PROCESSING", null);

            // Then
            Optional<ParseRequest> found = parseRequestRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.PROCESSING, found.get().getStatus());
        }

        @Test
        @DisplayName("updates status to FAILED with error message")
        void updatesToFailedWithError() {
            // Given
            String url = BASE_URL + "-update-failed";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            ParseRequest saved = parseRequestRepository.save(request);
            String errorMessage = "Parsing failed: connection timeout";

            // When
            parseRequestRepository.updateStatus(saved.getId(), "FAILED", errorMessage);

            // Then
            Optional<ParseRequest> found = parseRequestRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.FAILED, found.get().getStatus());
            assertEquals(errorMessage, found.get().getErrorMessage());
        }

        @Test
        @DisplayName("updates status to COMPLETED")
        void updatesToCompleted() {
            // Given
            String url = BASE_URL + "-update-completed";
            Resource resource = createResource(url);
            ParseRequest request = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            ParseRequest saved = parseRequestRepository.save(request);

            // When
            parseRequestRepository.updateStatus(saved.getId(), "COMPLETED", null);

            // Then
            Optional<ParseRequest> found = parseRequestRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.COMPLETED, found.get().getStatus());
            assertNull(found.get().getErrorMessage());
        }
    }

    @Nested
    @DisplayName("deduplication scenario")
    class DeduplicationScenario {

        @Test
        @DisplayName("only finds most relevant in-flight request when multiple exist")
        void findsOneInFlightRequest() {
            // Given - create multiple requests for same URL
            String url = BASE_URL + "-dedup-multi";
            Resource resource = createResource(url);

            // Create a completed request
            ParseRequest completed = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            completed.markCompleted();
            parseRequestRepository.save(completed);

            // Create a pending request
            ParseRequest pending = ParseRequest.create(resource.getUrlHash(), UUID.randomUUID());
            parseRequestRepository.save(pending);

            // When
            Optional<ParseRequest> found = parseRequestRepository.findInFlightByUrlHash(resource.getUrlHash());

            // Then - should find the pending one, not the completed one
            assertTrue(found.isPresent());
            assertEquals(ParseStatus.PENDING, found.get().getStatus());
        }
    }
}
