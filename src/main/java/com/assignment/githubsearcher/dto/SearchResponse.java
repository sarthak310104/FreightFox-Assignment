package com.assignment.githubsearcher.dto;

import java.util.List;

public record SearchResponse(String message, List<RepositoryResponse> repositories) {
}