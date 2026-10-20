package com.assignment.githubsearcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchRequest(
        @NotBlank(message = "query must not be blank")
        @Size(max = 256, message = "query must be at most 256 characters")
        String query,

        @Size(max = 50, message = "language must be at most 50 characters")
        String language,

        SortOption sort) {

    public SearchRequest {
        if (sort == null) {
            sort = SortOption.STARS;
        }
    }
}