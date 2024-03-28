package com.github.rmannibucau.drone.http;

import com.github.rmannibucau.drone.DronePlugin;
import io.yupiik.fusion.framework.api.scope.ApplicationScoped;
import io.yupiik.fusion.framework.api.scope.DefaultScoped;
import io.yupiik.fusion.framework.build.api.scanning.Bean;
import io.yupiik.fusion.httpclient.core.ExtendedHttpClient;
import io.yupiik.fusion.httpclient.core.ExtendedHttpClientConfiguration;
import io.yupiik.fusion.httpclient.core.listener.impl.ExchangeLogger;

import java.util.List;
import java.util.logging.Logger;

import static java.time.Clock.systemUTC;

@DefaultScoped
public class HttpClientBean {
    @Bean
    @ApplicationScoped
    public ExtendedHttpClient client() {
        return new ExtendedHttpClient(new ExtendedHttpClientConfiguration()
                .setRequestListeners(List.of(new ExchangeLogger(Logger.getLogger(DronePlugin.class.getName()), systemUTC(), false))));
    }
}
