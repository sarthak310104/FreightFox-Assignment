package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.TestcontainersConfiguration;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestcontainersConfiguration.class, RepositoryUpsertDao.class})
class RepositoryUpsertDaoTest {

    @Autowired
    private RepositoryUpsertDao upsertDao;

    @Autowired
    private GithubRepoRepository repository;

    @Test
    void shouldInsertNewRepositories() {
        upsertDao.upsertAll(List.of(repo(1L, "Java", 100), repo(2L, "Go", 50)));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void shouldUpdateExistingRepositoryInsteadOfDuplicating() {
        upsertDao.upsertAll(List.of(repo(1L, "Java", 100)));
        upsertDao.upsertAll(List.of(repo(1L, "Java", 500)));

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(1L))
                .get()
                .extracting(RepositoryEntity::getStars)
                .isEqualTo(500);
    }

    @Test
    void shouldDoNothingForEmptyList() {
        upsertDao.upsertAll(List.of());

        assertThat(repository.count()).isZero();
    }

    private RepositoryEntity repo(long id, String language, int stars) {
        Instant time = Instant.parse("2024-01-01T12:00:00Z");
        return new RepositoryEntity(id, "repo-" + id, "owner/repo-" + id, "desc", "owner",
                language, stars, 10, stars, 1, "https://github.com/owner/repo-" + id,
                time, time, time);
    }
}