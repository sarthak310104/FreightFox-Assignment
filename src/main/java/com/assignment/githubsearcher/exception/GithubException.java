package com.assignment.githubsearcher.exception;

public abstract class GithubException extends RuntimeException {
    protected GithubException(String message) { super(message); }
    protected GithubException(String message, Throwable cause) { super(message, cause); }
}