package com.assignment.githubsearcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "github.api")
public record GithubProperties(
        String baseUrl,
        String token,
        int perPage,
        Duration connectTimeout,
        Duration readTimeout) {
}