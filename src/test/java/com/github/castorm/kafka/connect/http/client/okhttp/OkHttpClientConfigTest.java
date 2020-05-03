package com.github.castorm.kafka.connect.http.client.okhttp;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class OkHttpClientConfigTest {

    @Test
    void whenNoConnectionTimeoutMillis_thenDefault() {
        assertThat(config(emptyMap()).getConnectionTimeoutMillis()).isEqualTo(2000L);
    }

    @Test
    void whenConnectionTimeoutMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.client.connection.timeout.millis", "42")).getConnectionTimeoutMillis()).isEqualTo(42L);
    }

    @Test
    void whenNoReadTimeoutMillis_thenDefault() {
        assertThat(config(emptyMap()).getConnectionTimeoutMillis()).isEqualTo(2000L);
    }

    @Test
    void whenReadTimeoutMillis_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.client.read.timeout.millis", "42")).getReadTimeoutMillis()).isEqualTo(42L);
    }

    @Test
    void whenNoKeepAliveDuration_thenDefault() {
        assertThat(config(emptyMap()).getKeepAliveDuration()).isEqualTo(300000L);
    }

    @Test
    void whenKeepAliveDuration_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.client.keep-alive.duration.millis", "42")).getKeepAliveDuration()).isEqualTo(42L);
    }

    @Test
    void whenMaxIdleConnections_thenDefault() {
        assertThat(config(emptyMap()).getMaxIdleConnections()).isEqualTo(5);
    }

    @Test
    void whenMaxIdleConnections_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.client.max-idle", "42")).getMaxIdleConnections()).isEqualTo(42L);
    }

    private static OkHttpClientConfig config(Map<String, String> config) {
        return new OkHttpClientConfig(config);
    }
}
