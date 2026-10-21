package com.assignment.githubsearcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "GitHub search criteria")
public record SearchRequest(

        @Schema(description = "Repository name to search for (partial or full match)", example = "spring boot", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "query must not be blank")
        @Size(max = 256, message = "query must be at most 256 characters")
        String query,

        @Schema(description = "Programming language filter (optional)", example = "Java")
        @Size(max = 50, message = "language must be at most 50 characters")
        String language,

        @Schema(description = "Sort field (optional, defaults to stars)", example = "stars")
        SortOption sort) {

    public SearchRequest {
        if (sort == null) {
            sort = SortOption.STARS;
        }
    }
}