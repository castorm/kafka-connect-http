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
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserConfigTest.Fixture.config;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserConfigTest.Fixture.configWithout;
import static com.github.castorm.kafka.connect.http.response.jackson.JacksonHttpRecordParserConfigTest.Fixture.offsetConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class JacksonHttpRecordParserConfigTest {

    @Test
    void whenItemsPointerConfigured_thenInitialized() {
        assertThat(config("http.response.records.pointer", "/test-pointer").getRecordsPointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingItemKeyPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.response.record.key.pointer").getKeyPointer()).isEmpty();
    }

    @Test
    void whenItemKeyPointerConfigured_thenInitialized() {
        assertThat(config("http.response.record.key.pointer", "/test-pointer").getKeyPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenItemValuePointerConfigured_thenInitialized() {
        assertThat(config("http.response.record.value.pointer", "/test-pointer").getValuePointer()).isEqualTo(compile("/test-pointer"));
    }

    @Test
    void whenMissingTimestampPointerConfigured_thenInitialized() {
        assertThat(configWithout("http.response.record.timestamp.pointer").getTimestampPointer()).isEmpty();
    }

    @Test
    void whenItemTimestampPointerConfigured_thenInitialized() {
        assertThat(config("http.response.record.timestamp.pointer", "/test-pointer").getTimestampPointer()).isEqualTo(Optional.of(compile("/test-pointer")));
    }

    @Test
    void whenItemOffsetPointersHalfConfigured_thenEmpty() {
        assertThat(catchThrowable(() -> offsetConfig("key").getOffsetPointers())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenItemOffsetPointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key=/value").getOffsetPointers()).isEqualTo(ImmutableMap.of("key", compile("/value")));
    }

    @Test
    void whenItemOffsetMultiplePointersConfigured_thenInitialized() {
        assertThat(offsetConfig("key=/value,key2=/value2").getOffsetPointers()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMultiplePointersWithSpacesConfigured_thenInitialized() {
        assertThat(offsetConfig("  key  =  /value  ,  key2  =  /value2  ").getOffsetPointers()).isEqualTo(ImmutableMap.of("key", compile("/value"), "key2", compile("/value2")));
    }

    @Test
    void whenItemOffsetMoreValuesPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key=/value,/value2"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whenItemOffsetMoreKeysPointersLengthConfigured_thenInitialized() {
        assertThat(catchThrowable(() -> offsetConfig("key=/value,key2"))).isInstanceOf(IllegalStateException.class);
    }

    interface Fixture {
        static JacksonHttpRecordParserConfig config(String key, String value) {
            Map<String, String> customMap = new HashMap<>();
            customMap.put(key, value);
            return new JacksonHttpRecordParserConfig(customMap);
        }

        static JacksonHttpRecordParserConfig configWithout(String key) {
            Map<String, String> customMap = new HashMap<>();
            customMap.remove(key);
            return new JacksonHttpRecordParserConfig(customMap);
        }

        static JacksonHttpRecordParserConfig offsetConfig(String values) {
            Map<String, String> customMap = new HashMap<>();
            customMap.put("http.response.record.offset.pointer", values);
            return new JacksonHttpRecordParserConfig(customMap);
        }
    }
}
