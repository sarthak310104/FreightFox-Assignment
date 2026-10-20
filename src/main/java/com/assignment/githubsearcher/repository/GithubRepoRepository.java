package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GithubRepoRepository extends JpaRepository<RepositoryEntity, Long>, JpaSpecificationExecutor<RepositoryEntity> { }