package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.config;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class JacksonHttpResponseParserConfigTest {

    @Test
    void whenItemsParserClassConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.record.parser", "com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParser")).getRecordParser())
                .isInstanceOf(JacksonHttpRecordParser.class);
    }

    @Test
    void whenMissingItemParserClassConfigured_thenInitialized() {
        assertThat(config(emptyMap()).getRecordParser()).isInstanceOf(JacksonHttpRecordParser.class);
    }

    interface Fixture {
        static JacksonHttpResponseParserConfig config(Map<String, String> settings) {
            return new JacksonHttpResponseParserConfig(settings);
        }
    }
}
