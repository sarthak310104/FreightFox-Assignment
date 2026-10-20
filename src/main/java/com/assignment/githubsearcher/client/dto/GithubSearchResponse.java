package com.assignment.githubsearcher.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubSearchResponse(
        @JsonProperty("total_count") int totalCount,
        List<GithubRepoItem> items) {
}