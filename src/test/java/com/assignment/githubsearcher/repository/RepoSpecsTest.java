package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.TestcontainersConfiguration;
import com.assignment.githubsearcher.dto.SortOption;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, RepositoryUpsertDao.class})
class RepoSpecsTest {

    @Autowired
    private RepositoryUpsertDao upsertDao;

    @Autowired
    private GithubRepoRepository repository;

    @BeforeEach
    void insertTestData() {
        upsertDao.upsertAll(List.of(
                repo(1L, "Java", 500, 50, "2024-03-01T00:00:00Z"),
                repo(2L, "Java", 100, 300, "2024-01-01T00:00:00Z"),
                repo(3L, "Go", 1000, 10, "2024-02-01T00:00:00Z"),
                repo(4L, null, 5, 0, "2023-01-01T00:00:00Z")));
    }

    @Test
    void shouldReturnAllRepositoriesSortedByStarsWhenNoFilters() {
        assertThat(ids(find(null, null, SortOption.STARS))).containsExactly(3L, 1L, 2L, 4L);
    }

    @Test
    void shouldFilterByLanguageIgnoringCase() {
        assertThat(ids(find("jAvA", null, SortOption.STARS))).containsExactly(1L, 2L);
    }

    @Test
    void shouldFilterByMinimumStars() {
        assertThat(ids(find(null, 100, SortOption.STARS))).containsExactly(3L, 1L, 2L);
    }

    @Test
    void shouldCombineLanguageAndMinimumStars() {
        assertThat(ids(find("Java", 200, SortOption.STARS))).containsExactly(1L);
    }

    @Test
    void shouldSortByForks() {
        assertThat(ids(find(null, null, SortOption.FORKS))).containsExactly(2L, 1L, 3L, 4L);
    }

    @Test
    void shouldSortByLastUpdated() {
        assertThat(ids(find(null, null, SortOption.UPDATED))).containsExactly(1L, 3L, 2L, 4L);
    }

    @Test
    void shouldReturnEmptyListWhenNothingMatches() {
        assertThat(find("Rust", null, SortOption.STARS)).isEmpty();
    }

    private List<RepositoryEntity> find(String language, Integer minStars, SortOption sort) {
        Sort dbSort = Sort.by(Sort.Direction.DESC, sort.getEntityField());
        return repository.findAll(RepoSpecs.matching(language, minStars), dbSort);
    }

    private List<Long> ids(List<RepositoryEntity> repositories) {
        return repositories.stream().map(RepositoryEntity::getId).toList();
    }

    private RepositoryEntity repo(long id, String language, int stars, int forks, String updated) {
        Instant updatedAt = Instant.parse(updated);
        return new RepositoryEntity(id, "repo-" + id, "owner/repo-" + id, "desc", "owner",
                language, stars, forks, stars, 0, "https://github.com/owner/repo-" + id,
                updatedAt, updatedAt, updatedAt);
    }
}