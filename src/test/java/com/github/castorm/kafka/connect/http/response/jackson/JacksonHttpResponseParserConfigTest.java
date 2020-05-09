package com.github.castorm.kafka.connect.http.response.jackson;

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

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.config;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class JacksonHttpResponseParserConfigTest {

    @Test
    void whenItemsParserClassConfigured_thenInitialized() {
        assertThat(config(ImmutableMap.of("http.response.item.parser", "com.github.castorm.kafka.connect.http.response.jackson.JacksonItemParser")).getItemParser())
                .isInstanceOf(JacksonItemParser.class);
    }

    @Test
    void whenMissingItemParserClassConfigured_thenInitialized() {
        assertThat(config(emptyMap()).getItemParser()).isInstanceOf(JacksonItemParser.class);
    }

    interface Fixture {
        static JacksonHttpResponseParserConfig config(Map<String, String> settings) {
            return new JacksonHttpResponseParserConfig(settings);
        }
    }
}
