package com.assignment.githubsearcher.client;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.SortOption;

import java.util.List;

public interface GithubClient {
    List<GithubRepoItem> searchRepositories(String query, String language, SortOption sort);
}