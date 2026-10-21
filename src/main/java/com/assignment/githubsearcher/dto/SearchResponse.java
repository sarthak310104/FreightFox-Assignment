package com.assignment.githubsearcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of a GitHub search")
public record SearchResponse(
        @Schema(description = "Outcome message", example = "Repositories fetched and saved successfully")
        String message,

        @Schema(description = "Repositories returned by GitHub")
        List<RepositoryResponse> repositories) { }