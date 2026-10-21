package com.assignment.githubsearcher.mapper;

import com.assignment.githubsearcher.client.dto.GithubOwner;
import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RepoMapperTest {

    private static final Instant CREATED = Instant.parse("2020-05-01T10:00:00Z");
    private static final Instant UPDATED = Instant.parse("2024-01-01T12:00:00Z");
    private static final Instant FETCHED = Instant.parse("2024-06-01T00:00:00Z");

    private final RepoMapper mapper = new RepoMapper();

    @Test
    void shouldMapGithubItemToEntity() {
        GithubRepoItem item = new GithubRepoItem(123456L, "spring-boot-example", "user123/spring-boot-example", "An example repository", new GithubOwner("user123"), "Java", 450, 120, 460, 3, "https://github.com/user123/spring-boot-example", CREATED, UPDATED);

        RepositoryEntity entity = mapper.toEntity(item, FETCHED);

        assertThat(entity.getId()).isEqualTo(123456L);
        assertThat(entity.getName()).isEqualTo("spring-boot-example");
        assertThat(entity.getFullName()).isEqualTo("user123/spring-boot-example");
        assertThat(entity.getDescription()).isEqualTo("An example repository");
        assertThat(entity.getOwner()).isEqualTo("user123");
        assertThat(entity.getLanguage()).isEqualTo("Java");
        assertThat(entity.getStars()).isEqualTo(450);
        assertThat(entity.getForks()).isEqualTo(120);
        assertThat(entity.getWatchers()).isEqualTo(460);
        assertThat(entity.getOpenIssues()).isEqualTo(3);
        assertThat(entity.getHtmlUrl()).isEqualTo("https://github.com/user123/spring-boot-example");
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED);
        assertThat(entity.getLastUpdated()).isEqualTo(UPDATED);
        assertThat(entity.getFetchedAt()).isEqualTo(FETCHED);
    }

    @Test
    void shouldUseUnknownOwnerWhenOwnerIsMissing() {
        GithubRepoItem item = new GithubRepoItem(1L, "repo", "someone/repo", null, null, null, 0, 0, 0, 0, null, null, null);

        RepositoryEntity entity = mapper.toEntity(item, FETCHED);
        assertThat(entity.getOwner()).isEqualTo("unknown");
    }

    @Test
    void shouldMapEntityToResponse() {
        RepositoryEntity entity = new RepositoryEntity(123456L, "spring-boot-example", "user123/spring-boot-example", "An example repository", "user123", "Java", 450, 120, 460, 3, "https://github.com/user123/spring-boot-example", CREATED, UPDATED, FETCHED);

        RepositoryResponse response = mapper.toResponse(entity);

        assertThat(response).isEqualTo(new RepositoryResponse(123456L, "spring-boot-example", "An example repository", "user123", "Java", 450, 120, UPDATED));
    }
}