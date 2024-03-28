package com.github.rmannibucau.drone.configuration;

import io.yupiik.fusion.framework.build.api.configuration.Property;
import io.yupiik.fusion.framework.build.api.configuration.RootConfiguration;

@RootConfiguration("drone")
public record DroneConfiguration(
        Build build,
        Repo repo,
        Commit commit,
        @Property(value = "pull_request") PullRequest pullRequest
) {
    public record PullRequest(String title) {
    }

    public record Build(String status) {
    }

    public record Repo(String namespace, String name, String branch) {
    }

    public record Commit(String ref, String sha, String link, Author author, String message) {
    }

    public record Author(String name, String email) {
    }
}
