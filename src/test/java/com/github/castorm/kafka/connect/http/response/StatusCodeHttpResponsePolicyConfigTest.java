package com.github.castorm.kafka.connect.http.response;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeHttpResponsePolicyConfigTest {

    @Test
    void whenNoDelegate_thenDefault() {
        assertThat(config(emptyMap()).getProcessCodes()).containsExactlyInAnyOrder(rangeClosed(200, 299).boxed().distinct().toArray(Integer[]::new));
    }

    @Test
    void whenDelegate_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.policy.codes.process", "200..201")).getProcessCodes()).containsExactlyInAnyOrder(200, 201);
    }

    @Test
    void whenNoPolicy_thenDefault() {
        assertThat(config(emptyMap()).getSkipCodes()).containsExactlyInAnyOrder(rangeClosed(300, 399).boxed().distinct().toArray(Integer[]::new));
    }

    @Test
    void whenPolicy_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.policy.codes.skip", "300..301")).getSkipCodes()).containsExactlyInAnyOrder(300, 301);
    }

    private static StatusCodeHttpResponsePolicyConfig config(Map<String, Object> settings) {
        return new StatusCodeHttpResponsePolicyConfig(settings);
    }
}
