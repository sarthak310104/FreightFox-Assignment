package com.assignment.githubsearcher.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoItem(
        long id,
        String name,
        @JsonProperty("full_name") String fullName,
        String description,
        GithubOwner owner,
        String language,
        @JsonProperty("stargazers_count") int stargazersCount,
        @JsonProperty("forks_count") int forksCount,
        @JsonProperty("watchers_count") int watchersCount,
        @JsonProperty("open_issues_count") int openIssuesCount,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {
}