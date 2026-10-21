package com.assignment.githubsearcher.controller;

import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.service.RepoSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GitHub Repositories", description = "Search GitHub and query stored repositories")
@RestController
@RequestMapping("/api/github")
public class GithubRepoController {

    private final RepoSearchService service;

    public GithubRepoController(RepoSearchService service) {
        this.service = service;
    }

    @Operation( summary = "Search GitHub and save the results", description = "Searches GitHub repositories whose name matches the query, optionally filtered by language and sorted by stars, forks or last updated date. Results are saved to the database: existing repositories are updated, new ones are inserted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Repositories fetched and saved, or no results found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body (blank query, invalid sort, malformed JSON)"),
            @ApiResponse(responseCode = "422", description = "GitHub rejected the search query"),
            @ApiResponse(responseCode = "429", description = "GitHub API rate limit exceeded"),
            @ApiResponse(responseCode = "502", description = "GitHub API unavailable or returned an invalid response")
    })
    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody SearchRequest request) {
        return service.searchAndSave(request);
    }

    @Operation(
            summary = "Get stored repositories",
            description = "Returns repositories saved by earlier searches. All filters are optional. "
                    + "Results are sorted in descending order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stored repositories returned (may be empty)"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value or sort option")
    })
    @GetMapping("/repositories")
    public RepositoryListResponse getRepositories(
            @Parameter(description = "Programming language, case-insensitive", example = "Java")
            @RequestParam(required = false)
            @Size(max = 50, message = "language must be at most 50 characters") String language,

            @Parameter(description = "Minimum stars count", example = "100")
            @RequestParam(required = false)
            @Min(value = 0, message = "minStars must be 0 or greater") Integer minStars,

            @Parameter(description = "Sort field: stars, forks or updated", example = "stars")
            @RequestParam(defaultValue = "stars") SortOption sort) {
        return service.findStored(language, minStars, sort);
    }
}