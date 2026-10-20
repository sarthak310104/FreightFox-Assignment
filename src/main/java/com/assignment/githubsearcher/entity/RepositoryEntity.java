package com.assignment.githubsearcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "repositories")
public class RepositoryEntity {

    @Id
    private Long id;

    private String name;
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String owner;
    private String language;
    private int stars;
    private int forks;
    private int watchers;
    private int openIssues;
    private String htmlUrl;
    private Instant createdAt;
    private Instant lastUpdated;
    private Instant fetchedAt;

    protected RepositoryEntity() {}

    public RepositoryEntity(Long id, String name, String fullName, String description, String owner, String language, int stars, int forks, int watchers, int openIssues, String htmlUrl, Instant createdAt, Instant lastUpdated, Instant fetchedAt) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.owner = owner;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.watchers = watchers;
        this.openIssues = openIssues;
        this.htmlUrl = htmlUrl;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.fetchedAt = fetchedAt;
    }

    //Return Functions
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getFullName() { return fullName; }
    public String getDescription() { return description; }
    public String getOwner() { return owner; }
    public String getLanguage() { return language; }
    public int getStars() { return stars; }
    public int getForks() { return forks; }
    public int getWatchers() { return watchers; }
    public int getOpenIssues() { return openIssues; }
    public String getHtmlUrl() { return htmlUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdated() { return lastUpdated; }
    public Instant getFetchedAt() { return fetchedAt; }
}