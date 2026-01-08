package org.homechef.core.domain.recipe;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity tracking the lifecycle of a parse request.
 * Multiple ParseRequests can reference the same Resource.
 */
public class ParseRequest {

    private final UUID id;
    private final UUID userId; // nullable for guests
    private final UrlHash urlHash;
    private ParseStatus status;
    private String errorMessage;
    private final Instant createdAt;
    private Instant updatedAt;

    private ParseRequest(UUID id, UUID userId, UrlHash urlHash, ParseStatus status,
                         String errorMessage, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.userId = userId; // nullable
        this.urlHash = Objects.requireNonNull(urlHash, "urlHash cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.errorMessage = errorMessage;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    /**
     * Creates a new ParseRequest in PENDING state.
     */
    public static ParseRequest create(UrlHash urlHash, UUID userId) {
        Instant now = Instant.now();
        return new ParseRequest(
                UUID.randomUUID(),
                userId,
                urlHash,
                ParseStatus.PENDING,
                null,
                now,
                now
        );
    }

    /**
     * Creates a new ParseRequest for a guest user.
     */
    public static ParseRequest createForGuest(UrlHash urlHash) {
        return create(urlHash, null);
    }

    /**
     * Reconstitutes a ParseRequest from persistence.
     */
    public static ParseRequest reconstitute(UUID id, UUID userId, String urlHash, ParseStatus status,
                                            String errorMessage, Instant createdAt, Instant updatedAt) {
        return new ParseRequest(
                id,
                userId,
                UrlHash.fromHash(urlHash),
                status,
                errorMessage,
                createdAt,
                updatedAt
        );
    }

    /**
     * Transitions to PROCESSING state.
     */
    public void markProcessing() {
        validateTransition(ParseStatus.PROCESSING);
        this.status = ParseStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to COMPLETED state.
     */
    public void markCompleted() {
        validateTransition(ParseStatus.COMPLETED);
        this.status = ParseStatus.COMPLETED;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions to FAILED state with error message.
     */
    public void markFailed(String errorMessage) {
        validateTransition(ParseStatus.FAILED);
        this.status = ParseStatus.FAILED;
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage required for FAILED status");
        this.updatedAt = Instant.now();
    }

    private void validateTransition(ParseStatus targetStatus) {
        if (this.status.isTerminal()) {
            throw new IllegalStateException(
                    "Cannot transition from terminal state " + this.status + " to " + targetStatus);
        }
        // Valid transitions: PENDING -> PROCESSING, PENDING -> COMPLETED/FAILED, PROCESSING -> COMPLETED/FAILED
        if (this.status == ParseStatus.PROCESSING && targetStatus == ParseStatus.PENDING) {
            throw new IllegalStateException("Cannot transition from PROCESSING back to PENDING");
        }
    }

    /**
     * Returns true if this request is still in-flight (PENDING or PROCESSING).
     */
    public boolean isInFlight() {
        return status.isInFlight();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UrlHash getUrlHash() {
        return urlHash;
    }

    public ParseStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParseRequest that = (ParseRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ParseRequest{id=" + id + ", urlHash=" + urlHash.value() + ", status=" + status + "}";
    }
}