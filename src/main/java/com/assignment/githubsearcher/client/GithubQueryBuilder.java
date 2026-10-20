package com.assignment.githubsearcher.client;

import org.springframework.util.StringUtils;

public final class GithubQueryBuilder {

    private GithubQueryBuilder() {}

    public static String build(String query, String language) {
        StringBuilder searchQuery = new StringBuilder(query.trim()).append(" in:name");

        if (StringUtils.hasText(language)) {
            String trimmedLanguage = language.trim();
            searchQuery.append(" language:");
            if (trimmedLanguage.contains(" ")) {
                searchQuery.append('"').append(trimmedLanguage).append('"');
            } else {
                searchQuery.append(trimmedLanguage);
            }
        }

        return searchQuery.toString();
    }
}