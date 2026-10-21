package com.assignment.githubsearcher.mapper;

import com.assignment.githubsearcher.client.dto.GithubRepoItem;
import com.assignment.githubsearcher.dto.RepositoryResponse;
import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RepoMapper {

    public RepositoryEntity toEntity(GithubRepoItem item, Instant fetchedAt) { return null; }

    public RepositoryResponse toResponse(RepositoryEntity entity) { return null; }
}