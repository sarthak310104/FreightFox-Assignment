package com.assignment.githubsearcher.client;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.config.GithubProperties;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.exception.GithubApiException;
import com.assignment.githubsearcher.exception.GithubRateLimitException;
import com.assignment.githubsearcher.exception.InvalidSearchQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GithubRestClientTest {

    private static final String BASE_URL = "https://api.github.com";
    private static final String SEARCH_PATH = "/search/repositories";

    private MockRestServiceServer server;
    private GithubRestClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        GithubProperties properties = new GithubProperties(
                BASE_URL, "", 30, Duration.ofSeconds(5), Duration.ofSeconds(10));
        client = new GithubRestClient(builder.build(), properties);
    }

    @Test
    void shouldReturnRepositoriesOnSuccess() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("sort", "stars"))
                .andExpect(queryParam("order", "desc"))
                .andExpect(queryParam("per_page", "30"))
                .andRespond(withSuccess("""
                        {
                          "total_count": 1,
                          "items": [{
                            "id": 123456,
                            "name": "spring-boot-example",
                            "full_name": "user123/spring-boot-example",
                            "description": "An example repository for Spring Boot",
                            "owner": {"login": "user123"},
                            "language": "Java",
                            "stargazers_count": 450,
                            "forks_count": 120,
                            "watchers_count": 450,
                            "open_issues_count": 3,
                            "html_url": "https://github.com/user123/spring-boot-example",
                            "created_at": "2020-05-01T10:00:00Z",
                            "updated_at": "2024-01-01T12:00:00Z"
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<GithubRepoItem> result = client.searchRepositories("spring boot", "Java", SortOption.STARS);

        assertThat(result).hasSize(1);
        GithubRepoItem item = result.get(0);
        assertThat(item.id()).isEqualTo(123456L);
        assertThat(item.name()).isEqualTo("spring-boot-example");
        assertThat(item.owner().login()).isEqualTo("user123");
        assertThat(item.stargazersCount()).isEqualTo(450);
        assertThat(item.forksCount()).isEqualTo(120);
        assertThat(item.updatedAt()).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
        server.verify();
    }

    @Test
    void shouldReturnEmptyListWhenGithubFindsNothing() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withSuccess("""
                        {"total_count": 0, "items": []}
                        """, MediaType.APPLICATION_JSON));

        List<GithubRepoItem> result = client.searchRepositories("zzzz-nothing", null, SortOption.STARS);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowRateLimitExceptionOn403WithNoRemainingRequests() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .header("X-RateLimit-Remaining", "0")
                        .header("X-RateLimit-Reset", "1700000000"));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubRateLimitException.class)
                .extracting(e -> ((GithubRateLimitException) e).getResetAt())
                .isEqualTo(Instant.ofEpochSecond(1700000000L));
    }

    @Test
    void shouldThrowRateLimitExceptionOn429() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "60"));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubRateLimitException.class);
    }

    @Test
    void shouldThrowApiExceptionOn403WithoutRateLimitHeaders() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubApiException.class);
    }

    @Test
    void shouldThrowInvalidQueryExceptionOn422() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(InvalidSearchQueryException.class);
    }

    @Test
    void shouldThrowApiExceptionOnServerError() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withServerError());

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubApiException.class).hasMessageContaining("500");
    }

    @Test
    void shouldThrowApiExceptionOnMalformedJson() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withSuccess("this is not json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubApiException.class);
    }

    @Test
    void shouldThrowApiExceptionWhenItemsAreMissing() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withSuccess("""
                        {"total_count": 0}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubApiException.class);
    }

    @Test
    void shouldThrowApiExceptionOnTimeout() {
        server.expect(requestTo(containsString(SEARCH_PATH))).andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.searchRepositories("spring", null, SortOption.STARS)).isInstanceOf(GithubApiException.class).hasMessageContaining("unreachable");
    }
}