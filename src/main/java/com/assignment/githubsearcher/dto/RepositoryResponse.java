package com.assignment.githubsearcher.dto;

import java.time.Instant;

public record RepositoryResponse(
        Long id,
        String name,
        String description,
        String owner,
        String language,
        int stars,
        int forks,
        Instant lastUpdated) {
}