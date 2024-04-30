package com.github.rmannibucau.drone.configuration.source;

import io.yupiik.fusion.framework.api.configuration.ConfigurationSource;
import io.yupiik.fusion.framework.api.scope.DefaultScoped;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@DefaultScoped
public class K8sEnvSource implements ConfigurationSource {
    private final Pattern posix = Pattern.compile("[^A-Za-z0-9]");
    private final Map<String, String> env = new HashMap<>();

    public K8sEnvSource() {
        final var src = Path.of("/run/drone/env");
        if (Files.exists(src)) {
            /* file contains the env and is "dynamic" compared to the container env
            DRONE_BUILD_FINISHED="1714473226"
            DRONE_BUILD_STATUS="success"
            DRONE_STAGE_FINISHED="1714473226"
            DRONE_STAGE_STATUS="success"
            io.drone="true"
            io.drone.build.number="2522"
            io.drone.created="1714473169"
            io.drone.expires="1714483969"
            io.drone.protected="false"
            io.drone.repo.name="bar"
            io.drone.repo.namespace="foo"
            io.drone.repo.slug="foo/bar"
            io.drone.stage.name="default"
            io.drone.stage.number="1"
            io.drone.system.host="xxx"
            io.drone.system.proto="http"
            io.drone.system.version="2.18.0"
             */
            final var props = new Properties();
            try (final var reader = Files.newBufferedReader(src)) {
                props.load(reader);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            env.putAll(props.stringPropertyNames().stream()
                    .collect(toMap(identity(), props::getProperty, (a, b) -> b)));
        }
    }

    @Override
    public String get(final String key) {
        return env.get(posix.matcher(key).replaceAll("_").toUpperCase(ROOT));
    }
}
