package com.assignment.githubsearcher.service;

import com.assignment.githubsearcher.client.GithubClient;
import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.mapper.RepoMapper;
import com.assignment.githubsearcher.repository.GithubRepoRepository;
import com.assignment.githubsearcher.repository.RepositoryUpsertDao;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class RepoSearchServiceImpl implements RepoSearchService {

    private final GithubClient githubClient;
    private final GithubRepoRepository repository;
    private final RepositoryUpsertDao upsertDao;
    private final RepoMapper mapper;
    private final Clock clock;

    public RepoSearchServiceImpl(GithubClient githubClient, GithubRepoRepository repository, RepositoryUpsertDao upsertDao, RepoMapper mapper, Clock clock) {
        this.githubClient = githubClient;
        this.repository = repository;
        this.upsertDao = upsertDao;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public SearchResponse searchAndSave(SearchRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }

    @Override
    public RepositoryListResponse findStored(String language, Integer minStars, SortOption sort) { throw new UnsupportedOperationException("Not implemented yet"); }
}