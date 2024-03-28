
package com.github.rmannibucau.drone.configuration;

import io.yupiik.fusion.framework.build.api.configuration.RootConfiguration;

// plain env, supports MATTERMOST_* env var since it is often more convenient than settings
@RootConfiguration("mattermost")
public record MattermostConfiguration(String base, String token, String channelId) {

}