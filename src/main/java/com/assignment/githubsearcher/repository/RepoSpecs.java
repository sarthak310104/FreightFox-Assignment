package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.data.jpa.domain.Specification;

public final class RepoSpecs {

    private RepoSpecs() {}

    public static Specification<RepositoryEntity> matching(String language, Integer minStars) { return (root, query, cb) -> cb.conjunction(); }
}