package org.homechef.core.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * REST request body for submitting a URL to parse.
 */
public record SubmitUrlRequest(
        @NotBlank(message = "URL is required")
        @URL(message = "Must be a valid URL")
        String url
) {
}