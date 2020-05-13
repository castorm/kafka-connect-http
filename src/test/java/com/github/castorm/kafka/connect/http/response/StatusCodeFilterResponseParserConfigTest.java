package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
