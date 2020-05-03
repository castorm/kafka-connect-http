package com.github.castorm.kafka.connect.http.record;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class SchemedSourceRecordMapperConfigTest {

    @Test
    void whenMissingKafkaTopic_thenException() {
        assertThat(catchThrowable(() -> new SchemedSourceRecordMapperConfig(emptyMap()))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenKafkaTopic_thenInitialized() {
        assertThat(new SchemedSourceRecordMapperConfig(ImmutableMap.of("kafka.topic", "test-topic")).getTopic()).isEqualTo("test-topic");
    }
}
