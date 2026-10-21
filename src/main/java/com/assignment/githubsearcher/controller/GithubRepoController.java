package com.assignment.githubsearcher.controller;

import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.service.RepoSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GithubRepoController {

    private final RepoSearchService service;

    public GithubRepoController(RepoSearchService service) { this.service = service; }

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return service.searchAndSave(request);
    }

    @GetMapping("/repositories")
    public RepositoryListResponse getRepositories(
            @RequestParam(required = false)
            @Size(max = 50, message = "language must be at most 50 characters") String language,
            @RequestParam(required = false)
            @Min(value = 0, message = "minStars must be 0 or greater") Integer minStars,
            @RequestParam(defaultValue = "stars") SortOption sort) {
        return service.findStored(language, minStars, sort);
    }
}