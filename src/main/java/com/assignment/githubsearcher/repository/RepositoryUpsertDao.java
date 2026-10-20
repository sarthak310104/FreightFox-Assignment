package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositoryUpsertDao {

    private final JdbcTemplate jdbcTemplate;

    public RepositoryUpsertDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertAll(List<RepositoryEntity> repositories) {
        // not implemented yet
    }
}