package com.assignment.githubsearcher.exception;

import java.time.Instant;

public class GithubRateLimitException extends GithubException {
    private final Instant resetAt;

    public GithubRateLimitException(Instant resetAt) {
        super("GitHub API rate limit exceeded");
        this.resetAt = resetAt;
    }

    public Instant getResetAt() { return resetAt; }
}