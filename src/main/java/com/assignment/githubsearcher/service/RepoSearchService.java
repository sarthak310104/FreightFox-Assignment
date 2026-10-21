package com.assignment.githubsearcher.service;

import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;

public interface RepoSearchService {
    SearchResponse searchAndSave(SearchRequest request);

    RepositoryListResponse findStored(String language, Integer minStars, SortOption sort);
}