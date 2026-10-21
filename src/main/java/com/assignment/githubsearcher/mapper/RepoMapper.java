package com.assignment.githubsearcher.mapper;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RepoMapper {

    private static final String UNKNOWN_OWNER = "unknown";

    public RepositoryEntity toEntity(GithubRepoItem item, Instant fetchedAt) {
        String owner = item.owner() != null ? item.owner().login() : UNKNOWN_OWNER;

        return new RepositoryEntity(
                item.id(),
                item.name(),
                item.fullName(),
                item.description(),
                owner,
                item.language(),
                item.stargazersCount(),
                item.forksCount(),
                item.watchersCount(),
                item.openIssuesCount(),
                item.htmlUrl(),
                item.createdAt(),
                item.updatedAt(),
                fetchedAt);
    }

    public RepositoryResponse toResponse(RepositoryEntity entity) {
        return new RepositoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getOwner(),
                entity.getLanguage(),
                entity.getStars(),
                entity.getForks(),
                entity.getLastUpdated());
    }
}