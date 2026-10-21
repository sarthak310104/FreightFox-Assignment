package com.assignment.githubsearcher;

import com.assignment.githubsearcher.client.GithubClient;
import com.assignment.githubsearcher.client.dto.GithubOwner;
import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.repository.GithubRepoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RepoSearchIntegTest {

    private static final String SEARCH_BODY = """
            {"query": "spring boot", "language": "Java", "sort": "stars"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GithubRepoRepository repository;

    @MockitoBean
    private GithubClient githubClient;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void searchingTwiceShouldUpdateExistingRowsInsteadOfDuplicating() throws Exception {
        when(githubClient.searchRepositories(any(), any(), any())).thenReturn(List.of(item(1L, "Java", 450), item(2L, "Java", 50))).thenReturn(List.of(item(1L, "Java", 999), item(2L, "Java", 50)));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON).content(SEARCH_BODY)).andExpect(status().isOk());
        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON).content(SEARCH_BODY)).andExpect(status().isOk());

        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(1L)).get().extracting(entity -> entity.getStars()).isEqualTo(999);
    }

    @Test
    void storedResultsShouldBeFilteredAndSorted() throws Exception {
        when(githubClient.searchRepositories(any(), any(), any()))
                .thenReturn(List.of(item(1L, "Java", 450), item(2L, "Java", 50), item(3L, "Go", 900)));

        mockMvc.perform(post("/api/github/search").contentType(MediaType.APPLICATION_JSON).content(SEARCH_BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Repositories fetched and saved successfully")).andExpect(jsonPath("$.repositories.length()").value(3));

        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.repositories.length()").value(1)).andExpect(jsonPath("$.repositories[0].id").value(1)).andExpect(jsonPath("$.repositories[0].stars").value(450));
    }

    private GithubRepoItem item(long id, String language, int stars) {
        Instant time = Instant.parse("2024-01-01T12:00:00Z");
        return new GithubRepoItem(id, "repo-" + id, "owner/repo-" + id, "desc", new GithubOwner("owner"), language, stars, 10, stars, 0, "https://github.com/owner/repo-" + id, time, time);
    }
}