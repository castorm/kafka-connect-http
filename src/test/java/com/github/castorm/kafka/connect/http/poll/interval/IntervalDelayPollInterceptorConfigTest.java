package com.github.castorm.kafka.connect.http.poll.interval;

import com.github.castorm.kafka.connect.http.poll.IntervalDelayPollInterceptorConfig;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class IntervalDelayPollInterceptorConfigTest {

    @Test
    void whenPollIntervalMillis_thenDefault() {
        assertThat(new IntervalDelayPollInterceptorConfig(emptyMap()).getPollIntervalMillis()).isEqualTo(60000L);
    }

    @Test
    void whenPollIntervalMillis_thenInitialized() {
        assertThat(new IntervalDelayPollInterceptorConfig(ImmutableMap.of("http.source.poll.interval.millis", "42")).getPollIntervalMillis()).isEqualTo(42L);
    }
}
