package com.assignment.githubsearcher.client;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.client.dto.GithubSearchResponse;
import com.assignment.githubsearcher.config.GithubProperties;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.exception.GithubApiException;
import com.assignment.githubsearcher.exception.GithubException;
import com.assignment.githubsearcher.exception.GithubRateLimitException;
import com.assignment.githubsearcher.exception.InvalidSearchQueryException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class GithubRestClient implements GithubClient {
    private static final String SEARCH_PATH = "/search/repositories";
    private final RestClient restClient;
    private final int perPage;

    public GithubRestClient(RestClient githubHttpClient, GithubProperties properties) {
        this.restClient = githubHttpClient;
        this.perPage = properties.perPage();
    }

    @Override
    public List<GithubRepoItem> searchRepositories(String query, String language, SortOption sort) {
        String searchQuery = GithubQueryBuilder.build(query, language);
        try {
            GithubSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_PATH)
                            .queryParam("q", searchQuery)
                            .queryParam("sort", sort.getValue())
                            .queryParam("order", "desc")
                            .queryParam("per_page", perPage)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, errorResponse) -> {
                        throw toGithubException(errorResponse);
                    }).body(GithubSearchResponse.class);

            if (response == null || response.items() == null) {
                throw new GithubApiException("GitHub returned an empty or invalid response");
            }
            return response.items();

        } catch (ResourceAccessException e) {
            throw new GithubApiException("GitHub API is unreachable or timed out", e);
        } catch (RestClientException e) {
            throw new GithubApiException("Invalid response from GitHub API", e);
        }
    }

    private GithubException toGithubException(ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        HttpHeaders headers = response.getHeaders();

        boolean rateLimitHit = "0".equals(headers.getFirst("X-RateLimit-Remaining")) || headers.getFirst(HttpHeaders.RETRY_AFTER) != null;

        if (status == 429 || (status == 403 && rateLimitHit)) {
            return new GithubRateLimitException(parseResetTime(headers.getFirst("X-RateLimit-Reset")));
        }
        if (status == 422) {
            return new InvalidSearchQueryException("GitHub rejected the search query");
        }
        return new GithubApiException("GitHub API returned status " + status);
    }

    private Instant parseResetTime(String resetHeader) {
        if (resetHeader == null) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(resetHeader));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}