package com.assignment.githubsearcher.controller;

import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.exception.GithubApiException;
import com.assignment.githubsearcher.exception.GithubRateLimitException;
import com.assignment.githubsearcher.exception.InvalidSearchQueryException;
import com.assignment.githubsearcher.service.RepoSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubRepoController.class)
class GithubRepoControllerTest {

    private static final Instant UPDATED = Instant.parse("2024-01-01T12:00:00Z");
    private static final RepositoryResponse SAMPLE = new RepositoryResponse(123456L, "spring-boot-example", "An example repository for Spring Boot", "user123", "Java", 450, 120, UPDATED);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepoSearchService service;

    // ---------- POST /api/github/search ----------
    @Test
    void shouldReturnSearchResults() throws Exception {
        when(service.searchAndSave(any())).thenReturn(new SearchResponse("Repositories fetched and saved successfully", List.of(SAMPLE)));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring boot", "language": "Java", "sort": "stars"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories[0].id").value(123456))
                .andExpect(jsonPath("$.repositories[0].name").value("spring-boot-example"))
                .andExpect(jsonPath("$.repositories[0].owner").value("user123"))
                .andExpect(jsonPath("$.repositories[0].stars").value(450))
                .andExpect(jsonPath("$.repositories[0].forks").value(120))
                .andExpect(jsonPath("$.repositories[0].lastUpdated").value("2024-01-01T12:00:00Z"));

        verify(service).searchAndSave(new SearchRequest("spring boot", "Java", SortOption.STARS));
    }

    @Test
    void shouldDefaultSortToStarsWhenMissingFromBody() throws Exception {
        when(service.searchAndSave(any())).thenReturn(new SearchResponse("ok", List.of()));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring"}
                                """))
                .andExpect(status().isOk());

        verify(service).searchAndSave(new SearchRequest("spring", null, SortOption.STARS));
    }

    @Test
    void shouldReturn400WhenQueryIsBlank() throws Exception {
        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "   "}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.query").value("query must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400WhenQueryIsMissing() throws Exception {
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.query").exists());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400ForInvalidSortInBody() throws Exception {
        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring", "sort": "likes"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Malformed request body or invalid field value"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn429WithRetryAfterWhenGithubRateLimited() throws Exception {
        when(service.searchAndSave(any())).thenThrow(new GithubRateLimitException(Instant.now().plusSeconds(60)));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring"}
                                """))
                .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After")).andExpect(jsonPath("$.detail").value(containsString("rate limit")));
    }

    @Test
    void shouldReturn422WhenGithubRejectsQuery() throws Exception {
        when(service.searchAndSave(any())).thenThrow(new InvalidSearchQueryException("GitHub rejected the search query"));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring"}
                                """))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.detail").value("GitHub rejected the search query"));
    }

    @Test
    void shouldReturn502WhenGithubFails() throws Exception {
        when(service.searchAndSave(any())).thenThrow(new GithubApiException("GitHub API returned status 500"));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring"}
                                """))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.detail").value("GitHub API returned status 500"));
    }

    @Test
    void shouldReturn500WithoutLeakingInternalDetails() throws Exception {
        when(service.searchAndSave(any())).thenThrow(new IllegalStateException("database password is secret123"));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query": "spring"}
                                """))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.detail").value("An unexpected error occurred")).andExpect(jsonPath("$.detail").value(not(containsString("secret123"))));
    }

    // ---------- GET /api/github/repositories ----------

    @Test
    void shouldReturnStoredRepositoriesWithFilters() throws Exception {
        when(service.findStored("Java", 100, SortOption.STARS)).thenReturn(new RepositoryListResponse(List.of(SAMPLE)));

        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.repositories[0].id").value(123456)).andExpect(jsonPath("$.repositories[0].language").value("Java"));
    }

    @Test
    void shouldUseDefaultsWhenNoParametersGiven() throws Exception {
        when(service.findStored(null, null, SortOption.STARS)).thenReturn(new RepositoryListResponse(List.of()));

        mockMvc.perform(get("/api/github/repositories")).andExpect(status().isOk()).andExpect(jsonPath("$.repositories").isEmpty());
        verify(service).findStored(null, null, SortOption.STARS);
    }

    @Test
    void shouldAcceptSortParameterInAnyCase() throws Exception {
        when(service.findStored(null, null, SortOption.FORKS)).thenReturn(new RepositoryListResponse(List.of()));

        mockMvc.perform(get("/api/github/repositories").param("sort", "FORKS")).andExpect(status().isOk());
        verify(service).findStored(null, null, SortOption.FORKS);
    }

    @Test
    void shouldReturn400ForInvalidSortParameter() throws Exception {
        mockMvc.perform(get("/api/github/repositories").param("sort", "likes")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value(containsString("Allowed values: stars, forks, updated")));
        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400ForNegativeMinStars() throws Exception {
        mockMvc.perform(get("/api/github/repositories").param("minStars", "-1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("minStars must be 0 or greater"));
        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn400ForNonNumericMinStars() throws Exception {
        mockMvc.perform(get("/api/github/repositories").param("minStars", "abc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Invalid value 'abc' for parameter 'minStars'"));
        verifyNoInteractions(service);
    }

    @Test
    void shouldReturn404ForUnknownPath() throws Exception {
        mockMvc.perform(get("/api/github/does-not-exist")).andExpect(status().isNotFound());
    }
}