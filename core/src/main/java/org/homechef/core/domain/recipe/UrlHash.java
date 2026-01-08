package org.homechef.core.domain.recipe;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Value Object representing a normalized, hashed URL.
 * Immutable. Equality by hash value.
 */
public record UrlHash(String value) {

    public UrlHash {
        Objects.requireNonNull(value, "UrlHash value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("UrlHash value cannot be blank");
        }
        if (value.length() != 64) {
            throw new IllegalArgumentException("UrlHash must be 64 characters (SHA-256 hex)");
        }
    }

    /**
     * Creates a UrlHash from a raw URL string.
     * Normalizes the URL before hashing to ensure consistent deduplication.
     */
    public static UrlHash fromUrl(String url) {
        Objects.requireNonNull(url, "URL cannot be null");
        if (url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be blank");
        }

        String normalized = normalizeUrl(url);
        String hash = sha256(normalized);
        return new UrlHash(hash);
    }

    /**
     * Reconstructs a UrlHash from an existing hash string (e.g., from DB).
     */
    public static UrlHash fromHash(String hash) {
        return new UrlHash(hash);
    }

    private static String normalizeUrl(String url) {
        try {
            URI uri = new URI(url.trim());

            // Normalize: lowercase scheme and host, remove trailing slash, remove fragment
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "https";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath() : "";
            String query = uri.getQuery();

            // Remove default ports
            if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
                port = -1;
            }

            // Remove trailing slash from path (unless it's just "/")
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            // Reconstruct normalized URL (without fragment)
            StringBuilder normalized = new StringBuilder();
            normalized.append(scheme).append("://").append(host);
            if (port != -1) {
                normalized.append(":").append(port);
            }
            normalized.append(path);
            if (query != null && !query.isBlank()) {
                normalized.append("?").append(query);
            }

            return normalized.toString();
        } catch (URISyntaxException e) {
            // Fallback: use trimmed lowercase URL as-is
            return url.trim().toLowerCase();
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}