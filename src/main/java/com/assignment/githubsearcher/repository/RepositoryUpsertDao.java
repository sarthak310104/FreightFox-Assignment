package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.entity.RepositoryEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class RepositoryUpsertDao {

    private static final String UPSERT_SQL = """
            INSERT INTO repositories (id, name, full_name, description, owner, language,
                stars, forks, watchers, open_issues, html_url, created_at, last_updated, fetched_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                full_name = EXCLUDED.full_name,
                description = EXCLUDED.description,
                owner = EXCLUDED.owner,
                language = EXCLUDED.language,
                stars = EXCLUDED.stars,
                forks = EXCLUDED.forks,
                watchers = EXCLUDED.watchers,
                open_issues = EXCLUDED.open_issues,
                html_url = EXCLUDED.html_url,
                created_at = EXCLUDED.created_at,
                last_updated = EXCLUDED.last_updated,
                fetched_at = EXCLUDED.fetched_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public RepositoryUpsertDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void upsertAll(List<RepositoryEntity> repositories) {
        if (repositories.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(UPSERT_SQL, repositories, repositories.size(), (ps, r) -> {
            ps.setLong(1, r.getId());
            ps.setString(2, r.getName());
            ps.setString(3, r.getFullName());
            ps.setString(4, r.getDescription());
            ps.setString(5, r.getOwner());
            ps.setString(6, r.getLanguage());
            ps.setInt(7, r.getStars());
            ps.setInt(8, r.getForks());
            ps.setInt(9, r.getWatchers());
            ps.setInt(10, r.getOpenIssues());
            ps.setString(11, r.getHtmlUrl());
            ps.setObject(12, toUtc(r.getCreatedAt()), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(13, toUtc(r.getLastUpdated()), Types.TIMESTAMP_WITH_TIMEZONE);
            ps.setObject(14, toUtc(r.getFetchedAt()), Types.TIMESTAMP_WITH_TIMEZONE);
        });
    }

    private static OffsetDateTime toUtc(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}