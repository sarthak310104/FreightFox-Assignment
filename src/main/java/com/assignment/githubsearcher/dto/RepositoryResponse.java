package com.assignment.githubsearcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "A GitHub repository")
public record RepositoryResponse(
        @Schema(description = "GitHub repository ID", example = "6296790")
        Long id,

        @Schema(description = "Repository name", example = "spring-boot")
        String name,

        @Schema(description = "Repository description", example = "Spring Boot helps you to create Spring-powered applications")
        String description,

        @Schema(description = "Owner login", example = "spring-projects")
        String owner,

        @Schema(description = "Primary programming language", example = "Java")
        String language,

        @Schema(description = "Stars count", example = "76000")
        int stars,

        @Schema(description = "Forks count", example = "41000")
        int forks,

        @Schema(description = "Last updated time on GitHub (UTC)", example = "2024-01-01T12:00:00Z")
        Instant lastUpdated) { }