package com.github.castorm.kafka.connect.http.record;

/*-
 * #%L
 * kafka-connect-http
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.castorm.kafka.connect.http.record.SimpleKvSourceRecordMapperConfigTest.Fixture.minimumConfig;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class SimpleKvSourceRecordMapperConfigTest {

    @Test
    void whenMissingKafkaTopic_thenException() {
        assertThat(catchThrowable(() -> new SimpleKvSourceRecordMapperConfig(emptyMap()))).isInstanceOf(ConfigException.class);
    }

    @Test
    void whenKafkaTopic_thenInitialized() {
        assertThat(minimumConfig(emptyMap()).getTopic()).isEqualTo("test-topic");
    }

    @Test
    void whenMissingKeyProperty_thenDefault() {
        assertThat(minimumConfig(emptyMap()).getKeyPropertyName()).isEqualTo("key");
    }

    @Test
    void whenKeyProperty_thenInitialized() {
        assertThat(minimumConfig(ImmutableMap.of("http.record.schema.key.property.name", "custom")).getKeyPropertyName()).isEqualTo("custom");
    }

    @Test
    void whenMissingValueProperty_thenDefault() {
        assertThat(minimumConfig(emptyMap()).getValuePropertyName()).isEqualTo("value");
    }

    @Test
    void whenValueProperty_thenInitialized() {
        assertThat(minimumConfig(ImmutableMap.of("http.record.schema.value.property.name", "custom")).getValuePropertyName()).isEqualTo("custom");
    }

    interface Fixture {

        static SimpleKvSourceRecordMapperConfig minimumConfig(Map<String, String> customConfig) {
            HashMap<String, String> finalConfig = new HashMap<>();
            finalConfig.put("kafka.topic", "test-topic");
            finalConfig.putAll(customConfig);
            return new SimpleKvSourceRecordMapperConfig(finalConfig);
        }
    }
}
