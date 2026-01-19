package org.homechef.core.domain.recipe;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a known URL resource.
 * Keyed by url_hash (natural key).
 */
public class Resource {

    private final UrlHash urlHash;
    private final String url;
    private final Instant createdAt;

    private Resource(UrlHash urlHash, String url, Instant createdAt) {
        this.urlHash = Objects.requireNonNull(urlHash, "urlHash cannot be null");
        this.url = Objects.requireNonNull(url, "url cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    /**
     * Creates a new Resource from a URL (generates hash).
     */
    public static Resource create(String url) {
        Objects.requireNonNull(url, "url cannot be null");
        return new Resource(
                UrlHash.fromUrl(url),
                url.trim(),
                Instant.now()
        );
    }

    /**
     * Reconstitutes a Resource from persistence.
     */
    public static Resource reconstitute(String urlHash, String url, Instant createdAt) {
        return new Resource(
                UrlHash.fromHash(urlHash),
                url,
                createdAt
        );
    }

    public UrlHash getUrlHash() {
        return urlHash;
    }

    public String getUrl() {
        return url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(urlHash, resource.urlHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urlHash);
    }

    @Override
    public String toString() {
        return "Resource{urlHash=" + urlHash.value() + ", url='" + url + "'}";
    }
}