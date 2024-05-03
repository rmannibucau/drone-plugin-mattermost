package com.github.rmannibucau.drone.mattermost;

import com.github.rmannibucau.drone.configuration.DroneConfiguration;
import com.github.rmannibucau.drone.configuration.MattermostConfiguration;
import com.github.rmannibucau.drone.configuration.PluginConfiguration;
import io.yupiik.fusion.framework.api.scope.DefaultScoped;
import io.yupiik.fusion.framework.build.api.json.JsonModel;
import io.yupiik.fusion.framework.build.api.json.JsonProperty;
import io.yupiik.fusion.framework.handlebars.HandlebarsCompiler;
import io.yupiik.fusion.httpclient.core.ExtendedHttpClient;
import io.yupiik.fusion.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.Locale.ROOT;
import static java.util.Optional.ofNullable;

@DefaultScoped
public class MattermostCreatePost implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final JsonMapper jsonMapper;
    private final MattermostConfiguration env;
    private final ExtendedHttpClient http;
    private final DroneConfiguration drone;
    private final PluginConfiguration plugin;

    public MattermostCreatePost(final JsonMapper jsonMapper, final ExtendedHttpClient http,
                                final MattermostConfiguration env, final PluginConfiguration plugin, final DroneConfiguration drone) {
        this.jsonMapper = jsonMapper;
        this.http = http;
        this.env = env;
        this.plugin = plugin;
        this.drone = drone;
    }

    @Override
    public void run() {
        final var message = message();
        final var createPost = new CreatePost(ofNullable(plugin.channelId()).orElseGet(env::channelId), message);
        final var uri = URI.create(ofNullable(plugin.base()).orElseGet(env::base)).resolve("/api/v4/posts");
        final var result = http
                .send(HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(jsonMapper.toString(createPost)))
                        .uri(uri)
                        .header("authorization", "Token " + ofNullable(plugin.token()).orElseGet(env::token))
                        .header("content-type", "application/json")
                        .header("accept", "application/json")
                        .build());
        if (result.statusCode() != 201) {
            throw new IllegalStateException("Invalid post: " + result + "\n" + result.body());
        }
        Logger.getLogger(getClass().getName())
                .info(() -> "Sent " + createPost + " to '" + uri + "'");
    }

    private String message() {
        if (plugin.template() == null || plugin.template().isBlank()) {
            return createDefaultMessage();
        }
        return createHandlebarsMessage();
    }

    private String createHandlebarsMessage() {
        return new HandlebarsCompiler(
                // for now default on empty, to revisit depending the complexity of the templates to come
                (data, name) -> {
                    // enable to use drone.commit.url for DRONE_COMMIT_URL env var
                    final var key = (data instanceof Visited v ? v.prefix() + '_' : "") + name.toUpperCase(ROOT);
                    final var value = System.getenv(key);
                    if (value != null) {
                        return value;
                    }

                    final var prefix = key + '_';
                    if (System.getenv().keySet().stream().anyMatch(k -> k.startsWith(prefix))) {
                        return new Visited(prefix);
                    }

                    if ("is_success".equalsIgnoreCase(key)) {
                        return isSuccess() ? true : null;
                    }

                    return null;
                })
                .compile(new HandlebarsCompiler.CompilationContext(
                        new HandlebarsCompiler.Settings()
                                .helpers(Map.of(
                                        "uc", o -> o == null ? "" : o.toString().toUpperCase(ROOT),
                                        "lc", o -> o == null ? "" : o.toString().toLowerCase(ROOT))),
                        plugin.template()))
                .render(Map.of());
    }

    private String createDefaultMessage() {
        final var emoji = isSuccess() ? ":tada:" : ":x:";
        final var message = new StringBuilder("## ")
                .append(drone.repo().namespace()).append('/').append(drone.repo().name()).append(' ')
                .append('#').append(drone.build().number()).append(' ')
                .append(emoji).append('\n');
        message.append("[`")
                .append(drone.repo().branch()).append(" - ")
                .append(drone.commit().ref()).append(" - ")
                .append(drone.commit().sha())
                .append("`](")
                .append(drone.commit().link())
                .append(") by `")
                .append(ofNullable(drone.commit().author().name())
                        .or(() -> ofNullable(drone.commit().author().email()))
                        .orElse("?"))
                .append("`\n");
        ofNullable(drone.pullRequest().title())
                .filter(Predicate.not(String::isBlank))
                .ifPresent(title -> message.append("### ").append(title).append('\n'));
        ofNullable(drone.commit().message())
                .filter(Predicate.not(String::isBlank))
                .ifPresent(msg -> message.append("> ").append(msg.replace("\n", "\n> ")));
        return message.toString();
    }

    private boolean isSuccess() {
        final var buildStatus = drone.build().status();
        final var stageStatus = drone.stage().status();
        final boolean result = "success".equals(buildStatus) && "success".equals(stageStatus);
        logger.info(() -> "Build status: " + buildStatus + ", stage status: " + stageStatus + "; success=" + result);
        return result;
    }

    private record Visited(String prefix) {
        @Override
        public String toString() {
            return prefix;
        }
    }

    @JsonModel
    public record CreatePost(@JsonProperty("channel_id") String channelId, String message) {
    }
}
