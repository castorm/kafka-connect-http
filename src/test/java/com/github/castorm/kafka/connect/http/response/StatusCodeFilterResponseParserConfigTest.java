package com.github.castorm.kafka.connect.http.response;

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

import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class StatusCodeFilterResponseParserConfigTest {

    @Test
    void whenNoDelegate_thenDefault() {
        assertThat(config(emptyMap()).getDelegateParser()).isInstanceOf(JacksonHttpResponseParser.class);
    }

    @Test
    void whenDelegate_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.parser.delegate", TestResponseParser.class.getName())).getDelegateParser()).isInstanceOf(TestResponseParser.class);
    }

    public static class TestResponseParser implements HttpResponseParser {

        @Override
        public List<HttpRecord> parse(HttpResponse response) {
            return null;
        }
    }

    private static StatusCodeFilterResponseParserConfig config(Map<String, Object> settings) {
        return new StatusCodeFilterResponseParserConfig(settings);
    }
}
