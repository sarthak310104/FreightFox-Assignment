package com.assignment.githubsearcher.exception;

public class GithubApiException extends GithubException {
    public GithubApiException(String message) { super(message); }
    public GithubApiException(String message, Throwable cause) { super(message, cause); }
}