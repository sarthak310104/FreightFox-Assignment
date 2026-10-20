CREATE TABLE repositories (
    id            BIGINT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    full_name     VARCHAR(512) NOT NULL,
    description   TEXT,
    owner         VARCHAR(255) NOT NULL,
    language      VARCHAR(100),
    stars         INTEGER NOT NULL DEFAULT 0,
    forks         INTEGER NOT NULL DEFAULT 0,
    watchers      INTEGER NOT NULL DEFAULT 0,
    open_issues   INTEGER NOT NULL DEFAULT 0,
    html_url      VARCHAR(512),
    created_at    TIMESTAMPTZ,
    last_updated  TIMESTAMPTZ,
    fetched_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_repositories_language ON repositories (LOWER(language));
CREATE INDEX idx_repositories_stars ON repositories (stars DESC);