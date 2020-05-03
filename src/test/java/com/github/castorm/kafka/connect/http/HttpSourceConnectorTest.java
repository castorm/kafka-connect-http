package com.github.castorm.kafka.connect.http;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpSourceConnectorTest {

    HttpSourceConnector connector = new HttpSourceConnector();

    @Test
    void whenTaskClass_thenHttpSourceTask() {
        assertThat(connector.taskClass()).isEqualTo(HttpSourceTask.class);
    }

    @Test
    void whenConfig_thenNotEmpty() {
        assertThat(connector.config().configKeys()).isNotEmpty();
    }

    @Test
    void whenSeveralTaskConfigs_thenAsManyReturned() {
        assertThat(connector.taskConfigs(3)).hasSize(3);
    }

    @Test
    void whenSeveralTaskConfigs_thenAllWithConnectorConfig() {

        ImmutableMap<String, String> myMap = ImmutableMap.of("key", "value");

        connector.start(myMap);

        assertThat(connector.taskConfigs(3)).containsExactly(myMap, myMap, myMap);
    }
}
