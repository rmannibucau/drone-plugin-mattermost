
package com.github.rmannibucau.drone.configuration;

import io.yupiik.fusion.framework.build.api.configuration.RootConfiguration;

// settings section instead of env (Configuration)
@RootConfiguration("plugin")
public record PluginConfiguration(String base, String token, String channelId, String template) {

}