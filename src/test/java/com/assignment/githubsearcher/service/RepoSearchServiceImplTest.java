package com.assignment.githubsearcher.service;

import com.assignment.githubsearcher.client.GithubClient;
import com.assignment.githubsearcher.client.dto.GithubOwner;
import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.RepositoryListResponse;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.dto.SearchRequest;
import com.assignment.githubsearcher.dto.SearchResponse;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import com.assignment.githubsearcher.exception.GithubApiException;
import com.assignment.githubsearcher.mapper.RepoMapper;
import com.assignment.githubsearcher.repository.GithubRepoRepository;
import com.assignment.githubsearcher.repository.RepositoryUpsertDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepoSearchServiceImplTest {

    private static final Instant NOW = Instant.parse("2024-06-01T00:00:00Z");
    private static final Instant UPDATED = Instant.parse("2024-01-01T12:00:00Z");

    @Mock
    private GithubClient githubClient;

    @Mock
    private GithubRepoRepository repository;

    @Mock
    private RepositoryUpsertDao upsertDao;

    private RepoSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new RepoSearchServiceImpl(githubClient, repository, upsertDao, new RepoMapper(), fixedClock);
    }

    @Test
    void shouldFetchSaveAndReturnRepositories() {
        when(githubClient.searchRepositories("spring boot", "Java", SortOption.STARS)).thenReturn(List.of(sampleItem()));

        SearchResponse response = service.searchAndSave(new SearchRequest("spring boot", "Java", SortOption.STARS));

        assertThat(response.message()).isEqualTo("Repositories fetched and saved successfully");
        assertThat(response.repositories()).containsExactly(new RepositoryResponse(123456L, "spring-boot-example", "An example repository", "user123", "Java", 450, 120, UPDATED));

        ArgumentCaptor<List<RepositoryEntity>> saved = ArgumentCaptor.captor();
        verify(upsertDao).upsertAll(saved.capture());
        assertThat(saved.getValue()).hasSize(1);
        assertThat(saved.getValue().get(0).getId()).isEqualTo(123456L);
        assertThat(saved.getValue().get(0).getFetchedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldReturnEmptyResultAndSkipSavingWhenGithubFindsNothing() {
        when(githubClient.searchRepositories(any(), any(), any())).thenReturn(List.of());

        SearchResponse response = service.searchAndSave(new SearchRequest("zzzz", null, null));

        assertThat(response.message()).isEqualTo("No repositories found for the given criteria");
        assertThat(response.repositories()).isEmpty();
        verifyNoInteractions(upsertDao);
    }

    @Test
    void shouldNotSaveAnythingWhenGithubFails() {
        when(githubClient.searchRepositories(any(), any(), any())).thenThrow(new GithubApiException("GitHub is down"));

        assertThatThrownBy(() -> service.searchAndSave(new SearchRequest("spring", null, null))).isInstanceOf(GithubApiException.class);
        verifyNoInteractions(upsertDao);
    }

    @Test
    void shouldReturnStoredRepositoriesMappedToResponses() {
        when(repository.findAll(ArgumentMatchers.<Specification<RepositoryEntity>>any(), any(Sort.class))).thenReturn(List.of(sampleEntity()));

        RepositoryListResponse response = service.findStored("Java", 100, SortOption.STARS);

        assertThat(response.repositories()).containsExactly(new RepositoryResponse(123456L, "spring-boot-example", "An example repository", "user123", "Java", 450, 120, UPDATED));
    }

    @Test
    void shouldSortStoredRepositoriesByRequestedFieldDescending() {
        when(repository.findAll(ArgumentMatchers.<Specification<RepositoryEntity>>any(), any(Sort.class))).thenReturn(List.of());

        service.findStored(null, null, SortOption.UPDATED);

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<RepositoryEntity>>any(), sort.capture());
        Sort.Order order = sort.getValue().getOrderFor("lastUpdated");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldDefaultToSortingByStarsWhenSortIsNull() {
        when(repository.findAll(ArgumentMatchers.<Specification<RepositoryEntity>>any(), any(Sort.class))).thenReturn(List.of());

        service.findStored(null, null, null);

        ArgumentCaptor<Sort> sort = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<RepositoryEntity>>any(), sort.capture());
        assertThat(sort.getValue().getOrderFor("stars")).isNotNull();
    }

    private GithubRepoItem sampleItem() {
        return new GithubRepoItem(123456L, "spring-boot-example", "user123/spring-boot-example", "An example repository", new GithubOwner("user123"), "Java", 450, 120, 450, 3, "https://github.com/user123/spring-boot-example", UPDATED, UPDATED);
    }

    private RepositoryEntity sampleEntity() {
        return new RepositoryEntity(123456L, "spring-boot-example", "user123/spring-boot-example", "An example repository", "user123", "Java", 450, 120, 450, 3, "https://github.com/user123/spring-boot-example", UPDATED, UPDATED, NOW);
    }
}