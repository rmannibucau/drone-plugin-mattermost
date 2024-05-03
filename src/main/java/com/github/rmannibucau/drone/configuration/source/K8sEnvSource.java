package com.github.rmannibucau.drone.configuration.source;

import io.yupiik.fusion.framework.api.configuration.ConfigurationSource;
import io.yupiik.fusion.framework.api.scope.DefaultScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@DefaultScoped
public class K8sEnvSource implements ConfigurationSource {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final Pattern posix = Pattern.compile("[^A-Za-z0-9]");
    private final Map<String, String> env = new HashMap<>();

    public K8sEnvSource() {
        final var src = Path.of("/run/drone/env");
        if (Files.exists(src)) {
            logger.info(() -> "Reading '" + src + "'");
            final var props = new Properties();
            try (final var reader = Files.newBufferedReader(src)) {
                props.load(reader);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            env.putAll(props.stringPropertyNames().stream()
                    .collect(toMap(identity(), k -> {
                        final var value = props.getProperty(k);
                        return value.startsWith("\"") && value.endsWith("\"") && value.length() > 1 ? value.substring(1, value.length() - 1) : value;
                    }, (a, b) -> b)));
            logger.finest(() -> "Loaded env:\n" + props);
        } else {
            logger.warning(() -> "No file '" + src + "' found");
        }
    }

    @Override
    public String get(final String key) {
        if (env.isEmpty()) {
            return null;
        }
        return env.get(posix.matcher(key).replaceAll("_").toUpperCase(ROOT));
    }
}
