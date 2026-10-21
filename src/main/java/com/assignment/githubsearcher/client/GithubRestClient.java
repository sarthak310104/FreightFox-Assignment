package com.assignment.githubsearcher.client;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.config.GithubProperties;
import com.assignment.githubsearcher.dto.SortOption;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GithubRestClient implements GithubClient {
    private final RestClient restClient;
    private final int perPage;

    public GithubRestClient(RestClient githubRestClient, GithubProperties properties) {
        this.restClient = githubRestClient;
        this.perPage = properties.perPage();
    }

    @Override
    public List<GithubRepoItem> searchRepositories(String query, String language, SortOption sort) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}