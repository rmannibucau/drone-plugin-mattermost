package com.github.rmannibucau.drone;

import com.github.rmannibucau.drone.mattermost.MattermostCreatePost;
import io.yupiik.fusion.framework.api.ConfiguringContainer;

public final class DronePlugin {
    private DronePlugin() {
        // no-op
    }

    public static void main(final String... args) {
        try (final var container = ConfiguringContainer.of().start();
             final var entrypoint = container.lookup(MattermostCreatePost.class)) {
            entrypoint.instance().run();
        }
    }
}
