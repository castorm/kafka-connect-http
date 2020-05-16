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
