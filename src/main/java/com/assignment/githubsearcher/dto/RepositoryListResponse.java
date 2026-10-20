package com.assignment.githubsearcher.dto;

import java.util.List;

public record RepositoryListResponse(List<RepositoryResponse> repositories) { }