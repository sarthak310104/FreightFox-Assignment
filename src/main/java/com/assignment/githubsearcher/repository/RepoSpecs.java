package com.assignment.githubsearcher.repository;

import com.assignment.githubsearcher.entity.RepositoryEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RepoSpecs {

    private RepoSpecs() {}

    public static Specification<RepositoryEntity> matching(String language, Integer minStars) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(language)) {
                predicates.add(cb.equal(
                        cb.lower(root.<String>get("language")),
                        language.trim().toLowerCase(Locale.ROOT)));
            }

            if (minStars != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Integer>get("stars"), minStars));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}