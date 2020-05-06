package com.github.castorm.kafka.connect.http.response.jackson;

/*-
 * #%L
 * kafka-connect-http-plugin
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.core.JsonPointer.compile;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.configWithout;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.defaultMap;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpResponseParserConfigTest.Fixture.offsetConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class JacksonHttpResponseParserConfigTest {

    @Test
    void whenItemsPointerConfigured_thenInitialized() {
        assertThat(config("http.response.json.items.pointer", "/test-pointer").getItemsPointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingItemKeyPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.response.json.item.key.pointer").getItemKeyPointer()).isEmpty();
    }

    @Test
    void whenItemKeyPointerConfigured_thenInitialized() {
        assertThat(config("http.response.json.item.key.pointer", "/test-pointer").getItemKeyPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenItemValuePointerConfigured_thenInitialized() {
        assertThat(config("http.response.json.item.value.pointer", "/test-pointer").getItemValuePointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingTimestampPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.response.json.item.timestamp.pointer").getItemTimestampPointer()).isEmpty();
    }

    @Test
    void whenItemTimestampPointerConfigured_thenInitialized() {
        assertThat(config("http.response.json.item.timestamp.pointer", "/test-pointer").getItemTimestampPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenItemOffsetPointersHalfConfigured_thenEmpty() {
        assertThat(offsetConfig("key", null).getItemOffsets()).isEmpty();
    }

    @Test
    void whenItemOffsetPointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key", "/value").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value")));
    }

    @Test
    void whenItemOffsetMultiplePointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key,key2", "/value,/value2").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMultiplePointersWithSpacesConfigured_thenInitialized() {
        assertThat(offsetConfig("  key  ,  key2  ", "  /value  ,  /value2  ").getItemOffsets()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMoreValuesPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key", "/value,/value2"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenItemOffsetMoreKeysPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key,key2", "/value"))).isInstanceOf(IllegalStateException.class);
    }

    interface Fixture {
        static Map<String, String> defaultMap() {
            return new HashMap<String, String>() {{
            }};
        }

        static JacksonHttpResponseParserConfig config(String key, String value) {
            Map<String, String> customMap = defaultMap();
            customMap.put(key, value);
            return new JacksonHttpResponseParserConfig(customMap);
        }

        static JacksonHttpResponseParserConfig configWithout(String key) {
            Map<String, String> customMap = defaultMap();
            customMap.remove(key);
            return new JacksonHttpResponseParserConfig(customMap);
        }

        static JacksonHttpResponseParserConfig offsetConfig(String keys, String values) {
            Map<String, String> customMap = defaultMap();
            customMap.put("http.response.json.item.offset.value.pointer", values);
            customMap.put("http.response.json.item.offset.key", keys);
            return new JacksonHttpResponseParserConfig(customMap);
        }
    }
}
