package com.assignment.githubsearcher.service;

import com.assignment.githubsearcher.client.GithubClient;
import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import com.assignment.githubsearcher.mapper.RepoMapper;
import com.assignment.githubsearcher.repository.GithubRepoRepository;
import com.assignment.githubsearcher.repository.RepoSpecs;
import com.assignment.githubsearcher.repository.RepositoryUpsertDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class RepoSearchServiceImpl implements RepoSearchService {

    static final String SAVED_MESSAGE = "Repositories fetched and saved successfully";
    static final String NO_RESULTS_MESSAGE = "No repositories found for the given criteria";

    private final GithubClient githubClient;
    private final GithubRepoRepository repository;
    private final RepositoryUpsertDao upsertDao;
    private final RepoMapper mapper;
    private final Clock clock;

    public RepoSearchServiceImpl(GithubClient githubClient,
                                 GithubRepoRepository repository,
                                 RepositoryUpsertDao upsertDao,
                                 RepoMapper mapper,
                                 Clock clock) {
        this.githubClient = githubClient;
        this.repository = repository;
        this.upsertDao = upsertDao;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public SearchResponse searchAndSave(SearchRequest request) {
        List<GithubRepoItem> items = githubClient.searchRepositories(request.query(), request.language(), request.sort());

        if (items.isEmpty()) { return new SearchResponse(NO_RESULTS_MESSAGE, List.of()); }

        Instant fetchedAt = clock.instant();
        List<RepositoryEntity> entities = items.stream().map(item -> mapper.toEntity(item, fetchedAt)).toList();
        upsertDao.upsertAll(entities);

        return new SearchResponse(SAVED_MESSAGE, toResponses(entities));
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryListResponse findStored(String language, Integer minStars, SortOption sort) {
        SortOption effectiveSort = sort != null ? sort : SortOption.STARS;
        Sort dbSort = Sort.by(Sort.Direction.DESC, effectiveSort.getEntityField());

        List<RepositoryEntity> results = repository.findAll(RepoSpecs.matching(language, minStars), dbSort);

        return new RepositoryListResponse(toResponses(results));
    }

    private List<RepositoryResponse> toResponses(List<RepositoryEntity> entities) {
        return entities.stream().map(mapper::toResponse).toList();
    }
}